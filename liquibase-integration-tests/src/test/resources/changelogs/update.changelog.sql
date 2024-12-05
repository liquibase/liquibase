--liquibase formatted sql

--changeset mallod:1 labels:generateChangelogWithEmptyTable,both
CREATE TABLE public.generate_changelog_test_sql (id int not null primary key, name varchar(255));

-- --changeset mallod:2 labels:both
-- INSERT INTO public.generate_changelog_test_sql (id, name) VALUES (1, 'Geronimo!');

