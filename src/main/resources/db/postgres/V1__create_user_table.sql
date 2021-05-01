CREATE TABLE IF NOT EXISTS users
(
    username                  varchar(50)   not null,
    email                     varchar(100)  not null,
    password                  varchar(100)  not null,
    enabled                   boolean       not null default false,
    created_at                timestamp     not null,
    updated_at                timestamp     not null,
    last_logged_in_at         timestamp     not null,
    primary key(username)
);

CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE UNIQUE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username_password ON users(username, password);

CREATE TABLE IF NOT EXISTS user_roles
(
    user_role_id              SERIAL PRIMARY KEY,
    username                  varchar(50)   not null,
    role                      varchar(20)   not null,
    unique(username, role),
    foreign key (username) references users (username)
);

insert into users(username, email, password, enabled, created_at, updated_at, last_logged_in_at) values ('username', 'email', 'password', true, NOW(), NOW(), NOW());
insert into user_roles(username, role) values ('username', 'ROLE_USER');
