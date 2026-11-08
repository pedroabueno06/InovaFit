package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

public class Inovafit {
    public static void main(String[] args) {
        String url = env("DB_URL", "jdbc:postgresql://localhost:5432/inovafit");
        String user = env("DB_USER", "inovafit");
        String password = env("DB_PASSWORD", "inovafit_lab");
        
        try (Connection connection = DriverManager.getConnection(url, user, password);
            Scanner scanner = new Scanner(System.in)) {
                connection.setAutoCommit(false);

            while (true) {
                System.out.println("=== InovaFit ===");
                System.out.println("Escolha uma opção:");
                System.out.println("1- Consultar alunos e sua situação financeira");
                System.out.println("2- Confirmar pagamento pelo ID");
                System.out.print("Digite a sua escolha: ");
                int opcao;
                
                try {
                    opcao = Integer.parseInt(scanner.nextLine());

                } catch (NumberFormatException e) {
                    System.out.println("Digite um número válido");
                    continue;
                }

                try {
                    if (opcao == 1) {
                    consultar(connection);
                        connection.commit();

                } else if (opcao == 2) {
                    System.out.print("ID do pagamento: ");
                    long id = Long.parseLong(scanner.nextLine());
                    confirmarPagamento(connection, id);
                        connection.commit();

                } else {
                    System.out.println("Opção inválida. Digite uma opção válida");
                    connection.commit();
                    
                }
            } catch (SQLException e) {
                System.err.println("Erro de banco de dados: " + e.getMessage());
                System.err.println("Código SQL: " + e.getSQLState());

            } catch (NumberFormatException e) {
                System.err.println("Informe um número válido.");
            }
            }
        } catch (SQLException e) {
            System.err.println("Erro de conexão: " + e.getMessage());
        }
    }

    private static void consultar(Connection connection) throws SQLException {
        String sql = "SELECT a.nome, p.nome AS plano, m.status, m.bloqueada "
                + "FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno "
                + "JOIN plano p ON p.id_plano=m.id_plano WHERE m.status=? ORDER BY a.nome";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "Matricula Ativa");
            try (java.sql.ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    System.out.printf("%s | %s | %s | bloqueada=%s%n",
                            resultSet.getString("Aluno: "),
                            resultSet.getString("Plano: "),
                            resultSet.getString("Status: "),
                            resultSet.getObject("Bloqueada: "));
                }
            }
        }
    }

    private static void confirmarPagamento(Connection connection, long id) throws SQLException {
        //Aqui é onde a procedure é chamada a partir de um parâmetro;
        //Commit fica sob controle da aplicação.
        try (java.sql.CallableStatement callableStatement = connection.prepareCall("{call registrar_pagamento(?)}")) {
            callableStatement.setLong(1, id);
            callableStatement.execute();
            System.out.println("Pagamento confirmado!");
            System.out.println("Matrícula desbloqueada.");

        } catch (SQLException e) {
            connection.rollback();
            String msg = e.getMessage() == null ? "" : e.getMessage();
            if (msg.contains("PAGAMENTO_NAO_ENCONTRADO") || msg.contains("20001")) {
                System.err.println("Pagamento não encontrado."); 
            }else if (msg.contains("PAGAMENTO_CONFIRMADO") || msg.contains("20002")) {
                System.err.println("Pagamento já confirmado."); 
            }else {
                System.err.println("Não foi possível confirmar: " + msg);
            }
            throw e;
        }
    }

    private static String env(String key, String fallback) {
        String valor = System.getenv(key);
        return valor == null || valor.isBlank() ? fallback : valor;
    }
}