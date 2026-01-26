USE flighton;
CREATE TABLE aerolineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(2) NOT NULL UNIQUE,
    nombre VARCHAR(30) NOT NULL
);
INSERT INTO aerolineas (codigo, nombre) VALUES
('9E', 'Endeavor Air'),
('AA', 'American Airlines'),
('AQ', '9 Air'),
('AS', 'Alaska Airlines'),
('B6', 'JetBlue Airways'),
('CO', 'FTL Airlines'),
('DL', 'Delta Air Lines'),
('EV', 'ExpressJet Airlines'),
('F9', 'Frontier Airlines'),
('FL', 'Fly Lili'),
('HA', 'Hawaiian Airlines'),
('MQ', 'Envoy Air'),
('NW', 'Northwest Airlines'),
('OH', 'PSA Airlines'),
('OO', 'SkyWest Airlines'),
('UA', 'United Airlines'),
('US', 'US Airways'),
('WN', 'Southwest Airlines'),
('XE', 'JetSuiteX'),
('YV', 'Tibet Airlines Co.');
