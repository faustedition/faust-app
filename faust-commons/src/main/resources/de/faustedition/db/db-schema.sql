create table identifier (
	xml_path varchar(200) not null,
	id_type varchar(20) not null,
	id varchar(200) not null
);

create index identifier_path_idx on identifier (xml_path);
create index identifier_id_idx on identifier (id);

create table encoding_status (
	xml_path varchar(200) unique,
	encoding_status varchar(20) not null
);

create table hand (
	id serial,
	scribe varchar(50),
	material varchar(50),
	style varchar(50),
	unique (scribe, material, style)
);