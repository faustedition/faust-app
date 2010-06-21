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

create table glyph (
	id varchar(50) primary key,
	name varchar(50) not null,
	description text not null,
	equivalent varchar(25)
);

create table hand (
	id serial,
	scribe varchar(50),
	material varchar(50),
	style varchar(50),
	unique (scribe, material, style)
);

create table report (
	name varchar(50) primary key,
	generated_on timestamp not null,
	body text
);