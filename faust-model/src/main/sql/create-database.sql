create database faustedition character set = 'latin1' collate = 'latin1_german1_ci';
grant all privileges on faustedition.* to faustedition@localhost identified by 'faustedition';
grant all privileges on faustedition.* to faustedition@127.0.0.1 identified by 'faustedition';
flush privileges;