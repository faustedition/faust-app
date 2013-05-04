create schema if not exists faust;

set schema faust;

create sequence if not exists interedition_text_id;

create table if not exists interedition_name (
  id bigint primary key,
  ln varchar(100) not null,
  ns varchar(100),
  unique (ln, ns)
);

create table if not exists interedition_text_layer (
  id bigint primary key,
  name_id bigint not null references interedition_name (id) on delete cascade,
  text_content clob not null,
  layer_data blob
);

create table if not exists interedition_text_anchor (
  id bigint primary key,
  from_id bigint not null references interedition_text_layer (id) on delete cascade,
  to_id bigint not null references interedition_text_layer (id) on delete cascade,
  range_start bigint not null,
  range_end bigint not null
);

create index if not exists interedition_text_range on interedition_text_anchor (range_start, range_end);

create table if not exists transcript (
  id bigint identity,
  source_uri varchar(100) not null,
  material_unit_id bigint not null,
  text_id bigint,
  unique(source_uri),
  foreign key (text_id) references interedition_text_layer (id)
);

create table if not exists transcribed_verse_interval (
  transcript_id bigint not null,
  verse_start int not null,
  verse_end int not null,
  foreign key (transcript_id) references transcript (id),
  unique (transcript_id, verse_start, verse_end)
);
