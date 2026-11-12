package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.junit.jupiter.api.Test;

public class ProceduresTest {
    
    //13- Testa se a procedure retorna o status dos pagamentos
    @Test
    void proceduresRetornaStatusDosPagamentos() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            assertEquals("PAGO", consultarStatusPagamento(banco, connection, 1));
            assertEquals("PENDENTE", consultarStatusPagamento(banco, connection, 2));
            assertEquals("ATRASADO", consultarStatusPagamento(banco, connection, 3));
        });
    }

    //Método auxiliar para que o status possa ser consultado
    // dentro do banco de dados que está sendo utilizado
    private String consultarStatusPagamento(BancoTest.Banco banco, Connection connection, long id) throws SQLException {
        if (banco.oracle()) {
            try (CallableStatement callableStatement = connection.prepareCall("{call registrar_pagamento(?, ?)}")) {
                callableStatement.setLong(1, id);
                callableStatement.registerOutParameter(2, Types.VARCHAR);
                callableStatement.execute();
                return callableStatement.getString(2);
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("CALL registrar_pagamento(?, NULL)")) {
            preparedStatement.setLong(1, id);
            boolean temResultado = preparedStatement.execute();
            assertTrue(temResultado, "PostgreSQL deveria retornar o parâmetro OUT como resultado");
            try (ResultSet resultSet = preparedStatement.getResultSet()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    //14- Testa se a procedure retorna erro de negócio em pagamento inexistente
    @Test
    void proceduresRetornaErroDeNegocioEmPagamentoInexistente() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            SQLException erro;
            if (banco.oracle()) {
                try (CallableStatement callableStatement = connection.prepareCall("{call registrar_pagamento(?, ?)}")) {
                    callableStatement.setLong(1, 999999);
                    callableStatement.registerOutParameter(2, Types.VARCHAR);
                    erro = assertThrows(SQLException.class, callableStatement::execute);
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement("CALL registrar_pagamento(?, NULL)")) {
                    preparedStatement.setLong(1, 999999);
                    erro = assertThrows(SQLException.class, preparedStatement::execute);
                }
            }
            assertTrue(erro.getMessage().contains("PAGAMENTO_NAO_ENCONTRADO"), banco.nome());
        });
    }

    //15- Testa se a auditoria de dados funciona ao cancelar a matrícula, assim atualizando os dados
    @Test
    void cancelarMatriculaAtualizaDadosEGeraAuditoria() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            long auditoriasAntes;
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula=1 AND status_novo='CANCELADA'"); ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next(); auditoriasAntes = resultSet.getLong(1);
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("CALL cancelar_matricula(?)")) {
                preparedStatement.setLong(1, 1);
                preparedStatement.execute();
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT status,bloqueada,data_fim FROM matricula WHERE id_matricula=1"); ResultSet resultSet = preparedStatement.executeQuery()) {
                assertTrue(resultSet.next());
                assertEquals("CANCELADA", resultSet.getString("status"));
                if (banco.oracle()) assertEquals(1, resultSet.getInt("bloqueada"));
                else assertTrue(resultSet.getBoolean("bloqueada"));
                assertNotNull(resultSet.getDate("data_fim"));
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM auditoria_matricula WHERE id_matricula=1 AND status_novo='CANCELADA'"); ResultSet resultSet = preparedStatement.executeQuery()) {
                resultSet.next(); assertEquals(auditoriasAntes + 1, resultSet.getLong(1));
            }
        });
    }

    //16- Testa se ao tentar cancelar uma matrícula inativa é retornada uma mensagem de erro
    @Test
    void cancelarMatriculaInativaRetornaErro() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            SQLException erro;
            try (PreparedStatement preparedStatement = connection.prepareStatement("CALL cancelar_matricula(?)")) {
                preparedStatement.setLong(1, 4); // matrícula já cancelada na carga inicial
                erro = assertThrows(SQLException.class, preparedStatement::execute);
            }
            assertTrue(erro.getMessage().contains("MATRICULA_ATIVA_NAO_ENCONTRADA"), banco.nome());
        });
    }

    
}
