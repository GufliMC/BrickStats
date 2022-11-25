-- apply changes
create table stats (
  id                            uuid not null,
  entity_id                     uuid not null,
  relation                      uuid default '00000000-0000-0000-0000-000000000000' not null,
  keyname                       varchar(255) not null,
  value                         integer default 0 not null,
  created_at                    timestamp not null,
  updated_at                    timestamp not null,
  constraint uq_stats_entity_id_relation_keyname unique (entity_id,relation,keyname),
  constraint pk_stats primary key (id)
);

