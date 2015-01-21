-- cat create-users.sql | heroku pg:psql

INSERT INTO roles (rolename) VALUES ('admin'), ('user');

INSERT INTO users (username, password, fullname) VALUES ('admin', 'CRYPT:*', 'Admin');

INSERT INTO user_roles (user_id, role_id) SELECT users.id, roles.id FROM users, roles WHERE users.username = 'admin' and roles.rolename = 'admin';
