package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;

public class TriggerTest {
    
    //11- Testa se o trigger permite emitir frequência para uma matrícula ativa
    @Test
    void triggerPermiteFrequenciaParaMatriculaAtiva() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(1)")) {
                assertEquals(1, preparedStatement.executeUpdate());
            }
        });
    }

    //12-Testa se o trigger permite emitir frequência para uma matrícula cancelada
    @Test
    void triggerRejeitaFrequenciaParaMatriculaCancelada() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(4)")) {
                assertThrows(SQLException.class, preparedStatement::executeUpdate, banco.nome());
            }
        });
    }

    //13- Testa se o trigger do banco de dados Oracle rejeita uma matrícula com um atraso superior de dez dias
    @Test
    void triggerOracleRejeitaMatriculaComAtrasoSuperiorADezDias() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            if (!banco.oracle()) return; // a versão PostgreSQL atual não implementa essa regra no trigger
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO frequencia(id_matricula) VALUES(3)")) {
                SQLException erro = assertThrows(SQLException.class, preparedStatement::executeUpdate);
                assertTrue(erro.getMessage().contains("ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS"));
            } finally {
                connection.rollback();
            }
        });
    }
}
