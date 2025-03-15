-- auto-generated definition
create table user
(
    user_id          varchar(252)                       not null comment 'id'
        primary key,
    user_name        varchar(126)                       null comment '用户名',
    user_count       varchar(126)                       null comment '账户',
    user_password    varchar(126)                       null comment '密码',
    user_email       varchar(126)                       null comment '邮箱',
    sex              tinyint                            null comment '性别 (0-男，1-女)',
    user_phone       varchar(126)                       null comment '手机号',
    image_url        varchar(1000)                      null comment '用户头像',
    tags             varchar(252)                       null comment '用户标签',
    user_description varchar(252)                       null comment '描述',
    user_status      tinyint  default 0                 null comment '账户状态(0-正常，1-封禁)',
    create_time      datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    is_delete        tinyint  default 0                 null comment '删除状态(0-未删除，1-删除)',
    is_admin         tinyint  default 0                 null comment '是否为管理员 (0-普通用户，1-管理员)'
)
    comment '用户表';

