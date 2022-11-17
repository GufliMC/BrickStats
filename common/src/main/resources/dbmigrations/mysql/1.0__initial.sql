-- apply changes
create table stats (
  id                            varchar(40) not null,
  entity_id                     varchar(40) not null,
  relation                      varchar(40),
  keyname                       varchar(255) not null,
  value                         integer default 0 not null,
  created_at                    datetime(6) not null,
  updated_at                    datetime(6) not null,
  constraint uq_stats_entity_id_relation_keyname unique (entity_id,relation,keyname),
  constraint pk_stats primary key (id)
);

