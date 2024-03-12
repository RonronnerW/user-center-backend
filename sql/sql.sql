create table t_user
(
    id          bigint auto_increment primary key,
    username    varchar(256) null comment '用户名称',
    user_count  varchar(256) null comment '账号',
    avatar_url  varchar(1024) null comment '用户头像',
    gender      tinyint null comment '性别',
    user_pwd    varchar(512)       not null comment '密码',
    email       varchar(128) null comment '邮箱',
    user_status int      default 0 null comment '状态',
    phone       varchar(128) null comment '电话',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0 not null comment '删除标记',
    role        int null comment '用户角色 0-普通用户 1- 管理员',
    planet_code varchar(10) null comment '星球编号',
    tags        varchar(1024) null comment '标签 json 列表',
    profile     varchar(512) null comment '个人简介'
) comment '用户表';

create table t_team
(
    id          bigint auto_increment comment 'id' primary key,
    name        varchar(256)       not null comment '队伍名称',
    description varchar(1024) null comment '描述',
    max_num      int      default 1 not null comment '最大人数',
    expire_time  datetime null comment '过期时间',
    user_id      bigint comment '创建人id',
    status      int      default 0 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512) null comment '密码',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    is_delete    tinyint  default 0 not null comment '是否删除'
) comment '队伍';

create table t_user_team
(
    id         bigint auto_increment comment 'id' primary key,
    user_id     bigint comment '用户id',
    team_id     bigint comment '队伍id',
    join_time   datetime null comment '加入时间',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    is_dtelete   tinyint  default 0 not null comment '是否删除'
) comment '用户队伍关系';