-- ===================================================================================

drop database if exists smartup_node;
create database smartup_node character set utf8 collate utf8_general_ci;
use smartup_node;

-- ===================================================================================

drop table if exists user;
create table user (
  user_address varchar(42) primary key ,
  name varchar(32),
  avatar_ipfs_hash varchar(64),
  create_time datetime
);

drop table if exists market;
create table market (
  tx_hash varchar(64) primary key,
  creator_address varchar(42) not null,
  market_address varchar(42),
  name varchar(32) not null ,
  description varchar(512),
  type varchar(16),
  stage varchar(16),
  create_time datetime
);

drop table if exists trade;
create table trade (
  tx_hash varchar(64) primary key ,
  stage varchar(16),
  user_address varchar(42),
  market_address varchar(42),
  type varchar(16),
  sut_amount decimal(40,20),
  ct_amount decimal(40,20),
  time datetime
);

drop table if exists post;
create table post (
  post_id bigint primary key,
  type varchar(16) comment 'root/market',
  market_address varchar(42),
  user_address varchar(42),
  title varchar(32),
  description varchar(512),
  create_time datetime
);

drop table if exists reply;
create table reply (
  reply_id bigint primary key,
  post_id bigint not null,
  father_id bigint default 0,
  user_address varchar(64),
  content varchar(512),
  create_time datetime
);
