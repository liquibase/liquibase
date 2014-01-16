--liquibase formatted sql

--changeset bob:1
create table item (
  id int not null primary key,
  name varchar(255) not null,
  description varchar(255)
);
--rollback drop table item

--changeset bob:2
create table account (
  id int not null primary key,
  username varchar(20) not null
);
--rollback drop table account

--changeset alice:3
create table cart_item (
  id int not null primary key,
  account_id int not null,
  item_id int not null,
  quantity int not null,
  constraint fk_cart_account foreign key (account_id) references account(id),
  constraint fk_cart_item foreign key (item_id) references item(id)
)
--rollback drop table cart_item

