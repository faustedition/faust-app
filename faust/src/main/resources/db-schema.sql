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
  source_uri varchar(150) not null,
  last_read timestamp,
  text_id bigint,
  unique (source_uri),
  foreign key (text_id) references interedition_text_layer (id)
);

create table if not exists transcribed_verse_interval (
  transcript_id bigint not null,
  verse_start int not null,
  verse_end int not null,
  foreign key (transcript_id) references transcript (id),
  unique (transcript_id, verse_start, verse_end)
);

create table if not exists archive (
  id bigint identity,
  label varchar(50) not null,
  name varchar(100) not null,
  institution varchar(100) not null,
  department varchar(100) null,
  city varchar(100) not null,
  country_code char(2) not null,
  country varchar(100) not null,
  url varchar(100) not null,
  location_lat double not null,
  location_lng double not null,
  unique (label)
);

create table if not exists document (
  id bigint identity,
  descriptor_uri varchar(100) not null,
  archive_id bigint,
  callnumber varchar(50),
  wa_id varchar(50),
  metadata clob,
  foreign key (archive_id) references archive (id)
);

create table if not exists material_unit (
  id bigint identity,
  document_id bigint not null,
  document_order int not null,
  transcript_id bigint,
  foreign key (document_id) references document (id)
);