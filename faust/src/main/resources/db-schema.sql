create table key_concordance (
	repository varchar(100) not null,
	call_number varchar(50) not null,
	old_call_number varchar(50),
	wa_key varchar(50),
	wa_citation varchar(100)	
);

create table encoding_status (
	xml_path varchar(200) unique,
	encoding_status varchar(20) not null
);