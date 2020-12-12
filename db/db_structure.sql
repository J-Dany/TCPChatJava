/*!40030 SET NAMES UTF8 */;
/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

DROP DATABASE IF EXISTS chat;
CREATE DATABASE chat;

USE chat;

CREATE TABLE IF NOT EXISTS utenti
(
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    CONSTRAINT pk_utenti PRIMARY KEY (username)
)DEFAULT CHARACTER SET utf8 ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS messaggi
(
    id INT NOT NULL AUTO_INCREMENT,
    message VARCHAR(2048) NOT NULL,
    user VARCHAR(255) NOT NULL,
    CONSTRAINT pk PRIMARY KEY (id),
    CONSTRAINT foreign_key_utente FOREIGN KEY (`user`) REFERENCES `utenti`(`username`)
)DEFAULT CHARACTER SET utf8 ENGINE=InnoDB;

CREATE USER IF NOT EXISTS 'chat'@'localhost' IDENTIFIED BY 'Abcd1234';
GRANT ALL PRIVILEGES ON chat.* TO 'chat'@'localhost';