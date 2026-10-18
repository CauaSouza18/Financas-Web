package com.app.financas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static final String URL = "jdbc:mysql://localhost:3306/app_financas";
    private static final String user = "root";
    private static final String senha = "27870015";

    private static Connection conexao;
    public static Connection conectar() throws SQLException {
        if (conexao == null || conexao.isClosed()) {
            conexao = DriverManager.getConnection(URL, user, senha);
        }
        else if (!conexao.isValid(5)) {
            conexao.close();
            conexao = DriverManager.getConnection(URL, user, senha);
        }
        return conexao;
    }
}

