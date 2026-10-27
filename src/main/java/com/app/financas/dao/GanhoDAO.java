package com.app.financas.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import com.app.financas.model.Ganho;

public class GanhoDAO {

    public ArrayList<Ganho> listGanhos(int usuarioId) throws SQLException {
        String sql = "SELECT id, valor, plataforma, Cadastrado_Em AS data FROM ganhos "
                + "WHERE usuario_id = ? ORDER BY Cadastrado_Em, id";
        ArrayList<Ganho> ganhos = new ArrayList<>();

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, usuarioId);
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    int id = resultado.getInt("id");
                    BigDecimal valor = resultado.getBigDecimal("valor");
                    String plataforma = resultado.getString("plataforma");
                    LocalDate data = resultado.getDate("data").toLocalDate();

                    Ganho ganho = new Ganho(id, valor, plataforma, data);
                    ganhos.add(ganho);
                }
            }
        }
        return ganhos;
    }

    public ArrayList<Ganho> listarPorPeriodo(LocalDate inicio, LocalDate fim, int usuarioId) throws SQLException {
        String sql = "SELECT id, valor, plataforma, Cadastrado_Em AS data FROM ganhos "
                + "WHERE usuario_id = ? AND Cadastrado_Em BETWEEN ? AND ? ORDER BY Cadastrado_Em, id";
        ArrayList<Ganho> ganhos = new ArrayList<>();

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, usuarioId);
            comando.setDate(2, java.sql.Date.valueOf(inicio));
            comando.setDate(3, java.sql.Date.valueOf(fim));
            try (var resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    ganhos.add(new Ganho(
                            resultado.getInt("id"),
                            resultado.getBigDecimal("valor"),
                            resultado.getString("plataforma"),
                            resultado.getDate("data").toLocalDate()
                    ));
                }
            }
        }
        return ganhos;
    }

    public boolean atualizar(Ganho ganho, int usuarioId) throws SQLException {
        String sql = "UPDATE ganhos set valor = ?, plataforma = ?, Cadastrado_Em = ? "
                + "WHERE id = ? AND usuario_id = ?";
        int linhasAfetadas;
        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setBigDecimal(1, ganho.getValor());
            comando.setString(2, ganho.getPlataforma());
            comando.setObject(3, ganho.getData());
            comando.setInt(4, ganho.getId());
            comando.setInt(5, usuarioId);
            linhasAfetadas = comando.executeUpdate();

        }
        return linhasAfetadas > 0;
    }

    public boolean deletar(int id, int usuarioId) throws SQLException {
        String sql = "DELETE from ganhos WHERE id = ? AND usuario_id = ?";
        int linhasAfetadas;
        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, id);
            comando.setInt(2, usuarioId);
            linhasAfetadas = comando.executeUpdate();

        }
        return linhasAfetadas > 0;

    }

    public void salvar(Ganho ganho, int usuarioId) throws SQLException {
        String sql = "INSERT INTO ganhos (valor, plataforma, Cadastrado_Em, usuario_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setBigDecimal(1, ganho.getValor());
            comando.setString(2, ganho.getPlataforma());
            comando.setObject(3, ganho.getData());
            comando.setInt(4, usuarioId);
            comando.executeUpdate();
        }
    }
    public double totalPorPlataforma(String plataforma, int usuarioId) throws SQLException {
        String sql = "SELECT SUM(valor) FROM ganhos WHERE plataforma = ? AND usuario_id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setString(1, plataforma);
            comando.setInt(2, usuarioId);

            try (ResultSet resultado = comando.executeQuery()) {
                if (resultado.next()) {
                    return resultado.getDouble(1);
                }
            }
        }

        return 0.0;
    }

    public Map<String, Double> totaisPorPlataforma(int usuarioId, LocalDate inicio, LocalDate fim)
            throws SQLException {
        String sql = "SELECT plataforma, SUM(valor) FROM ganhos "
                + "WHERE usuario_id = ? AND cadastrado_em BETWEEN ? AND ? "
                + "GROUP BY plataforma ORDER BY SUM(valor) DESC, plataforma";
        Map<String, Double> totais = new LinkedHashMap<>();

        try (Connection conn = Conexao.conectar(); PreparedStatement comando = conn.prepareStatement(sql)) {
            comando.setInt(1, usuarioId);
            comando.setDate(2, java.sql.Date.valueOf(inicio));
            comando.setDate(3, java.sql.Date.valueOf(fim));
            try (ResultSet resultado = comando.executeQuery()) {
                while (resultado.next()) {
                    totais.put(resultado.getString("plataforma"), resultado.getDouble(2));
                }
            }
        }
        return totais;
    }
}
