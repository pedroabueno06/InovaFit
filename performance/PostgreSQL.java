import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgreSQL extends Motor {
    public PostgreSQL() throws SQLException {

        String url = System.getenv().getOrDefault("PG_URL", "jdbc:postgresql://localhost:5432/inovafit");
        String usuario = System.getenv().getOrDefault("PG_USER", "inovafit");
        String senha = System.getenv().getOrDefault("PG_PASSWORD", "inovafit_lab");
        conexao = DriverManager.getConnection(url, usuario, senha);
        System.out.println("Conectado ao banco de dados PostgreSQL (schema performance)!");
    }

    @Override
    protected String[] nomesConsultas() {
        return new String[] {
            "Consulta 1: Pagamentos por matrícula (schema performance)",
            "Consulta 2: Pagamentos atrasados por vencimento e status (schema performance)"
        };
    }

    @Override
    protected String[] consultas() {
        return new String[] {
            """
            SELECT p.*
            FROM performance.pagamento p
            JOIN performance.matricula m 
                ON m.id_matricula = p.id_matricula
            JOIN performance.aluno a 
                ON a.id_aluno = m.id_aluno
            WHERE a.email = 'perf777@exemplo.test'
            AND m.status = 'ATIVA'
            """,

            """
            SELECT *
            FROM performance.pagamento
            WHERE status = 'ATRASADO'
            AND vencimento < CURRENT_DATE - 10
            """
        };
    }

    @Override
    protected void exibirPlano(String sql) throws SQLException {
        String explain = "EXPLAIN (ANALYZE, BUFFERS) " + sql;

        try (Statement statement = conexao.createStatement();
            ResultSet resultSet = statement.executeQuery(explain)) {
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }
    }
}
