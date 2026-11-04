-- NOVA PAY — Supabase foundation (demo data only)
-- Run this script once in Supabase Dashboard > SQL Editor.
-- No real balances, cards, or money movement are represented here.

create table if not exists public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  display_name text not null default '',
  phone text,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists public.demo_transactions (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references auth.users(id) on delete cascade,
  label text not null check (char_length(trim(label)) between 1 and 80),
  amount bigint not null check (amount > 0),
  currency text not null default 'XAF' check (currency = 'XAF'),
  direction text not null check (direction in ('debit', 'credit')),
  status text not null default 'simulated' check (status = 'simulated'),
  created_at timestamptz not null default now()
);

create index if not exists demo_transactions_user_created_idx
  on public.demo_transactions (user_id, created_at desc);

alter table public.profiles enable row level security;
alter table public.demo_transactions enable row level security;

drop policy if exists "Users can read own profile" on public.profiles;
create policy "Users can read own profile"
  on public.profiles for select to authenticated
  using (auth.uid() = id);

drop policy if exists "Users can create own profile" on public.profiles;
create policy "Users can create own profile"
  on public.profiles for insert to authenticated
  with check (auth.uid() = id);

drop policy if exists "Users can update own profile" on public.profiles;
create policy "Users can update own profile"
  on public.profiles for update to authenticated
  using (auth.uid() = id)
  with check (auth.uid() = id);

drop policy if exists "Users can read own demo transactions" on public.demo_transactions;
create policy "Users can read own demo transactions"
  on public.demo_transactions for select to authenticated
  using (auth.uid() = user_id);

drop policy if exists "Users can create own demo transactions" on public.demo_transactions;
create policy "Users can create own demo transactions"
  on public.demo_transactions for insert to authenticated
  with check (auth.uid() = user_id and status = 'simulated' and amount > 0);

-- Transactions are intentionally immutable from the client:
-- no UPDATE or DELETE policy is defined.
-- Never store a real card number, CVV, bank credential, or service-role key here.
