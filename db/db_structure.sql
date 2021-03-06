DROP DATABASE IF EXISTS chat;
CREATE DATABASE chat CHARACTER SET utf8;

USE chat;

CREATE TABLE IF NOT EXISTS utenti
(
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_utenti PRIMARY KEY (username)
);

CREATE USER IF NOT EXISTS 'chat'@'localhost' IDENTIFIED BY 'Abcd1234'; -- MODIFY !! --
GRANT ALL PRIVILEGES ON chat.* TO 'chat'@'localhost';