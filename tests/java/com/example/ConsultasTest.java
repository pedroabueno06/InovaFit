package com.example;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class ConsultasTest {

    //Realiza a leitura das consultas da classe correpondente ao banco de dados
    private List<String> carregarConsultas(BancoTest.Banco banco) throws Exception {
        String caminho = banco.oracle() ? "oracle/Consultas.sql" : "postgresql/Consultas.sql";
        String arquivo = Files.readString(Path.of(caminho));
        List<String> consultas = new ArrayList<>();

        //Realiza a remoção de comentários e faz a separação dos comandos pelo ponto e vírgula
        StringBuilder sql = new StringBuilder();
            
        for(String linha : arquivo.split("\\R")) {
                String linhaLimpa = linha.strip();

                if (linhaLimpa.startsWith("--") || linhaLimpa.isBlank()) {
                    continue;
                }

                sql.append(linha).append("\n");
        }

        for (String comando: sql.toString().split(";")) {
            String consulta = comando.trim();

            if (!consulta.isBlank()) {
                consultas.add(consulta);
            }
        }

        return consultas;
    }

    //Testa as consultas do projeto em ambos os bancos de dados
    @Test
    void consultasDoProjetoRodamNosDoisBancosDeDados() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            List<String> consultas = carregarConsultas(banco);
            assertEquals(10, consultas.size(), banco.nome() + ": o arquivo deve possuir dez consultas.");

            for (int i = 0; i < consultas.size(); i++) {
                try (Statement statement = connection.createStatement();
                    ResultSet resultado = statement.executeQuery(consultas.get(i))) {
                        assertNotNull(resultado.getMetaData(), banco.nome() + ": erro na consulta " + (i + 1));
                    }
            }
            
        }));
    }

    //Testa se as consultas existem nos bancos de dados
    @Test
    void consultasExistem() throws Exception {
        for (BancoTest.Banco banco : BancoTest.bancos()) {
            List<String> consultas = carregarConsultas(banco);

            assertEquals(10, consultas.size(), banco.nome() + ": deveriam existir 10 consultas em cada banco de dados.");
        }
    }

    //Testa de todas as dez consultas correspondem ao valores esperados
    @Test
    void resultadosDasConsultasEstaoCorretos() throws Exception {
        BancoTest.testarEmAmbosOsBancosDeDados(((banco, connection) -> {
            List<String> consultas = carregarConsultas(banco);
            LocalDate hoje = obterDataAtual(connection, banco);
            LocalDate vencimento = obterVencimentoDePagamentoAtrasado(connection);

            List<Set<String>> esperados = resultadosEsperados(hoje, vencimento);

            assertEquals(10, consultas.size());
            assertEquals(10, esperados.size());

            for (int i = 0; i < consultas.size(); i++) {
                Set<String> resultadoObtido = executarConsulta(connection, consultas.get(i));

                assertEquals(esperados.get(i), resultadoObtido, banco.nome() + ": Consulta obteve um resultado incorreto" + (i + 1));
            }
        }));
    }

    //Métodos auxiliares utilizados pelo teste resultadosDasConsultasEstaoCorretos

    // Consegue a data que está dentro do banco de dados, para que assim
    // as consultas que dependem de datas possam ser validadas
    private LocalDate obterDataAtual(Connection connection, BancoTest.Banco banco) throws Exception {
        String sql = banco.oracle() ? "SELECT TRUNC(SYSDATE) FROM DUAL" : "SELECT CURRENT_DATE";

        try (Statement statement = connection.createStatement();
            ResultSet resultado = statement.executeQuery(sql)) {
                resultado.next();
                return resultado.getDate(1).toLocalDate();
            }
    }

    //Executa o SQL em uma consulta e faz com que suas linhas
    // sejam transformadas em um conjunto de valores
    private Set<String> executarConsulta(Connection connection, String sql) throws Exception {
        Set<String> linhas = new java.util.HashSet<>();

        try (Statement statement = connection.createStatement();
            ResultSet resultado = statement.executeQuery(sql)) {
                int colunas = resultado.getMetaData().getColumnCount();

                while (resultado.next()) {
                    List<String> valores = new ArrayList<>();

                    for (int i = 1; i <= colunas; i++) {
                        valores.add(normalizarValor(resultado.getObject(i)));
                    }

                    //É adicionada a linha completa ao conjunto de resultados
                    linhas.add(String.join("|", valores));
                }
                return linhas;
            }
    }

    //Deixa números e datas de forma padronizada para que assim
    // sejam evitadas certas diferenças entre o formato dos
    // bancos de dados PostgreSQL e Oracle
    private String normalizarValor(Object valor) {
        if (valor == null) {
            return "NULL";
        }

        if(valor instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime().toLocalDate().toString();
        }

        if (valor instanceof Date data) {
            return data.toLocalDate().toString();
        }

        if (valor instanceof BigDecimal numero) {
            return numero.stripTrailingZeros().toPlainString();
        }

        if (valor instanceof Number numero) {
            return new BigDecimal(numero.toString()).stripTrailingZeros().toPlainString();
        }
        return valor.toString();
    }

    //Realiza a definição sobre quais resultados cada consulta deveria retornar
    private List<Set<String>> resultadosEsperados(LocalDate hoje, LocalDate vencimentoAtrasado) {
        String competencia = hoje.withDayOfMonth(1).toString();

        return List.of(
            Set.of(
                //Alunos que possuem suas matrículas ativas
                "Pedro Ascenço Bueno|Anual|ATIVA",
                "Joaquim Attanasio da Silva|Mensal|ATIVA",
                "Gabriel Gastal|Trimestral|ATIVA"
            ),

            Set.of(
                //Modalidades em que os alunos estão inscritos
                "Pedro Ascenço Bueno|Musculação",
                "Pedro Ascenço Bueno|Spinning",
                "Joaquim Attanasio da Silva|Musculação",
                "Gabriel Gastal|Musculação",
                "Gabriel Gastal|Pilates"
            ),

            Set.of(
                //Quantidade de matrículas por plano
                "Mensal|2",
                "Trimestral|1",
                "Anual|1"
            ),

            Set.of(
                //Planos que possuem matrículas cadastradas
                "Mensal|2",
                "Trimestral|1",
                "Anual|1"
            ),

            Set.of(
                //Ranking de frequência dos alunos na academia
                "Pedro Ascenço Bueno|0|1",
                "Joaquim Attanasio da Silva|0|1",
                "Gabriel Gastal|0|1",
                "Fabrício Lopes|0|1"
            ),

            Set.of(
                //Matrículas que possuem acúmulo de pagamentos
                "1|" + competencia + "|85.5|85.5",
                "2|" + competencia + "|120|120",
                "3|" + competencia + "|110|110"
            ),

            Set.of(
                //Alunos que não estão com sua matrícula ativa
                "4|Fabrício Lopes"
            ),

            Set.of(
                //Pagamentos que estão acima da média
                "2|2|120",
                "3|3|110"
            ),


            Set.of(
                //Mensalidades que estão atrasadas há mais de dez dias
                "Gabriel Gastal|" + vencimentoAtrasado + "|110"
            ),

            Set.of(
                //Receita de pagamentos recebidas por competência
                competencia + "|85.5"
            )
        );
    }

    private LocalDate obterVencimentoDePagamentoAtrasado(Connection connection) throws Exception {
        String sql = """
            SELECT vencimento
            FROM pagamento
            WHERE id_matricula = 3
            """;

        LocalDate vencimento;

        try (Statement statement = connection.createStatement();
            ResultSet resultado = statement.executeQuery(sql)) {

            assertTrue(resultado.next(), "Pagamento atrasado não encontrado");
            vencimento = resultado.getDate(1).toLocalDate();

            assertTrue(vencimento.isBefore(LocalDate.now().minusDays(10)), "O pagamento deveria estar atrasado há mais de dez dias");
                return vencimento;
        }

    }
}
