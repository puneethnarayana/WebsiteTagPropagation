create database wtpdb;
use wtpdb;
create table seed_list( sno int not null auto_increment unique, url varchar(766) primary key, tag varchar(200));
create table HITS(sno int not null auto_increment unique, url varchar(766), tag varchar(766), primary key(url));
