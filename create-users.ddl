-- cat create-users.ddl | heroku pg:psql

INSERT INTO roles (rolename) VALUES
    ('user'),
    ('server-administrator'),
    ('content-administrator'),
    ('admin');

INSERT INTO users (username, password, fullname, email) VALUES
    ('jetty', 'MD5:164c88b302622e17050af52c89945d44', 'Jim Jetty', 'jetty@foo.com'),
    ('admin', 'CRYPT:adpexzg3FUZAk', 'Arnold Admin', 'admin@foo.com'),
    ('other', 'OBF:1xmk1w261u9r1w1c1xmq', 'Ophelia Other', 'other@foo.com'),
    ('plain', 'plain', 'Peter Plain', 'plain@foo.com'),
    ('user', 'password', 'Ulysses User', 'user@foo.com');

INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'jetty' and roles.rolename = 'user';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'admin' and roles.rolename = 'server-administrator';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'admin' and roles.rolename = 'content-administrator';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'admin' and roles.rolename = 'admin';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'admin' and roles.rolename = 'user';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'other' and roles.rolename = 'user';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'plain' and roles.rolename = 'user';
INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'user' and roles.rolename = 'user';

SELECT * FROM roles;
SELECT * FROM users;
SELECT * FROM user_roles;
