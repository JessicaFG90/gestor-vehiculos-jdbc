CREATE DATABASE m0495_prg_evaluable06;

USE m0495_prg_evaluable06;

CREATE TABLE Vehiculo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    matricula VARCHAR(20) NOT NULL UNIQUE,
    consumoHomologado DOUBLE
);