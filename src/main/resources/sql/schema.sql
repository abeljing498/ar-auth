-- md_app
CREATE TABLE `md_app`
(
    `id`                     varchar(64)   NOT NULL,
    `secret`                 varchar(32)   NOT NULL,
    `name`                   varchar(32)   NOT NULL,
    `description`            varchar(1024) DEFAULT NULL,
    `owner`                  bigint(20)    NOT NULL,
    `authorized_grant_types` varchar(255)  NOT NULL,
    `authorities`            varchar(255)  NOT NULL,
    `scopes`                 varchar(255)  NOT NULL,
    `auto_approve_scopes`    varchar(255)  NOT NULL,
    `access_token_validity`  int(11)       NOT NULL,
    `refresh_token_validity` int(11)       NOT NULL,
    `callback_urls`          varchar(2048) NOT NULL,
    `homepage_url`           varchar(1024) NOT NULL,
    `create_time`            datetime      NOT NULL,
    `update_time`            datetime      NOT NULL,
    PRIMARY KEY (`id`)
);

-- md_app_developer
CREATE TABLE `md_app_developer`
(
    `app_id`  varchar(64) NOT NULL,
    `user_id` bigint(20)  NOT NULL,
    PRIMARY KEY (`app_id`, `user_id`)
);

-- md_menu
CREATE TABLE `md_menu`
(
    `id`          bigint(20)    NOT NULL AUTO_INCREMENT,
    `title`       varchar(64)   NOT NULL,
    `url`         varchar(1024) NOT NULL,
    `is_menu`     tinyint(4)    NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `parent_id`   bigint(20)    DEFAULT NULL,
    `app_id`      varchar(64)   NOT NULL,
    `create_time` datetime      NOT NULL,
    `update_time` datetime      NOT NULL,
    PRIMARY KEY (`id`)
);

-- md_permission
CREATE TABLE `md_permission`
(
    `id`          bigint(20)    NOT NULL AUTO_INCREMENT,
    `name`        varchar(64)   NOT NULL,
    `url`         varchar(1024) NOT NULL,
    `method`      varchar(8)    NOT NULL,
    `module`      varchar(64)   NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `app_id`      varchar(64)   NOT NULL,
    `create_time` datetime      NOT NULL,
    `update_time` datetime      NOT NULL,
    PRIMARY KEY (`id`)
);

-- md_role
CREATE TABLE `md_role`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `name`        varchar(64) NOT NULL,
    `type`        tinyint(4)  NOT NULL,
    `description` varchar(1024) DEFAULT NULL,
    `app_id`      varchar(64) NOT NULL,
    `create_time` datetime    NOT NULL,
    `update_time` datetime    NOT NULL,
    PRIMARY KEY (`id`)
);

-- md_role_menu
CREATE TABLE `md_role_menu`
(
    `app_id`  varchar(64) NOT NULL,
    `role_id` bigint(20)  NOT NULL,
    `menu_id` bigint(20)  NOT NULL,
    PRIMARY KEY (`app_id`, `role_id`, `menu_id`)
);

-- md_role_permission
CREATE TABLE `md_role_permission`
(
    `app_id`        varchar(64) NOT NULL,
    `role_id`       bigint(20)  NOT NULL,
    `permission_id` bigint(20)  NOT NULL,
    PRIMARY KEY (`app_id`, `role_id`, `permission_id`)
);

-- md_user
CREATE TABLE `md_user`
(
    `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
    `phone`       varchar(64) NOT NULL,
    `password`    varchar(64) DEFAULT NULL,
    `email`       varchar(32) DEFAULT NULL,
    `is_deleted`  tinyint(4)  NOT NULL,
    `create_time` datetime    NOT NULL,
    `update_time` datetime    NOT NULL,
    PRIMARY KEY (`id`)
);

-- md_user_role
CREATE TABLE `md_user_role`
(
    `app_id`  varchar(64) NOT NULL,
    `user_id` bigint(20)  NOT NULL,
    `role_id` bigint(20)  NOT NULL,
    PRIMARY KEY (`app_id`, `user_id`, `role_id`)
);