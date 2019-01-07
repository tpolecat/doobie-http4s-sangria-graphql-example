create table cities
(
  id         serial       primary key not null,
  name       varchar(100) not null,
  country_id varchar(2)   not null,
  district   varchar(20)  not null,
  population bigint
);
