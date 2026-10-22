package com.app.financas.dao;

import com.app.financas.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    public void salvar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO  usuarios (nome, email, senha_hash)  VALUES (?, ?, ?)";
        try (Connection conn = Conexao.conectar();
             PreparedStatement comando = conn.prepareStatement(sql);
        ) {
            comando.setString(1, usuario.getNome());
            comando.setString(2, usuario.getEmail());
            comando.setString(3, usuario.getSenhaHash());
            comando.executeUpdate();
        }
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id, nome, email, senha_hash FROM usuarios WHERE email = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement comando = conn.prepareStatement(sql)) {

            comando.setString(1, email);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    Usuario usuario = new Usuario();
                    usuario.setId(resultado.getInt("id"));
                    usuario.setNome(resultado.getString("nome"));
                    usuario.setEmail(resultado.getString("email"));
                    usuario.setSenhaHash(resultado.getString("senha_hash"));
                    return usuario;
                }
            }
        }
        return null;   // não achou ninguém com esse e-mail
    }
}
