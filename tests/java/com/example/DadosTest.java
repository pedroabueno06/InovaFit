
package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.Test;

public class DadosTest {

    //4- Testa se o projeto possui uma estrutura com dados de carga
    @Test
    void estruturaPossuiDadosDeCarga() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            assertTrue(contar(connection, "aluno") >= 4, banco.nome() + ": alunos");
            assertTrue(contar(connection, "plano") >= 3, banco.nome() + ": planos");
            assertTrue(contar(connection, "modalidade") >= 4, banco.nome() + ": modalidades");
            assertTrue(contar(connection, "matricula") >= 4, banco.nome() + ": matrículas");
            assertTrue(contar(connection, "matricula_modalidade") >= 5, banco.nome() + ": associações");
            assertTrue(contar(connection, "pagamento") >= 3, banco.nome() + ": pagamentos");
        });
    }

    //5- Testa se o sistema rejeita um email que já está sendo utilizado
    @Test
    void rejeitaEmailDuplicado() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            String sql = "INSERT INTO aluno(nome,email,data_nascimento) VALUES(?,?,?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, "Aluno teste");
                preparedStatement.setString(2, "aluno1@exemplo.com");
                preparedStatement.setDate(3, Date.valueOf("2000-01-01"));

                assertThrows(
                    SQLException.class,
                    preparedStatement::executeUpdate,
                    banco.nome()
                );
            }
        });
    }

    //6- Testa se o sistema rejeita um valor de plano inválido
    @Test
    void rejeitaValorDePlanoInvalido() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            String sql = "INSERT INTO plano(nome,valor_mensal,duracao_meses) "
                       + "VALUES('Plano teste',-1,1)";

            try (Statement statement = connection.createStatement()) {
                assertThrows(
                    SQLException.class,
                    () -> statement.executeUpdate(sql),
                    banco.nome()
                );
            }
        });
    }

    // Realiza a conta dos registros de uma tabela
    private long contar(java.sql.Connection connection, String tabela) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + tabela;

        try (Statement statement = connection.createStatement();
             ResultSet resultado = statement.executeQuery(sql)) {

            assertTrue(resultado.next());
            return resultado.getLong(1);
        }
    }
}
