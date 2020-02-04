--liquibase formatted sql

--changeset your.name:1
create table person (
    id int primary key,
    name varchar(50) not null,
    address1 varchar(50),
    address2 varchar(50),
    city varchar(30)
);

--changeset your.name:2
create table company (
    id int primary key,
    name varchar(50) not null,
    address1 varchar(50),
    address2 varchar(50),
    city varchar(30)
);

--changest other.dev:2
alter table person add column country varchar(2);

/* Uncomment the following code snippet to add the changeSet */
/*
--changeset your.name:3
ALTER TABLE PUBLIC.person ADD worksfor_company_id INT;

--changeset your.name:4
ALTER TABLE person ADD FOREIGN KEY (fk_person_worksfor) REFERENCES company(id) ;
*/
