CREATE DATABASE flighton CHARACTER SET utf8;
USE flighton;
CREATE TABLE aerolineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(2) NOT NULL UNIQUE
);
INSERT INTO aerolineas (codigo) VALUES
('9E'),
('AA'),
('AQ'),
('AS'),
('B6'),
('CO'),
('DL'),
('EV'),
('F9'),
('FL'),
('HA'),
('MQ'),
('NW'),
('OH'),
('OO'),
('UA'),
('US'),
('WN'),
('XE'),
('YV');
