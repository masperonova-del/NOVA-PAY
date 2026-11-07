-- NOVA PAY: create a profile automatically after a successful Auth signup.
-- Run once in Supabase Dashboard > SQL Editor.

create or replace function public.handle_new_nova_pay_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, display_name, phone)
  values (
    new.id,
    coalesce(new.raw_user_meta_data ->> 'display_name', ''),
    nullif(new.raw_user_meta_data ->> 'phone', '')
  )
  on conflict (id) do update
    set display_name = excluded.display_name,
        phone = excluded.phone,
        updated_at = now();

  return new;
end;
$$;

drop trigger if exists on_nova_pay_user_created on auth.users;

create trigger on_nova_pay_user_created
after insert on auth.users
for each row execute procedure public.handle_new_nova_pay_user();
