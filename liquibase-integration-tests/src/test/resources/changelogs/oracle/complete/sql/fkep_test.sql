create user MYTESTSCHEMA no authentication;

grant unlimited tablespace to MYTESTSCHEMA;

create table MYTESTSCHEMA.MY_OTHER_TABLE (MY_FK_COLUMN number);

alter table MYTESTSCHEMA.MY_OTHER_TABLE
    add constraint MY_OTHER_TABLE_PK primary key (MY_FK_COLUMN);

create table MYTESTSCHEMA.MY_FKP_TEST_TABLE (MY_FK_COLUMN number);

alter table MYTESTSCHEMA.MY_FKP_TEST_TABLE
    add constraint MY_FOREIGN_KEY foreign key (MY_FK_COLUMN)
        references MYTESTSCHEMA.MY_OTHER_TABLE (MY_FK_COLUMN);
