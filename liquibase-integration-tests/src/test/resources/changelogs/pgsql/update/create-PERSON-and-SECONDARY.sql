create table "public"."PERSON" (
    "ID"        integer primary key generated always as identity,
    "FIRSTNAME" varchar(50),
    "LASTNAME"  varchar(50) not null,
    "STATE"     char(2)
);

create table "public"."SECONDARY" (
    "ID"        integer primary key generated always as identity,
    "ADDRESS"   varchar(50),
    "COUNTRY"   varchar(50) not null,
    "REGION"    char(2)
);

insert into "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") values ('John', 'Kennedy', 'DC');
insert into "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") values ('Jacqueline', 'Kennedy', 'DC');

insert into "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") values ('1600 Pennsylvania Avenue', 'United States', 'NA');
insert into "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") values ('280 Mulberry Street', 'United States', 'NA');

commit;
