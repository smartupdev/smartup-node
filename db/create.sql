
drop database if exists smartup_node;
create database smartup_node character set utf8mb4 collate utf8mb4_general_ci;
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

drop table if exists user_account;
create table user_account (
  user_address varchar(42) primary key ,
  sut decimal(40,20),
  sut_amount decimal(40,20),
  update_time datetime
);

drop table if exists user_market_data;
create table user_market_data (
  user_address varchar(42),
  market_id varchar(32),
  post_count int(11),
  reply_count int(11),
  received_like_count int(11),
  primary key (user_address, market_id)
);

drop table if exists `transaction`;
create table `transaction` (
  tx_hash varchar(66) primary key ,
  stage varchar(16),
  type varchar(32) not null,
  user_address varchar(42),
  market_id varchar(42),
  market_address varchar(42),
  detail varchar(1024),
  create_time datetime,
  block_time datetime
);

drop table if exists market;
create table market (
  market_id varchar(16) primary key,
  tx_hash varchar(66),
  creator_address varchar(42) not null,
  market_address varchar(42),
  name varchar(64) not null ,
  cover varchar(64),
  photo varchar(64),
  description varchar(2048),
  type varchar(16),
  stage varchar(16),
  status varchar(16),
  init_sut decimal(40, 20),
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
  post_id varchar(32) primary key,
  type varchar(16),
  market_id varchar(42),
  market_address varchar(42),
  user_address varchar(42),
  title varchar(512),
  description mediumtext,
  photo varchar(66),
  create_time datetime
);

drop table if exists post_data;
create table post_data(
  post_id varchar(32) primary key,
  reply_count int,
  like_count int,
  dislike_count int,
  last_reply_time datetime,
  last_reply_id varchar(32)
);

drop table if exists reply;
create table reply (
  reply_id varchar(32) primary key,
  post_id varchar(32) not null,
  father_id varchar(32) default 0,
  user_address varchar(64),
  content varchar(512),
  create_time datetime
);

drop table if exists reply_data;
create table reply_data(
  reply_id varchar(32) primary key,
  like_count int,
  dislike_count int
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
  count bigint,
  post_count int(11),
  user_count int(11)
);

drop table if exists dict;
create table dict (
  name varchar(32) primary key,
  value varchar(1024)
);
insert into dict values ('block_number', '0');

drop table if exists ct_account;
create table ct_account(
  user_address varchar(42),
  market_address varchar(42),
  amount decimal(40,20),
  last_update_time datetime,
  primary key(user_address, market_address)
);

drop table if exists collect;
create table collect (
  user_address varchar(42),
  type varchar(64),
  object_mark varchar(64),
  create_time datetime,
  primary key (user_address, type, object_mark)
);

drop table if exists liked;
create table liked (
  user_address varchar(42),
  market_Id varchar(42),
  type varchar(64),
  object_mark varchar(64),
  is_like tinyint(1),
  create_time datetime,
  primary key (user_address, market_Id, type, object_mark)
);

drop table if exists notification;
create table notification (
  notification_id varchar(32) primary key,
  user_address varchar(42),
  style varchar(32),
  type varchar(32),
  content varchar(1024),
  is_read tinyint(1),
  create_time datetime,
  title_en varchar(512),
  title_zh_cn varchar(512),
  title_zh_tw varchar(512),
  text_en varchar(512),
  text_zh_cn varchar(512),
  text_zh_tw varchar(512)
);

drop table if exists proposal;
create table proposal(
  proposal_id bigint primary key,
  tx_hash varchar(66),
  stage varchar(16),
  type varchar(16),
  market_address varchar(42),
  user_address varchar(42),
  name varchar(64),
  description varchar(512),
  is_finished tinyint(1),
  create_time datetime,
  block_time datetime
);

drop table if exists proposal_sut;
create table proposal_sut(
  proposal_id bigint primary key,
  sut_amount decimal(40,20),
  is_success tinyint(1)
);

drop table if exists proposal_sut_vote;
create table proposal_sut_vote(
  proposal_vote_id bigint primary key,
  proposal_id bigint,
  tx_hash varchar(66),
  stage varchar(16),
  user_address varchar(42),
  market_address varchar(42),
  is_agree tinyint(1),
  create_time datetime,
  block_time datetime
);

drop table if exists proposal_suggest;
create table proposal_suggest(
  proposal_id bigint primary key,
  proposal_chain_id varchar(64)
);

drop table if exists proposal_option;
create table proposal_option(
  proposal_option_id bigint primary key,
  proposal_id bigint,
  `index` int(11),
  text varchar(128),
  vote_count int(11)
);

drop table if exists proposal_suggest_vote;
create table proposal_suggest_vote(
  vote_id bigint primary key,
  proposal_id bigint,
  proposal_option_id bigint,
  tx_hash varchar(66),
  stage varchar(16),
  user_address varchar(42),
  market_address varchar(42),
  `index` tinyint(1),
  create_time datetime,
  block_time datetime
);
