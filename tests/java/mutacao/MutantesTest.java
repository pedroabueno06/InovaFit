package mutacao;
//Testes de mutação
//Já possuímos 19 testes de integração, abaixo estaram os
//Ao todo temos 5 testes de mutação.

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

import com.example.BancoTest;

public class MutantesTest {
    
    //Métodos auxiliares
    //Data na sintaxe de cada banco de dados.
    private static String hoje(BancoTest.Banco banco) {
        return banco.oracle() ? "TRUNC(SYSDATE)" : "CURRENT_DATE";
    }

    private static int executarEContar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            return statement.executeUpdate(sql);
        }
    }

    private static void executar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private static long contar(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement();
            ResultSet resultado = statement.executeQuery(sql)) {
            assertTrue(resultado.next());
            return resultado.getLong(1);
        }
    }

    //Comando deve falhar;
    private static void rejeita(Connection connection, String sql, String mensagem) throws SQLException {
        Savepoint savepoint = connection.setSavepoint();

            try {
                assertThrows(SQLException.class, () -> executar(connection, sql), mensagem);

            } finally {
                //Desfaz somente a operação que falhou.
                connection.rollback(savepoint);
            }
    }

    private static void rejeitaComMensagem(Connection connection, String sql, String mensagem) throws SQLException {
        Savepoint savepoint = connection.setSavepoint();

            try {
                SQLException erro = assertThrows(SQLException.class, () -> executar(connection, sql));
                    assertTrue(erro.getMessage().contains(mensagem), ": mensagem recebeida = " + erro.getMessage());
                    
            } finally {
                //Desfaz somente a operação que falhou.
                connection.rollback(savepoint);
            }
    }

    //Comando deve funcionar e ser desfeito.
    private static void aceita(Connection connection, String sql, String mensagem) throws SQLException {
        assertEquals(1, executarEContar(connection, sql), mensagem);
    }

    //Mutante 1: Aluno deve estar com a matrícula ativa e desbloqueada.
    @Test
    void Mutante1_matriculaInativaOuBloqueada() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            
            //Aluno que possua sua matrícula cancelada
            // não pode registrar frequência.
            rejeitaComMensagem(connection, "INSERT INTO frequencia(id_matricula) VALUES(4)", "MATRICULA_INATIVA_OU_BLOQUEADA");

            //Aluno que possua sua matrícula ativa, mas bloqueada,
            // também não pode realizar registro de frequência.
            String bloqueada = banco.oracle() ? "1" : "TRUE";

            executar(connection, "UPDATE matricula SET bloqueada = " + bloqueada + " WHERE id_matricula = 2");
                rejeitaComMensagem(connection, "INSERT INTO frequencia(id_matricula) VALUES(2)", "MATRICULA_INATIVA_OU_BLOQUEADA");
        }));
    }

    //Mutante 2: Aluno possui um limite de ate 10 dias
    // de atraso para estar em dia com sua matrícula,
    // para que assim consigam adentrar na academia.
    @Test
    void Mutante2_limiteDeDezDiasDeAtraso() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            //Aluno que está com sua mensalidade
            // em 10 dias de atraso, ainda entra na academia.
            executar(connection, "UPDATE pagamento SET vencimento = " + hoje(banco) + " - 10 WHERE id_pagamento = 2");
                aceita(connection, "INSERT INTO frequencia(id_matricula) VALUES(2)", banco.nome() + ": 10 dias devem ser permitidos");

            //Alunos que estiverem com sua mensalidade
            // em 11 dias de atraso, não entra na academia.
            executar(connection, "UPDATE pagamento SET vencimento = " + hoje(banco) + " - 11 WHERE id_pagamento = 2");
                rejeitaComMensagem(connection, "INSERT INTO frequencia(id_matricula) VALUES(2)", "ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS");
        }));
    }

    //Mutante 3: Um aluno não pode possui mais
    // de uma matrícula ativa.
    @Test
    void Mutante3_UmaMatriculaAtivaPorAluno() throws  Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            //Aluno 4: Fabrício, possui sua matrícula cancelada,
            // com isso, pode criar outra matrícula e torna-la ativa.
            aceita(connection, "INSERT INTO matricula" + "(id_aluno,id_plano,data_inicio,status) " + "VALUES(4,1," + hoje(banco) + ",'ATIVA')", ": aluno 4 pode criar outra matrícula");

            //Aluno 1: Pedro Bueno, já possuio uma matrícula ativa,
            // com isso, ele não poderá criar outra.
            rejeita(connection, "INSERT INTO matricula" + "(id_aluno,id_plano,data_inicio,status) " + "VALUES(1,1," + hoje(banco) + ",'ATIVA')", banco.nome() + ": aluno 1 não pode criar outra matrícula ativa");
        }));
    }

    //Mutante 4: O valor mensal do plano deve ser maior que zero.
    @Test
    void Mutante4_valorMensalDeveSerMaiorQueZero() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            rejeita(connection, "INSERT INTO plano(nome,valor_mensal,duracao_meses) " + "VALUES('PlanoMutacao',0,1)", banco.nome() + ": plano não pode possuir valor zero");
        });
    }

    //Mutante 5: A auditoria de matrículas só deve registrar
    // mudanças reais de status na matrícula dos alunos.
    @Test
    void Mutante5_auditoriaSomenteQuandoStatusMudar() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {

            // Realiza a atualização do status para o mesmo valor.
            executar(connection, "UPDATE matricula SET status = status " + "WHERE id_matricula = 2"
            );

            // Nenhuma auditoria deve ser criada.
            assertEquals(0, contar( connection, "SELECT COUNT(*) FROM auditoria_matricula " + "WHERE id_matricula = 2"), banco.nome() + ": não deve auditar uma alteração inexistente");

            // Agora realiza uma mudança real.
            executar(connection, "UPDATE matricula SET status = 'ENCERRADA' " + "WHERE id_matricula = 2");

            assertEquals(1, contar(connection, "SELECT COUNT(*) FROM auditoria_matricula " + "WHERE id_matricula = 2 " + "AND status_anterior = 'ATIVA' " + "AND status_novo = 'ENCERRADA'"), banco.nome() + ": deve registrar a mudança real");
        });
    }
}

