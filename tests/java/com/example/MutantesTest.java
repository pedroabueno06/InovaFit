package com.example;
//Testes de mutação
//Já possuímos 19 testes de integração, abaixo estaram os
// testes de mutantes seguindo a ordem dos testes.
//Ao todo temos 10 testes de mutação.

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

public class MutantesTest {
    
    //Métodos auxiliares
    //Data na sintaxe de cada banco de dados.
    private static String hoje(BancoTest.Banco banco) {
        return banco.oracle() ? "TRUNC(SYSDATE)" : "CURRENT_DATE";
    }

    //BOOLEAN no banco de dados PostgreSQL;
    //NUMBER(1) no banco de dados Oracle.
    private static String verdadeiro(BancoTest.Banco banco) {
        return banco.oracle() ? "1" : "TRUE";
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
    private static SQLException rejeita(Connection connection, String rotulo, String sql) throws SQLException {
        Savepoint ponto = connection.setSavepoint();
        try (Statement statement = connection.createStatement()) {
            return assertThrows(SQLException.class, () -> statement.executeUpdate(sql), rotulo);
        } finally {
            connection.rollback(ponto);
        }
    }

    private static void rejeitaComMensagem(Connection connection, String rotulo, String sql, String trecho) throws SQLException {
        SQLException erro = rejeita(connection, rotulo, sql);
        assertTrue(erro.getMessage().contains(trecho), rotulo + ": mensagem recebeida = " + erro.getMessage());
    }

    //Comando deve funcionar e ser desfeito.
    private static void aceita(Connection connection, String rotulo, String sql) throws SQLException {
        Savepoint ponto = connection.setSavepoint();
        try (Statement statement = connection.createStatement()) {
            assertEquals(1, statement.executeUpdate(sql), rotulo);
        } finally {
            connection.rollback(ponto);
        }
    }

    //20- Alunos que possuem entre 7 e 10 dias de atraso na mensalidade
    // conseguem entrar na academia, que passa dos 10 dias não consegue.
    // Mata os mutantes 1,2 e 3.
    @Test
    void frequenciaRespeitaLimiteDeDezDiasDeAtraso() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            //Matrícula 2, possui o pagamento 2, que está pendente e não atrasado.
            for (int dias : new int[] {7, 10}) {
                executar(connection, "UPDATE pagamento SET vencimento = " + hoje(banco) + "-" + dias + " WHERE id_pagamento = 2");
                    aceita(connection, banco.nome() + ": " + dias + " dias de atraso ainda pode entrar na academia", "INSERT INTO frequencia(id_matricula) VALUES(2)");

            }
            executar(connection, "UPDATE pagamento SET vencimento = " + hoje(banco) + " - 11 WHERE id_pagamento = 2");
                rejeitaComMensagem(connection, banco.nome() + ": 11 dias de atraso barra a entrada do aluno na academia", "INSERT INTO frequencia(id_matricula) VALUES(2)", "ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS");
        }));
    }

    //21- Aluno está com sua matrícula ativa, porém a mesma
    // está bloqueada, ficando assim impedido de adentrar na acdemia.
    // Mata o mutante 4.
    @Test
    void frequenciaBarraMatriculaAtivaPoremBloqueada() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            executar(connection, "UPDATE matricula SET bloqueada = " + verdadeiro(banco) + " WHERE id_matricula = 2");
                rejeitaComMensagem(connection, banco.nome() + ": matricula bloqueada", "INSERT INTO frequencia(id_matricula) VALUES(2)", "MATRICULA_INATIVA_OU_BLOQUEADA");
            }));
    }

    //22- Aluno que tiver a matrícula cancelada, tera
    // a mensagem de negócio certa retornada.
    //Mata o mutante 6.
    @Test
    void frequenciaDeMatriculaCanceladaDevolveMensagemDeNegocio() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            rejeitaComMensagem(connection, banco.nome() + ": matricula cancelada", "INSERT INTO frequencia(id_matricula) VALUES(4)", "MATRICULA_INATIVA_OU_BLOQUEADA");
        });
    }

    //23- Nenhum aluno que possuir sua matrícula ativa pode
    // criar outra, apenas alunos com a matrícula bloqueada.
    //Mata os mutantes 21 e 22.
    @Test
    void alunoSoPodeTerUmaMatriculaAtiva() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            aceita(connection, banco.nome() + ": aluno 4 só tem matrícula cancelada, pode ter uma ativa", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,status) VALUES(4,1," + hoje(banco) + ",'ATIVA')");
                rejeita(connection, banco.nome() + ": aluno 1 já tem matrícula ativa", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,status) VALUES(1,1," + hoje(banco) + ",'ATIVA')");
        });
    }

    //24 - Testa se as retrições do banco de dados estão impedindo
    // valores inválidos e permitindo valores válidos nos limites que foram definidos.
    //Mata os mutantes 15,16,17,25,26,27 e 28.
    @Test
    void checksBarramOsValoresNosLimites() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            String h = hoje(banco);
            rejeita(connection, "plano de valor 0", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('P0',0,1)");
                rejeita(connection, "plano de duração 0 meses", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('P0',10,0)");
                    rejeita(connection, "plano de duração 25 meses", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('P25',10,25)");
                        aceita(connection, "plano de duração 24 meses", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('P24',10,24)");
                            aceita(connection, "plano duração 1", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('P1',10,1)");
                                if (banco.oracle()) {
                                    rejeita(connection, "plano ativo 2 (só o banco de dados Oracle tem ck_plano_ativo)", "INSERT INTO plano(nome,valor_mensal,duracao_meses,ativo) VALUES('PX',10,1,2)");
            }

            rejeita(connection, "nome com 2 letras" ,"INSERT INTO aluno(nome,email,data_nascimento) VALUES('Ab','ab@exemplo.com',DATE '2000-01-01')");
                aceita(connection, "nome com 3 letras", "INSERT INTO aluno(nome,email,data_nascimento) VALUES('Abc','abc@exemplo.com',DATE '2000-01-01')");

            rejeita(connection, "matrícula com status inventado", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,status) VALUES(4,1," + h + ",'SUSPENSA')");
                aceita(connection, "data_fim igual a data_inicio","INSERT INTO matricula(id_aluno,id_plano,data_inicio,data_fim,status) VALUES(4,1," + h + "," + h + ",'ENCERRADA')");
                    rejeita(connection, "data_fim antes de data_inicio", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,data_fim,status) VALUES(4,1," + h + "," + h + " - 1,'ENCERRADA')");

            rejeita(connection, "pagamento valor 0", "INSERT INTO pagamento(id_matricula,competencia,vencimento,valor,status) VALUES(2," + h + " - 400," + h + ",0,'PENDENTE')");
                rejeita(connection, "pagamento com status inventado", "INSERT INTO pagamento(id_matricula,competencia,vencimento,valor,status) VALUES(2," + h + " - 400," + h + ",10,'XYZ')");
        });
    }

    //25- Testa se as colunas que possuem UNIQUE
    // estão sendo atendidas, email já está sendo testado pelo teste 5.
    //Mata os mutantes 19 e 20.
    @Test
    void uniquesBarramDuplicidades() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            rejeita(connection, "plano com nome repetido", "INSERT INTO plano(nome,valor_mensal,duracao_meses) VALUES('Mensal',10,1)");
                rejeita(connection, "modalidade com nome repetido", "INSERT INTO modalidade(nome) VALUES('Spinning')");
                    rejeita(connection, "mesma modalidade duas vezes na mesma matrícula", "INSERT INTO matricula_modalidade(id_matricula,id_modalidade) VALUES(1,1)");
                        rejeita(connection, "duas mensalidades da mesma competência", "INSERT INTO pagamento(id_matricula,competencia,vencimento,valor,status) " + "SELECT id_matricula,competencia,vencimento,10,'PENDENTE' FROM pagamento WHERE id_pagamento = 1");
        });
    }

    //26- Testa se as chaves estrangeiras estão barrando
    // pagamento, matriculas, modalidades inexistentes,
    // além de excluir matrículas que possuírem pagamentos.
    //Mata o mutante 24.
    @Test
    void chavesEstrangeirasBarramOrfaos() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            String h = hoje(banco);
            rejeita(connection, "pagamento de matrícula inexistente", "INSERT INTO pagamento(id_matricula,competencia,vencimento,valor,status) VALUES(999999," + h + "," + h + ",10,'PENDENTE')");
                rejeita(connection, "matrícula de aluno inexistente", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,status) VALUES(999999,1," + h + ",'CANCELADA')");
                    rejeita(connection, "matrícula de plano inexistente", "INSERT INTO matricula(id_aluno,id_plano,data_inicio,status) VALUES(4,999999," + h + ",'CANCELADA')");
                        rejeita(connection, "modalidade inexistente", "INSERT INTO matricula_modalidade(id_matricula,id_modalidade) VALUES(4,999999)");
                            rejeita(connection, "matrícula inexistente em matricula_modalidade", "INSERT INTO matricula_modalidade(id_matricula,id_modalidade) VALUES(999999,1)");
                                rejeita(connection, "apagar matrícula que tem pagamento", "DELETE FROM matricula WHERE id_matricula = 1");
        });
    }

    //27- Testa se um aluno exclui sua matrícula,
    // seus dados como modalidades são excluídos também.
    //Mata o mutante 23.
    @Test
    void apagarMatriculaApagaSuasModalidadesEmCascata() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            executar(connection, "INSERT INTO matricula_modalidade(id_matricula,id_modalidade) VALUES(4,1)");
            assertEquals(1, contar(connection, "SELECT COUNT(*) FROM matricula_modalidade WHERE id_matricula = 4"));
            executar(connection, "DELETE FROM matricula WHERE id_matricula = 4");
            assertEquals(0, contar(connection, "SELECT COUNT(*) FROM matricula_modalidade WHERE id_matricula = 4"), banco.nome());
        });
    }

    //28- Testa se cancelar_matricula salva a data de hoje
    // e a auditoria da matrícula com antes e depois.
    //Mata o mutante 10.
    @Test
    void cancelarMatriculaGravaDataDeHojeEAuditoriaCompleta() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("CALL cancelar_matricula(?)")) {
                preparedStatement.setLong(1, 2);
                preparedStatement.execute();
            }
            assertEquals(1, contar(connection, "SELECT COUNT(*) FROM matricula WHERE id_matricula = 2 AND data_fim = " + hoje(banco)), banco.nome() + ": data_fim deve ser hoje");
                assertEquals(1, contar(connection, "SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula = 2 " + "AND status_anterior = 'ATIVA' AND status_novo = 'CANCELADA'"), banco.nome() + ": auditoria deve guardar o status de antes e o de depois");
        });
    }

    //29- Testa se a auditoria de matrícula registra
    // apenas a mudança real do status de matrícula.
    //Mata o mutante 7.
    @Test
    void auditoriaSoRegistraMudancaRealDeStatus() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            executar(connection, "UPDATE matricula SET status = status WHERE id_matricula = 2");
                assertEquals(0, contar(connection, "SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula = 2"),banco.nome() + ": status igual não gera auditoria");
            executar(connection, "UPDATE matricula SET status = 'ENCERRADA' WHERE id_matricula = 2");
                assertEquals(1, contar(connection, "SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula = 2 " + "AND status_anterior = 'ATIVA' AND status_novo = 'ENCERRADA'"), banco.nome() + ": mudança real gera uma auditoria");
        });
    }

}
