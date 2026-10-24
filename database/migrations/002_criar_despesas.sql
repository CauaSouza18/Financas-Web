CREATE TABLE IF NOT EXISTS despesas (
    id INT NOT NULL AUTO_INCREMENT,
    valor DECIMAL(10, 2) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    descricao VARCHAR(255) NOT NULL DEFAULT '',
    data DATE NOT NULL,
    usuario_id INT NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_despesas_usuario_data (usuario_id, data, id)
);
