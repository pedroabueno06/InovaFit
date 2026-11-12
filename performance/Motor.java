import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
public abstract class Motor implements AutoCloseable {
    
//AutoCloseable é uma interface que define um objeto que possui a capacidade
// de reter recursos, como conexões de banco de dados que necessitam ser fechados após serem utilizados.

    protected Connection conexao;
    protected abstract String[] nomesConsultas();
    protected abstract String[] consultas();
    protected abstract void exibirPlano(String sql) throws SQLException;

    private static final int REPETICOES = 100;

    public void executarTestes() throws SQLException{
        String[] nomes = nomesConsultas();
        String[] sqls = consultas();
        String fase = System.getenv().getOrDefault("PERF_FASE", "EXECUCAO");

        if (nomes.length != sqls.length) {
            throw new IllegalArgumentException("A quantidade de consultas e nomes é diferente.");
        }

        System.out.println("FASE: " + fase);

        for(int i = 0; i < sqls.length; i++) {
            System.out.println("\n==============================");
            System.out.println(nomes[i]);
            System.out.println("===============================");
            System.out.println("\nPlano de execução:");
            exibirPlano(sqls[i]);

            double mediana = medirConsultas(sqls[i]);

            System.out.printf("%nTempo da aplicação (mediana de %d execuções, inclui JDBC/rede): %.4f ms%n", REPETICOES, mediana);
        }
    }

    private double medirConsultas(String sql) throws SQLException {
        int aquecimentos = 10;
        int repeticoes = REPETICOES; //Cada consulta será executada 100 vezes.

        //Aquecimento do banco de dados.
        for (int i = 0; i < aquecimentos; i++) {
            executarConsultas(sql);
        }

        long[] tempos = new long[repeticoes];

        //Medição das execuções
        for (int i = 0; i < repeticoes; i++) {
            long inicio = System.nanoTime(); //Realiza a medição do tempo de execução.
            executarConsultas(sql);
            long fim = System.nanoTime(); //Realiza a medição do tempo de execução.
            tempos[i] = fim - inicio;
        }

        Arrays.sort(tempos); //Tempos são organizados do menor ao maior.

        //Calcula a mediana.
        double mediana;

        if (repeticoes % 2 == 0) {
            mediana = (tempos[repeticoes / 2 - 1] + tempos[repeticoes / 2]) / 2.0;
        } else {
            mediana = tempos[repeticoes / 2];
        }
        return mediana / 1_000_000.0;
    }

    private void executarConsultas(String sql) throws SQLException {
        try (PreparedStatement statement = conexao.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                resultSet.getObject(1);
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if(conexao != null && !conexao.isClosed()) {
            conexao.close();
        }
    }
}
