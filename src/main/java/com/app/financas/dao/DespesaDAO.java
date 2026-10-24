package com.app.financas.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.app.financas.model.Despesa;

public class DespesaDAO {

    public void salvar(Despesa despesa, int usuarioId) throws SQLException {
        String sql = "INSERT INTO despesas (valor, categoria, descricao, data, usuario_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setBigDecimal(1, despesa.getValor());
            comando.setString(2, despesa.getCategoria());
            comando.setString(3, despesa.getDescricao());
            comando.setDate(4, java.sql.Date.valueOf(despesa.getData()));
            comando.setInt(5, usuarioId);
            comando.executeUpdate();
        }
    }

    public ArrayList<Despesa> listarPorPeriodo(LocalDate inicio, LocalDate fim, int usuarioId) throws SQLException {
        String sql = "SELECT id, valor, categoria, descricao, data FROM despesas "
                + "WHERE usuario_id = ? AND data BETWEEN ? AND ? ORDER BY data, id";
        ArrayList<Despesa> despesas = new ArrayList<>();

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, usuarioId);
            comando.setDate(2, java.sql.Date.valueOf(inicio));
            comando.setDate(3, java.sql.Date.valueOf(fim));
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    Despesa despesa = new Despesa();
                    despesa.setId(resultado.getInt("id"));
                    despesa.setValor(resultado.getBigDecimal("valor"));
                    despesa.setCategoria(resultado.getString("categoria"));
                    despesa.setDescricao(resultado.getString("descricao"));
                    despesa.setData(resultado.getDate("data").toLocalDate());
                    despesas.add(despesa);
                }
            }
        }
        return despesas;
    }

    public Map<String, Double> totalPorCategoria(int usuarioId, LocalDate inicio, LocalDate fim)
            throws SQLException {
        String sql = "SELECT categoria, SUM(valor) FROM despesas "
                + "WHERE usuario_id = ? AND data BETWEEN ? AND ? "
                + "GROUP BY categoria ORDER BY SUM(valor) DESC, categoria";
        Map<String, Double> totais = new LinkedHashMap<>();

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, usuarioId);
            comando.setDate(2, java.sql.Date.valueOf(inicio));
            comando.setDate(3, java.sql.Date.valueOf(fim));
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    totais.put(resultado.getString("categoria"), resultado.getDouble(2));
                }
            }
        }
        return totais;
    }

    public boolean atualizar(Despesa despesa, int usuarioId) throws SQLException {
        String sql = "UPDATE despesas SET valor = ?, categoria = ?, descricao = ?, data = ? "
                + "WHERE id = ? AND usuario_id = ?";

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setBigDecimal(1, despesa.getValor());
            comando.setString(2, despesa.getCategoria());
            comando.setString(3, despesa.getDescricao());
            comando.setDate(4, java.sql.Date.valueOf(despesa.getData()));
            comando.setInt(5, despesa.getId());
            comando.setInt(6, usuarioId);
            return comando.executeUpdate() > 0;
        }
    }

    public boolean deletar(int id, int usuarioId) throws SQLException {
        String sql = "DELETE FROM despesas WHERE id = ? AND usuario_id = ?";

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.setInt(2, usuarioId);
            return comando.executeUpdate() > 0;
        }
    }

}
