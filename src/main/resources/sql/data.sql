-- md_user
INSERT INTO `md_user`(phone, password, email, is_deleted, create_time, update_time)
VALUES ('18819487124', '$2a$10$BTchxVd1AkGjqaz9xBHKLO1X/NX2dke9e4uuFv8SIJZbPDBg.MrBG', 'lanrenguo@gmail.com', 0,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('18800000000', '$2a$10$mogGg8XSnFcmiObGVoLI7OuRmKdGQd4Txw153XvOE4p0coZYPDnPu', '980089843@qq.com', 0,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- md_app
INSERT INTO `md_app`(id, secret, name, description, owner, authorized_grant_types, authorities, scopes,
                     auto_approve_scopes,
                     access_token_validity, refresh_token_validity, callback_urls, homepage_url, create_time,
                     update_time)
VALUES ('md-id', 'md-secret', 'md', 'It is md', 1,
        'authorization_code,refresh_token,client_credentials', 'ROLE_CLIENT', 'profile', 'profile',
        60 * 60 * 12, 60 * 60 * 24 * 30, 'http://localhost:20202/md/login/oauth2/code/md', 'http://localhost:20202/md/',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('sample-id', 'sample-secret', 'md-app-sample', 'It is test-app', 1,
        'authorization_code,refresh_token', 'ROLE_CLIENT', 'profile', 'profile',
        60 * 60 * 12, 60 * 60 * 24 * 30, 'http://localhost:8080/app/login/oauth2/code/md',
        'http://localhost:20202/app/hi',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- md_role
INSERT INTO `md_role`(name, type, description, app_id, create_time, update_time)
VALUES ('anonymous', 0, '游客', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('default', 1, '普通用户', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('admin', 2, '管理员', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('sample-custom', 3, 'sample-自定义', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- md_menu
INSERT INTO `md_menu`(title, url, is_menu, description, parent_id, app_id, create_time, update_time)
VALUES ('menu1', '/menu1', 0, '菜单1', null, 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('menu2', '/menu2', 0, '菜单2', null, 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('menu1-1', '/menu1/1', 1, '菜单1-1', 1, 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('menu1-2', '/menu1/2', 1, '菜单1-2', 1, 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- md_role_menu
INSERT INTO `md_role_menu`(app_id, role_id, menu_id)
VALUES ('sample-id', 2, 1),
       ('sample-id', 2, 3),
       ('sample-id', 2, 4),
       ('sample-id', 1, 1);

-- md_permission
INSERT INTO `md_permission`(name, url, method, module, description, app_id, create_time, update_time)
VALUES ('add', '/add', 'POST', 'default module', 'add...', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('delete', '/delete', 'DELETE', 'default module', 'delete...', 'sample-id', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       ('update', '/update', 'PUT', 'default module', 'update...', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('search', '/search', 'GET', 'default module', 'search...', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('all', '/all', 'ALL', 'default module', 'all...', 'sample-id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- md_role_permission
INSERT INTO `md_role_permission`(app_id, role_id, permission_id)
VALUES ('sample-id', 2, 1),
       ('sample-id', 2, 3),
       ('sample-id', 2, 4),
       ('sample-id', 2, 5),
       ('sample-id', 1, 4);