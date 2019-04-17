
# drop database if exists smartup_node;
# create database smartup_node character set utf8 collate utf8_general_ci;
-- ===================================================================================
use smartup_node;
-- ===================================================================================

drop table if exists user;
create table user (
  user_address varchar(42) primary key ,
  name varchar(42),
  avatar_ipfs_hash varchar(64),
  code varchar(32),
  create_time datetime
);

drop table if exists market;
create table market (
  market_id varchar(16) primary key,
  tx_hash varchar(66),
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
  tx_hash varchar(66) primary key ,
  stage varchar(16),
  user_address varchar(42),
  market_address varchar(42),
  type varchar(16),
  sut_offer decimal(40,20),
  sut_amount decimal(40,20),
  ct_amount decimal(40,20),
  create_time datetime,
  block_time datetime
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

drop table if exists kline_node;
create table kline_node (
  market_address varchar(42),
  time_id varchar(32) not null ,
  segment varchar(32) not null,
  high decimal(40,20) not null,
  low decimal(40,20) not null,
  start decimal(40,20) not null,
  end decimal(40,20) not null,
  amount decimal(40,20) not null,
  count bigint not null,
  time datetime not null,
  primary key(market_address, time_id, segment)
);

drop table if exists market_data;
create table market_data (
  market_address varchar(42) primary key,
  lately_change decimal(40,20),
  last decimal(40,20),
  lately_volume decimal(40,20),
  amount decimal(40,20),
  ct_amount decimal(40,20),
  ct_top_amount decimal(40,20),
  count bigint
);

drop table if exists dict;
create table dict (
  name varchar(32) primary key,
  value varchar(128)
);
insert into dict values ('block_number', '0');