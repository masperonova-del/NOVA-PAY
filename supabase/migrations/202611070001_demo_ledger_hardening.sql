-- NOVA PAY demo ledger hardening — 2026-11-07
-- Apply AFTER supabase/schema.sql in Supabase SQL Editor.
-- DEMO ONLY: this does not process, hold, or represent real money.

alter table public.demo_transactions
    add column if not exists idempotency_key uuid;

create unique index if not exists demo_transactions_user_idempotency_uidx
    on public.demo_transactions (user_id, idempotency_key)
    where idempotency_key is not null;

-- The Android client may read its own rows, but cannot write directly.
drop policy if exists "Users can create own demo transactions" on public.demo_transactions;
revoke insert, update, delete on public.demo_transactions from anon, authenticated;
grant select on public.demo_transactions to authenticated;

create or replace function public.create_demo_transaction(
    p_idempotency_key uuid,
    p_direction text,
    p_amount_xaf bigint,
    p_label text
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_id uuid;
    v_balance bigint;
begin
    if v_user_id is null then
        raise exception 'Authentication required';
    end if;
    if p_idempotency_key is null then
        raise exception 'Idempotency key required';
    end if;
    if p_direction not in ('credit', 'debit') then
        raise exception 'Invalid demo transaction direction';
    end if;
    if p_amount_xaf is null or p_amount_xaf <= 0 or p_amount_xaf > 100000000 then
        raise exception 'Invalid demo amount';
    end if;
    if p_label is null or char_length(trim(p_label)) not between 1 and 80 then
        raise exception 'Invalid demo transaction label';
    end if;

    -- Serialize demo operations for this user to prevent concurrent overdrafts.
    perform pg_advisory_xact_lock(hashtextextended(v_user_id::text, 0));

    -- A retry with the same key returns the original operation, without applying it twice.
    select id into v_id
    from public.demo_transactions
    where user_id = v_user_id and idempotency_key = p_idempotency_key;

    if v_id is not null then
        return v_id;
    end if;

    select coalesce(sum(
        case when direction = 'credit' then amount else -amount end
    ), 0)::bigint
    into v_balance
    from public.demo_transactions
    where user_id = v_user_id;

    if p_direction = 'debit' and p_amount_xaf > v_balance then
        raise exception 'Insufficient demo balance';
    end if;

    insert into public.demo_transactions (
        user_id, idempotency_key, label, amount, currency, direction, status
    )
    values (
        v_user_id, p_idempotency_key, trim(p_label), p_amount_xaf,
        'XAF', p_direction, 'simulated'
    )
    returning id into v_id;

    return v_id;
end;
$$;

revoke all on function public.create_demo_transaction(uuid, text, bigint, text) from public;
grant execute on function public.create_demo_transaction(uuid, text, bigint, text) to authenticated;

create or replace view public.demo_wallet_balances
with (security_invoker = true)
as
select
    user_id,
    coalesce(sum(
        case when direction = 'credit' then amount else -amount end
    ), 0)::bigint as balance_xaf,
    count(*)::bigint as transaction_count
from public.demo_transactions
group by user_id;

grant select on public.demo_wallet_balances to authenticated;

comment on function public.create_demo_transaction(uuid, text, bigint, text)
is 'Creates an immutable NOVA PAY demo-only transaction. Never use for real funds.';
comment on view public.demo_wallet_balances
is 'Derived demo-only balance. Not a real stored-value account.';
