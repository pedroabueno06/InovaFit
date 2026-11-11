package com.example;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Scanner;

public class Inovafit {
    public static void main(String[] args) {
        String url;
        String usuario;
        String senha;
        String nomeBanco;

        String pgUrl = env("PG_URL", "jdbc:postgresql://localhost:5432/inovafit");
        String pgUser = env("PG_USER", "inovafit");
        String PgPassword = env("PG_PASSWORD", "inovafit_lab");

        String oraUrl = env("ORA_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1");
        String oraUser = env("ORA_USER", "inovafit");
        String oraPassword = env("ORA_PASSWORD_APP", "inovafit_lab");

        
        try (Scanner scanner = new Scanner(System.in)){

            while (true) {
                System.out.println("\n=== InovaFit ===");
                System.out.println("Escolha entre os dois bancos de dados:");
                System.out.println("1- PostgreSQL;");
                System.out.println("2- Oracle;");
                System.out.print("0- Encerrar.");
                System.out.println("\nDigite a sua escolha: ");
                int opcao;
                
                try {
                    opcao = Integer.parseInt(scanner.nextLine());

                } catch (NumberFormatException e) {
                    System.out.println("Digite um número válido");
                    continue;
                }

                if (opcao == 0) {
                    System.out.println("\nSistema encerrado.");
                    break;
                }

                if (opcao == 1) {
                    url = pgUrl;
                    usuario = pgUser;
                    senha = PgPassword;
                    nomeBanco = "PostgreSQL";

                } else if (opcao == 2) {
                    url = oraUrl;
                    usuario = oraUser;
                    senha = oraPassword;
                    nomeBanco = "Oracle";

                } else {
                    System.out.println("Opção inválida.");
                    continue;
                }

                try (Connection connection = DriverManager.getConnection(url, usuario, senha)) {
                    connection.setAutoCommit(false);

                    System.out.println("\nConectado ao " + nomeBanco + " com sucesso!");
                    executarMenu(connection, scanner);
                    
                } catch (SQLException e) {
                    System.err.println("\nErro de conexão com " + nomeBanco + ": " + e.getMessage());
                }
            }
        }
    }

    private static void executarMenu(Connection connection, Scanner scanner) {

        while (true) { 
            System.out.println("\n=== INOVAFIT ===");
            System.out.println("Banco de dados conectado: " + connectionBanco(connection) + ".");
            System.out.println("1- Consultar alunos e situação financeira;");
            System.out.println("2- Consultar status do pagamento pelo ID;");
            System.out.println("0- Voltar à escolha do banco de dados.");
            System.out.println("Digite a sua escolha: ");
                int opcao;

            try {
                opcao = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número válido");
                    continue;
            }

            if (opcao == 0) {
                return;
            }

            try {
                if (opcao == 1 ) {
                    consultar(connection);
                    connection.commit();

                } else if (opcao == 2) {
                    System.out.println("\nID de pagamento: ");
                        long id = Long.parseLong(scanner.nextLine());
                        confirmarPagamento(connection, id);
                        connection.commit();

                } else {
                    System.out.println("Opção inválida.");
                }

            } catch (SQLException e) {
                try {
                    connection.rollback();

                } catch (SQLException erroRolback) {
                    System.err.println("Erro ao desfazer a operação: " + erroRolback.getMessage());

                }
                tratarErro(e);

            } catch (NumberFormatException e) {
                System.out.println("Informe um ID válido.");
            }
        }
    }

    private static void consultar(Connection connection) throws SQLException {
        String sql = "SELECT a.nome, p.nome AS plano, "
                + "m.status, m.bloqueada "
                + "FROM aluno a "
                + "JOIN matricula m ON m.id_aluno = a.id_aluno "
                + "JOIN plano p ON p.id_plano = m.id_plano "
                + "WHERE m.status = ? "
                + "ORDER BY a.nome";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, "ATIVA");

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                System.out.println("\n==================== ALUNOS ====================");
                    boolean encontrou = false;

                while (resultSet.next()) {
                    encontrou = true;
                    System.out.println("--------------------------");
                    System.out.println("Aluno: " + resultSet.getString("nome"));
                    System.out.println("Plano: " + resultSet.getString("plano"));
                    System.out.println("Status: " + resultSet.getString("status"));
                    System.out.println("Bloqueada: " + resultSet.getObject("bloqueada"));
                }
                
                if(!encontrou) {
                    System.out.println("Nenhum aluno com matrícula ativa encontrado");
                }
            }
        }
    }

    private static void confirmarPagamento(Connection connection, long id) throws SQLException {
        String url = connection.getMetaData().getURL();
        String status;

        if(url.startsWith("jdbc:postgresql:")) {
            String sql = "CALL registrar_pagamento(?, NULL)";

            //Aqui é onde a procedure é chamada a partir de um parâmetro;
            //Commit fica sob controle da aplicação.
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setLong(1, id);
                boolean resultado = preparedStatement.execute();

                if(resultado) {
                    try (ResultSet resultSet = preparedStatement.getResultSet()) {
                        if(resultSet.next()) {
                            status = resultSet.getString(1);
                            System.out.println("Status de pagamento: " + status);
                        }
                    }
                }
            }

        } else if (url.startsWith("jdbc:oracle:")) {
            String sql = "{call registrar_pagamento(?, ?)}";

            //Aqui é onde a procedure é chamada a partir de um parâmetro;
            //Commit fica sob controle da aplicação.
            try (CallableStatement callableStatement = connection.prepareCall(sql)) {
                callableStatement.setLong(1, id);
                callableStatement.registerOutParameter(2, Types.VARCHAR);
                callableStatement.execute();

                status = callableStatement.getString(2);
                System.out.println("Status de pagamento: " + status);
            }
        }
    }

    private static void tratarErro(SQLException e) {
        
            String mensagem = e.getMessage() == null
                ?""
                : e.getMessage();
            
                String estado = e.getSQLState();
                    int codigo = e.getErrorCode();

                if(mensagem.contains("PAGAMENTO_NAO_ENCONTRADO") || "P1001".equals(estado) || codigo == 20001) {
                    System.err.println("Pagamento não encontrado.");

                } else if (mensagem.contains("PAGAMENTO_JA_CONFIRMADO") || "P1002".equals(estado) || codigo == 20002) {
                    System.err.println("Pagamento já confirmado.\n");

                } else if (mensagem.contains("PAGAMENTO_PENDENTE") || "P1004".equals(estado) || codigo == 20004) {
                    System.out.println("Pagamento pendente.\n");

                } else if (mensagem.contains("PAGAMENTO_ATRASADO") || "P1005".equals(estado) || codigo == 20005) {
                    System.out.println("Pagamento atrasado.\n");

                } else {
                    System.err.println("Erro de banco de dados: " + mensagem);
                    System.out.println("Código SQL: " + estado);
                    System.out.println("Código do banco: " + codigo);
                }
                
    }

    private static String connectionBanco(Connection connection) {
        try {
            String url = connection.getMetaData().getURL();

            if(url.startsWith("jdbc:oracle:")) {
                return "Oracle";
            }

            return "PostgreSQL";

        } catch (SQLException e) {
            return "Banco de dados conectado";
        }
    }

    private static String env(String key, String fallback) {
        String valor = System.getenv(key);
        return valor == null || valor.isBlank() ? fallback : valor;
    }
}