create table key_concordance (
	repository varchar(100) not null,
	call_number varchar(50) not null,
	old_call_number varchar(50),
	wa_key varchar(50),
	wa_citation varchar(100)	
);