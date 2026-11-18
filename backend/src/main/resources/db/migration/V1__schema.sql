create table entries (id bigserial primary key,tenant varchar(40) not null,field1 text not null,field2 text not null,field3 text not null,created_at timestamptz not null default now());
create index idx_entries_tenant on entries(tenant,id desc);
