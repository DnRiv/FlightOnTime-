CREATE TABLE aeropuertos_zonas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo_iata VARCHAR(3) NOT NULL UNIQUE,
    zona_horaria VARCHAR(50) NOT NULL
);
INSERT INTO aeropuertos_zonas (codigo_iata, zona_horaria) VALUES
('JFK','America/New_York'),
('LAX','America/Los_Angeles'),
('ORD','America/Chicago'),
('DFW','America/Chicago'),
('DEN','America/Denver'),
('PHX','America/Phoenix'),
('MIA','America/New_York'),
('SEA','America/Los_Angeles'),
('ATL','America/New_York'),
('SFO','America/Los_Angeles'),
('LAS','America/Los_Angeles'),
('MCO','America/New_York'),
('CLT','America/New_York'),
('MSP','America/Chicago'),
('DTW','America/New_York'),
('BOS','America/New_York'),
('PHL','America/New_York'),
('IAH','America/Chicago'),
('EWR','America/New_York'),
('TPA','America/New_York');

