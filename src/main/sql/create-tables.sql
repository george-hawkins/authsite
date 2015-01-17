-- cat create-db.sql | heroku pg:psql

CREATE TABLE users (
    id SERIAL,
    username varchar(50) NOT NULL,
    password varchar(50) NOT NULL,
    fullname varchar(50) NOT NULL,
    email varchar(50),
    PRIMARY KEY (id),
    UNIQUE (username)
);
CREATE TABLE roles (
    id SERIAL,
    rolename varchar(50) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE (rolename)
);
CREATE TABLE user_roles (
    user_id integer NOT NULL,
    role_id integer NOT NULL,
    UNIQUE (user_id, role_id)
);
