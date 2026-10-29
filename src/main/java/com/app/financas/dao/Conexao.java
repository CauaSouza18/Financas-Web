package com.app.financas.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static Connection conexao;
    public static Connection conectar() throws SQLException {
        String url = System.getenv("DB_URL");
        if (url == null) {
            url = "jdbc:mysql://HOST:25947/app_financas?sslMode=REQUIRED";

        }
        String user = System.getenv("DB_USER");
        String senha = System.getenv("DB_PASSWORD");

        if (conexao == null || conexao.isClosed()) {
            conexao = DriverManager.getConnection(url, user, senha);
        }
        else if (!conexao.isValid(5)) {
            conexao.close();
            conexao = DriverManager.getConnection(url, user, senha);
        }
        return conexao;
    }
}

