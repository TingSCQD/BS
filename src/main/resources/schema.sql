DROP DATABASE IF EXISTS BS;
CREATE DATABASE BS;
USE BS;
create table users(
    id int AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL  UNIQUE ,
    password varchar(200) NOT NULL ,
    email VARCHAR(100) NOT NULL UNIQUE
);

create table goods(
    id int AUTO_INCREMENT PRIMARY KEY ,
    title varchar(200) not null ,
    image varchar(500) not null,
    price varchar(50) not null,
    shopName varchar(200) not null ,
    source varchar(20) not null ,
    unique (title,shopName)
);

create table datePrice(
    date varchar(30),
    goods_id int,
    foreign key (goods_id) references goods(id),
    price varchar(50),
    primary key (date,goods_id)
);

create table discount(
    user_id int,
    good_id int,
    foreign key(user_id)  references users(id),
    foreign key(good_id) references goods(id),
     remind_price int default 0,
    unique (user_id,good_id)

);
