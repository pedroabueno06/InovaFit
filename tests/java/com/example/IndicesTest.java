package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import org.junit.jupiter.api.Test;

public class IndicesTest {
    //10- Testa se os índices do projeto estão criados
    @Test
    void indicesDoProjetoEstaoCriados() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados((banco, connection) -> {
            List<String> esperados = List.of("IX_MATRICULA_ID_PLANO", "IX_PAGAMENTO_ID_MATRICULA", "IX_PAGAMENTO_VENCIMENTO_STATUS");
            for (String nome : esperados) {
                String sql = banco.oracle()
                    ? "SELECT COUNT(*) FROM USER_INDEXES WHERE INDEX_NAME=?"
                    : "SELECT COUNT(*) FROM pg_indexes WHERE schemaname=current_schema() AND upper(indexname)=?";
                
                    try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setString(1, nome);
                    
                    try (ResultSet resultado = preparedStatement.executeQuery()) {
                        assertTrue(resultado.next());
                        assertEquals(1, resultado.getInt(1), banco.nome() + " índice " + nome + " não encontrado.");
                    }
                }
            }
        });
    }
}
