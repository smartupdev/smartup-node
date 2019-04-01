-- ===================================================================================

drop database if exists smartup_node;
create database smartup_node character set utf8 collate utf8_general_ci;
use smartup_node;

-- ===================================================================================

drop table if exists user;
create table user (
  user_address varchar(66) primary key ,
  name varchar(128),
  avatar_ipfs_hash varchar(128),
  create_time timestamp
);

drop table if exists market;
create table market (
  tx_hash varchar(128) primary key,
  creator_address varchar(66),
  market_address varchar(66),
  name varchar(32),
  description varchar(128),
  type varchar(16),
  stage varchar(16),
  create_time timestamp
);

drop table if exists trade;
create table trade (
  tx_hash varchar(128) primary key ,
  stage varchar(16),
  market_address varchar(66),
  type varchar(16),
  sut_amount decimal(40,20),
  ct_amount decimal(40,20),
  time timestamp
);

drop table if exists post;
create table post (
  post_id bigint primary key,
  type varchar(16) comment 'root/market',
  market_address varchar(66),
  user_address varchar(128),
  title varchar(128),
  intro varchar(128),
  create_time timestamp
);

drop table if exists reply;
create table reply (
  reply_id bigint primary key,
  post_id bigint not null,
  father_id bigint default 0,
  user_address varchar(128),
  content varchar(128),
  time timestamp
);


select *
from user where user_address = '123';