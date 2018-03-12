--liquibase formatted sql
--changeset bob:3
CREATE VIEW sampl_main.v_persons AS
select p.id, p.first_name, p.last_name, concat(p.first_name, concat(' ', p.last_name)) full_name, u.login
from sampl_main.persons p
left join sampl_admin.users u on u.person_id = p.id
;

GRANT SELECT ON sampl_main.v_persons TO "sampl_admin";
