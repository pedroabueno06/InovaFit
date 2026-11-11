package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import org.junit.jupiter.api.Test;

//Testes de integração do projeto InovaFit executados contra os bancos de dados PostegreSQL e Oracle.
public class InovafitTest {
    private record Banco(String nome, String url, String usuario, String senha, boolean oracle) {}

    private List<Banco> bancos() {
        return List.of (
        new Banco("PostgreSQL", env("PG_URL", "jdbc:postgresql://localhost:5432/inovafit"),
            env("PG_USER", "inovafit"), env("PG_PASSWORD", "inovafit_lab"), false),
        new Banco("Oracle", env("ORA_URL", "jdbc:oracle:thin:@//localhost:1521/XEPDB1"),
            env("ORA_USER", "inovafit"), env("ORA_PASSWORD_APP", "inovafit_lab"), true));
    }

    private static String env(String key, String fallback) {
        String valor = System.getenv(key);
        return valor == null || valor.isBlank() ? fallback : valor;
    }

    private Connection connection(Banco banco) throws SQLException {
        Connection connection = DriverManager.getConnection(banco.url(), banco.usuario(), banco.senha());
            connection.setAutoCommit(false);
                return connection;
    }

    private void testarEmAmbosOsBancosDeDados(Verificacao verificacao) throws Exception {
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

    @FunctionalInterface
    private interface Verificacao { void executar(Banco banco, Connection connection) throws Exception; }

    //1- Testa a conexão com os dois bancos de dados
    @Test
    void conexoesComOsDoisBancosDeDados() throws Exception {
        for(Banco banco : bancos()) {
            try (Connection connection = connection(banco)) {
                assertTrue(connection.isValid(5), "Conexão inválida: " + banco.nome);
            }
        }
    }

    //2- Testa se a classe Inovafit.java inicia e encerra pelo seu menu()
    @Test
    void InovafitIniciaEEncerraPeloMenu() {
        InputStream entradaOriginal = System.in;
        PrintStream saidaOriginal = System.out;
        ByteArrayOutputStream saida = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream("0\n".getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(saida, true, StandardCharsets.UTF_8));
            Inovafit.main(new String[0]);
            assertTrue(saida.toString(StandardCharsets.UTF_8).contains("Sistema encerrado."));
        } finally {
            System.setIn(entradaOriginal);
            System.setOut(saidaOriginal);
        }
    }

