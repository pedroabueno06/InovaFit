package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class BancoTest {
    public record Banco (String nome, String url, String usuario, String senha, boolean oracle) {}


    public static List<Banco> bancos() {
        return List.of (
        new Banco("PostgreSQL", env("PG_URL", "jdbc:postgresql://localhost:5432/inovafit"),
            env("PG_USER", "inovafit"), env("PG_PASSWORD", "inovafit_lab"), false),
        new Banco("Oracle", env("ORA_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1"),
            env("ORA_USER", "inovafit"), env("ORA_PASSWORD_APP", "inovafit_lab"), true));
    }

    private static String env(String chave, String padrao) {
        String valor = System.getenv(chave);
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private static Connection connection(Banco banco) throws SQLException {
        Connection connection = DriverManager.getConnection(banco.url(), banco.usuario(), banco.senha());
        connection.setAutoCommit(false);
        return connection;
    }

     @FunctionalInterface
    public interface Verificacao {
        void executar(Banco banco, Connection connection) throws Exception;
    }

    public static void testarEmAmbosOsBancosDeDados(Verificacao verificacao) throws Exception {
        for (Banco banco : bancos()) {
            try (Connection connection = connection(banco)) {
                try {
                    verificacao.executar(banco, connection);
                } finally {
                    connection.rollback();
                }
            }
        }
    }
}
