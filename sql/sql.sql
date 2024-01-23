create table t_user
(
    id          bigint auto_increment
        primary key,
    username    varchar(256)                       null comment '用户名称',
    user_count  varchar(256)                       null comment '账号',
    avatar_url  varchar(1024)                      null comment '用户头像',
    gender      tinyint                            null comment '性别',
    user_pwd    varchar(512)                       not null comment '密码',
    email       varchar(128)                       null comment '邮箱',
    user_status int      default 0                 null comment '状态',
    phone       varchar(128)                       null comment '电话',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '删除标记',
    role        int                                null comment '用户角色 0-普通用户 1- 管理员',
    planet_code varchar(10)                        null comment 'xing'
)
    comment '用户表';

