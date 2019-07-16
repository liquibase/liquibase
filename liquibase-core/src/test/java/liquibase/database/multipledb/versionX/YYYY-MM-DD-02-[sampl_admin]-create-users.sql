--liquibase formatted sql
--changeset bob:2
CREATE TABLE sampl_admin.users (
    ID INT PRIMARY KEY,
    LOGIN VARCHAR(32) NOT NULL,
    PSWDCRYD VARCHAR(128) NOT NULL,
    PERSON_ID INT NOT NULL
);

GRANT SELECT ON sampl_admin.users TO "sampl_main";

CREATE VIEW sampl_admin.v_users AS
select u.id, u.login, concat(p.first_name, concat(' ', p.last_name)) full_name
from sampl_admin.users u
join sampl_main.persons p on p.id = u.person_id
;

INSERT INTO sampl_admin.users (ID, LOGIN, PSWDCRYD, PERSON_ID) VALUES (1, 'nvoxland', 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA', 1);
INSERT INTO sampl_admin.users (ID, LOGIN, PSWDCRYD, PERSON_ID) VALUES (2, 'bob', 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA', 2);
