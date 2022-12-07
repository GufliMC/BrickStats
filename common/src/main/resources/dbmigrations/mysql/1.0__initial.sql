-- apply changes
create table actors (
  id                            varchar(40) not null,
  type                          varchar(255) not null,
  constraint pk_actors primary key (id,type)
);

create table records (
  id                            varchar(40) not null,
  keyname                       varchar(255) not null,
  value                         integer default 0 not null,
  created_at                    datetime(6) not null,
  updated_at                    datetime(6) not null,
  constraint pk_records primary key (id)
);

create table records_actors (
  record_id                     varchar(40) not null,
  actor_id                      varchar(40) not null,
  constraint pk_records_actors primary key (record_id,actor_id)
);

-- foreign keys and indices
create index ix_records_actors_records on records_actors (record_id);
alter table records_actors add constraint fk_records_actors_records foreign key (record_id) references records (id) on delete restrict on update restrict;

create index ix_records_actors_actors on records_actors (actor_id);
alter table records_actors add constraint fk_records_actors_actors foreign key (actor_id) references actors (id) on delete restrict on update restrict;

