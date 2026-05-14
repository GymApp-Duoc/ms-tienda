CREATE TABLE productos (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           nombre VARCHAR(100) NOT NULL UNIQUE,
                           descripcion TEXT,
                           precio DECIMAL(10,2) NOT NULL,
                           stock INT NOT NULL DEFAULT 0,
                           categoria VARCHAR(50)
);

CREATE TABLE ventas (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        producto_id BIGINT NOT NULL,
                        miembro_id BIGINT NOT NULL,
                        cantidad INT NOT NULL,
                        total DECIMAL(10,2) NOT NULL,
                        fecha_venta TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (producto_id) REFERENCES productos(id)
);