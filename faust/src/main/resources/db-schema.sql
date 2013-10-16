create table if not exists transcript (
  id bigint identity,
  source_uri varchar(150) not null,
  last_read timestamp,
  text_content clob,
  unique (source_uri)
);

create table if not exists transcript_annotation (
  id bigint primary key,
  transcript_id bigint not null,
  range_start bigint not null,
  range_end bigint not null,
  annotation_data blob,
  foreign key (transcript_id) references transcript (id)
);

create index if not exists transcript_annotation_range on transcript_annotation (transcript_id, range_start, range_end);

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
  callnumber varchar(100),
  wa_id varchar(50),
  metadata clob,
  foreign key (archive_id) references archive (id)
);

create table if not exists material_unit (
  id bigint identity,
  document_id bigint not null,
  document_order int not null,
  transcript_id bigint,
  foreign key (document_id) references document (id),
  unique (document_id, document_order)
);

create table if not exists facsimile (
  material_unit_id bigint not null,
  facsimile_order int not null,
  path varchar(150) not null,
  foreign key (material_unit_id) references material_unit (id),
  unique (material_unit_id, facsimile_order)
)