    //3- Testa se o projeto possui uma estrutura que tenha dados de carga
    @Test
    void estuturaPossuiDadosDeCarga() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            assertTrue(contar(c, "aluno") >= 4, b.nome() + ": alunos");
            assertTrue(contar(c, "plano") >= 3, b.nome() + ": planos");
            assertTrue(contar(c, "modalidade") >= 4, b.nome() + ": modalidades");
            assertTrue(contar(c, "matricula") >= 4, b.nome() + ": matrículas");
            assertTrue(contar(c, "matricula_modalidade") >= 5, b.nome() + ": associações");
            assertTrue(contar(c, "pagamento") >= 3, b.nome() + ": pagamentos");
        });
    }

    //4- Testa se o sistema rejeita um email que já esta sendo utilizado
    @Test
    void rejeitaEmailDuplicado() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            String sql = "INSERT INTO aluno(nome,email,data_nascimento) VALUES(?,?,?)";
            try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                preparedStatement.setString(1, "Aluno teste");
                preparedStatement.setString(2, "pedro.ascenco@gmail.com");
                preparedStatement.setDate(3, Date.valueOf("2000-01-01"));
                assertThrows(SQLException.class, preparedStatement::executeUpdate, b.nome());
            }
        });
    }

    //5- Testa se o sistema rejeita um valor de plano Inválido
    @Test
    void rejeitaValorDePlanoInvalido() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            String sql = "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('Plano teste',-1,1)";
            try (Statement statement = c.createStatement()) {
                assertThrows(SQLException.class, () -> statement.executeUpdate(sql), b.nome());
            }
        });
    }

    //6- Testa as consultas do projeto em ambos os bancos de dados
    @Test
    void consultasDoProjetoRodamNosDoisBancosDeDados() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            List<String> consultas = List.of(
                "SELECT a.nome,p.nome plano,m.status FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno JOIN plano p ON p.id_plano=m.id_plano WHERE m.status='ATIVA'",
                "SELECT a.nome,mo.nome modalidade FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno JOIN matricula_modalidade mm ON mm.id_matricula=m.id_matricula JOIN modalidade mo ON mo.id_modalidade=mm.id_modalidade",
                "SELECT p.nome,COUNT(m.id_matricula) total FROM plano p LEFT JOIN matricula m ON m.id_plano=p.id_plano GROUP BY p.nome",
                "SELECT p.nome,COUNT(*) total FROM plano p JOIN matricula m ON m.id_plano=p.id_plano GROUP BY p.nome HAVING COUNT(*)>=1",
                "SELECT a.nome,COUNT(f.id_frequencia) presencas,RANK() OVER(ORDER BY COUNT(f.id_frequencia) DESC) posicao FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno LEFT JOIN frequencia f ON f.id_matricula=m.id_matricula GROUP BY a.nome",
                "SELECT id_matricula,competencia,valor,SUM(valor) OVER(PARTITION BY id_matricula ORDER BY competencia) acumulado FROM pagamento",
                "SELECT a.id_aluno,a.nome FROM aluno a WHERE NOT EXISTS(SELECT 1 FROM matricula m WHERE m.id_aluno=a.id_aluno AND m.status='ATIVA')",
                "SELECT id_pagamento,id_matricula,valor FROM pagamento WHERE valor>(SELECT AVG(valor) FROM pagamento)",
                b.oracle()
                    ? "SELECT DISTINCT a.nome,p.vencimento,p.valor FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno JOIN pagamento p ON p.id_matricula=m.id_matricula WHERE p.status<>'PAGO' AND p.vencimento<TRUNC(SYSDATE)-10"
                    : "SELECT DISTINCT a.nome,p.vencimento,p.valor FROM aluno a JOIN matricula m ON m.id_aluno=a.id_aluno JOIN pagamento p ON p.id_matricula=m.id_matricula WHERE p.status<>'PAGO' AND p.vencimento<CURRENT_DATE-10",
                "SELECT competencia,SUM(valor) receita FROM pagamento WHERE status='PAGO' GROUP BY competencia ORDER BY competencia"
            );
            assertEquals(10, consultas.size());
            for (int i = 0; i < consultas.size(); i++) {
                try (Statement statement = c.createStatement(); ResultSet rs = statement.executeQuery(consultas.get(i))) {
                    assertNotNull(rs.getMetaData(), b.nome() + " consulta " + (i + 1));
                }
            }
        });
    }

    //7- Testa se a procedure retorna o status dos pagamentos
    @Test
    void procedureRetornaStatusDosPagamentos() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            assertEquals("PAGO", consultarStatusPagamento(b, c, 1));
            assertEquals("PENDENTE", consultarStatusPagamento(b, c, 2));
            assertEquals("ATRASADO", consultarStatusPagamento(b, c, 3));
        });
    }
    
    private String consultarStatusPagamento(Banco b, Connection c, long id) throws SQLException {
        if (b.oracle()) {
            try (CallableStatement callableStatement = c.prepareCall("{call registrar_pagamento(?, ?)}")) {
                callableStatement.setLong(1, id);
                callableStatement.registerOutParameter(2, Types.VARCHAR);
                callableStatement.execute();
                return callableStatement.getString(2);
            }
        }
        try (PreparedStatement preparedStatement = c.prepareStatement("CALL registrar_pagamento(?, NULL)")) {
            preparedStatement.setLong(1, id);
            boolean temResultado = preparedStatement.execute();
            assertTrue(temResultado, "PostgreSQL deveria retornar o parâmetro OUT como resultado");
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    //8- Testa se a procedure retorna erro de negócio em pagamento inexistente
    @Test
    void procedureRetornaErroDeNegocioEmPagamentoInexistente() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            SQLException erro;
            if (b.oracle()) {
                try (CallableStatement callableStatement = c.prepareCall("{call registrar_pagamento(?, ?)}")) {
                    callableStatement.setLong(1, 999999);
                    callableStatement.registerOutParameter(2, Types.VARCHAR);
                    erro = assertThrows(SQLException.class, callableStatement::execute);
                }
            } else {
                try (PreparedStatement preparedStatement = c.prepareStatement("CALL registrar_pagamento(?, NULL)")) {
                    preparedStatement.setLong(1, 999999);
                    erro = assertThrows(SQLException.class, preparedStatement::execute);
                }
            }
            assertTrue(erro.getMessage().contains("PAGAMENTO_NAO_ENCONTRADO"), b.nome());
        });
    }

    //9- Testa se a auditoria de dados funciona ao cancelar a matrícula, assim atualizando os dados
    @Test
    void cancelarMatriculaAtualizaDadosEGeraAuditorio() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            long auditoriasAntes;
            try (PreparedStatement preparedStatement = c.prepareStatement("SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula=1 AND status_novo='CANCELADA'"); ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next(); auditoriasAntes = resultSet.getLong(1);
            }
            try (PreparedStatement preparedStatement = c.prepareStatement("CALL cancelar_matricula(?)")) {
                preparedStatement.setLong(1, 1);
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = c.prepareStatement("SELECT status,bloqueada,data_fim FROM matricula WHERE id_matricula=1"); ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("CANCELADA", resultSet.getString("status"));
                if (b.oracle()) assertEquals(1, resultSet.getInt("bloqueada"));
                else assertTrue(resultSet.getBoolean("bloqueada"));
                assertNotNull(resultSet.getDate("data_fim"));
            }
            try (PreparedStatement preparedStatement = c.prepareStatement("SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula=1 AND status_novo='CANCELADA'"); ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next(); assertEquals(auditoriasAntes + 1, resultSet.getLong(1));
            }
        });
    }

    //10- Testa se ao tentar cancelar uma matrícula inativa é retornada uma mensagem de erro
    @Test
    void cancelarMatriculaInativaRetornaErro() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            SQLException erro;
            try (PreparedStatement preparedStatement = c.prepareStatement("CALL cancelar_matricula(?)")) {
                preparedStatement.setLong(1, 4); // matrícula já cancelada na carga inicial
                erro = assertThrows(SQLException.class, preparedStatement::execute);
            }
            assertTrue(erro.getMessage().contains("MATRICULA_ATIVA_NAO_ENCONTRADA"), b.nome());
        });
    }

    //11- Testa se o trigger permite emitir frequência para uma matrícula ativa
    @Test
    void triggerPermiteFrequenciaParaMatriculaAtiva() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            try (PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(1)")) {
                assertEquals(1, preparedStatement.executeUpdate());
            }
        });
    }

    //12-Testa se o trigger permite emitir frequência para uma matrícula cancelada
    @Test
    void triggerRejeitaFrequenciaParaMatriculaCancelada() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            try (PreparedStatement preparedStatement = c.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(4)")) {
                assertThrows(SQLException.class, preparedStatement::executeUpdate, b.nome());
            }
        });
    }

    //13- Testa se o trigger do banco de dados Oracle rejeita uma matrícula com um atraso superior de dez dias
    @Test
    void triggerOracleRejeitaMatriculaComAtrasoSuperiorADezDias() throws Exception {
        for (Banco banco : bancos()) {
            if (!banco.oracle()) continue; // a versão PostgreSQL atual não implementa essa regra no trigger
            try (Connection connection = connection(banco)) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(3)")) {
                    SQLException erro = assertThrows(SQLException.class, preparedStatement::executeUpdate);
                    assertTrue(erro.getMessage().contains("ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS"));
                } finally { connection.rollback(); }
            }
        }
    }

    //14- Testa se os índices do projeto estão criados
    @Test
    void indicesDoProjetoEstaoCriados() throws Exception {
        testarEmAmbosOsBancosDeDados((b, c) -> {
            List<String> esperados = List.of("IX_MATRICULA_ID_PLANO", "IX_PAGAMENTO_ID_MATRICULA",
                "IX_FREQUENCIA_ID_MATRICULA_DATA", "IX_PAGAMENTO_VENCIMENTO_STATUS");
            for (String nome : esperados) {
                String sql = b.oracle()
                    ? "SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME=?"
                    : "SELECT COUNT(*) FROM pg_indexes WHERE schemaname=current_schema() AND upper(indexname)=?";
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    preparedStatement.setString(1, nome);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        assertTrue(resultSet.next());
                        assertEquals(1, resultSet.getInt(1), b.nome() + " índice " + nome);
                    }
                }
            }
        });
    }

    private long contar(Connection c, String tabela) throws SQLException {
        try (Statement statement = c.createStatement(); ResultSet resulSet = statement.executeQuery("SELECT COUNT(*) FROM " + tabela)) {
            assertTrue(resulSet.next());
            return resulSet.getLong(1);
        }
    }
}
