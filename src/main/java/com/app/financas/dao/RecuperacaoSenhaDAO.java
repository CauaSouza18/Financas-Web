package com.app.financas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class RecuperacaoSenhaDAO {
    private void garantirTabela(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS recuperacao_senha_tokens ("
                + "token_hash CHAR(64) NOT NULL PRIMARY KEY, "
                + "usuario_id INT NOT NULL, "
                + "expira_em DATETIME NOT NULL, "
                + "criado_em DATETIME NOT NULL, "
                + "INDEX idx_recuperacao_usuario_criado (usuario_id, criado_em), "
                + "CONSTRAINT fk_recuperacao_usuario FOREIGN KEY (usuario_id) "
                + "REFERENCES usuarios(id) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement statement = conn.createStatement()) {
            statement.execute(sql);
        }
    }

    public boolean podeSolicitar(int usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM recuperacao_senha_tokens "
                + "WHERE usuario_id = ? AND criado_em >= DATE_SUB(UTC_TIMESTAMP(), INTERVAL 1 MINUTE)";
        try (Connection conn = Conexao.conectar()) {
            garantirTabela(conn);
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, usuarioId);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next() && result.getInt(1) == 0;
                }
            }
        }
    }

    public void salvarToken(int usuarioId, String tokenHash, LocalDateTime expiraEm) throws SQLException {
        String limparExpirados = "DELETE FROM recuperacao_senha_tokens WHERE expira_em <= UTC_TIMESTAMP()";
        String limparUsuario = "DELETE FROM recuperacao_senha_tokens WHERE usuario_id = ?";
        String inserir = "INSERT INTO recuperacao_senha_tokens (usuario_id, token_hash, expira_em, criado_em) "
                + "VALUES (?, ?, ?, UTC_TIMESTAMP())";
        try (Connection conn = Conexao.conectar()) {
            garantirTabela(conn);
            try (Statement cleanup = conn.createStatement()) {
                cleanup.executeUpdate(limparExpirados);
            }
            try (PreparedStatement cleanup = conn.prepareStatement(limparUsuario)) {
                cleanup.setInt(1, usuarioId);
                cleanup.executeUpdate();
            }
            try (PreparedStatement statement = conn.prepareStatement(inserir)) {
                statement.setInt(1, usuarioId);
                statement.setString(2, tokenHash);
                statement.setObject(3, expiraEm);
                statement.executeUpdate();
            }
        }
    }

    public boolean tokenValido(String tokenHash) throws SQLException {
        String sql = "SELECT 1 FROM recuperacao_senha_tokens "
                + "WHERE token_hash = ? AND expira_em > UTC_TIMESTAMP()";
        try (Connection conn = Conexao.conectar()) {
            garantirTabela(conn);
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, tokenHash);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
    }

    public boolean redefinirSenha(String tokenHash, String senhaHash) throws SQLException {
        String atualizar = "UPDATE usuarios u "
                + "INNER JOIN recuperacao_senha_tokens r ON r.usuario_id = u.id "
                + "SET u.senha_hash = ?, r.expira_em = UTC_TIMESTAMP() "
                + "WHERE r.token_hash = ? AND r.expira_em > UTC_TIMESTAMP()";
        String consumir = "DELETE FROM recuperacao_senha_tokens WHERE token_hash = ?";
        try (Connection conn = Conexao.conectar()) {
            garantirTabela(conn);
            int atualizados;
            try (PreparedStatement statement = conn.prepareStatement(atualizar)) {
                statement.setString(1, senhaHash);
                statement.setString(2, tokenHash);
                atualizados = statement.executeUpdate();
            }
            if (atualizados == 0) {
                return false;
            }
            try (PreparedStatement statement = conn.prepareStatement(consumir)) {
                statement.setString(1, tokenHash);
                statement.executeUpdate();
            }
            return true;
        }
    }

    public void expirarToken(String tokenHash) throws SQLException {
        String sql = "UPDATE recuperacao_senha_tokens SET expira_em = UTC_TIMESTAMP() WHERE token_hash = ?";
        try (Connection conn = Conexao.conectar()) {
            garantirTabela(conn);
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setString(1, tokenHash);
                statement.executeUpdate();
            }
        }
    }
}
