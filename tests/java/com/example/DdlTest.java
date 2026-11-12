package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class DdlTest {
    private static final List<String> TABELAS = List.of("aluno", "plano", "modalidade", "matricula", "matricula_modalidade", "pagamento", "frequencia", "auditoria_matricula");

    //7- Testa se todas as oito tabelas do projeto existem nos dois bancos
    @Test
    void tabelasDoProjeetoExistem() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            DatabaseMetaData metadata = connection.getMetaData();
            String schema = connection.getSchema();

            for (String tabela : TABELAS) {
                    String nomeTabela = banco.oracle() ? tabela.toUpperCase() : tabela.toLowerCase();

                    try (ResultSet resultado = metadata.getTables(null, schema, nomeTabela, new String[]{"TABLE"})) {
                        assertTrue(resultado.next(), banco.nome() + ": tabela não encontrada: " + tabela);
                    }
            }
        });
    }

    //8- Testa se todas as tabelas possuem uma chave primária
    @Test
    void todasAsTabelasPossuemChavePrimaria() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            DatabaseMetaData metadata = connection.getMetaData();
            String schema = connection.getSchema();

            for (String tabela : TABELAS) {
                String nomeTabela = banco.oracle() ? tabela.toUpperCase() : tabela.toLowerCase();

                try (ResultSet resultado = metadata.getPrimaryKeys(null, schema, nomeTabela)) {
                    assertTrue(resultado.next(), banco.nome() + ": tabela sem chave primária: " + tabela);
                }
            }
        });
    }

    //9- Testa se existem relacionamentos por chaves estrangeiras
    @Test
void relacionamentosPorChavesEstrangeirasExistem() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            DatabaseMetaData metadata = connection.getMetaData();
            String schema = connection.getSchema();

            int totalChavesEstrangeiras = 0;

            for (String tabela : TABELAS) {
                String nomeTabela = banco.oracle() ? tabela.toUpperCase() : tabela.toLowerCase();

                try (ResultSet resultado = metadata.getImportedKeys(null, schema, nomeTabela)) {
                    while (resultado.next()) {
                        totalChavesEstrangeiras++;
                    }
                }
            }

            assertTrue(
                totalChavesEstrangeiras > 0,
                banco.nome() + ": nenhum relacionamento por chave estrangeira encontrado"
            );
        });
    }
}