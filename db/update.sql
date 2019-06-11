

# 2019-06-11

alter table notification modify notification_id varchar(32);
alter table post modify post_id varchar(32);
alter table post_data modify post_id varchar(32);
alter table post_data modify last_reply_id varchar(32);
alter table reply modify reply_id varchar(32);
alter table reply modify post_id varchar(32);
alter table reply modify father_id varchar(32);
alter table reply_data modify reply_id varchar(32);