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
  descriptor_path varchar(100) not null,
  last_read timestamp not null,
  archive_id bigint,
  callnumber varchar(100),
  wa_id varchar(50),
  metadata clob,
  foreign key (archive_id) references archive (id) on delete cascade
);

create table if not exists material_unit (
  id bigint identity,
  document_id bigint not null,
  document_order int not null,
  foreign key (document_id) references document (id) on delete cascade,
  unique (document_id, document_order)
);

create table if not exists transcript (
  id bigint identity,
  material_unit_id bigint not null,
  source_uri varchar(150) not null,
  last_read timestamp,
  text_image_link_uri varchar(150),
  text_content clob,
  foreign key (material_unit_id) references material_unit (id) on delete cascade,
  unique (source_uri)
);

create table if not exists facsimile (
  transcript_id bigint not null,
  facsimile_order int not null,
  path varchar(150) not null,
  foreign key (transcript_id) references transcript (id) on delete cascade,
  unique (transcript_id, facsimile_order)
);

create table if not exists document_segment (
  document_id bigint not null,
  transcript_type tinyint not null,
  segment_unit tinyint not null,
  segment_order int not null,
  segment_start int not null,
  segment_end int not null,
  foreign key (document_id) references document (id) on delete cascade,
  unique (document_id, transcript_type, segment_unit, segment_order)
);

create index if not exists document_segment_range on document_segment (document_id, transcript_type, segment_unit, segment_start, segment_end);

create table if not exists transcribed_verse_interval (
  transcript_id bigint not null,
  verse_start int not null,
  verse_end int not null,
  foreign key (transcript_id) references transcript (id) on delete cascade,
  unique (transcript_id, verse_start, verse_end)
);

create table if not exists document_transcript_alignment (
  document_id bigint not null,
  doc_start int not null,
  doc_end int not null,
  text_start int not null,
  text_end int not null,
  is_changed bool not null default false,
  is_transposition bool not null default false,
  foreign key (document_id) references document (id) on delete cascade
);

create index if not exists document_transcript_alignment_doc on document_transcript_alignment (document_id, doc_start, doc_end);
create index if not exists document_transcript_alignment_text on document_transcript_alignment (document_id, text_start, text_end);