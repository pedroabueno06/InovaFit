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
        System.out.println("Conectado ao banco de dados PostgreSQL!");
    }

    @Override
    protected String[] nomesConsultas() {
        return new String[] {
            "Consultas 1: Pagamentos por matrícula",
            "Consultas 2: Pagamentos por vencimento e status"
        };
    }

    @Override 
    protected String[] consultas() {
        return new String[] {
            """
            SELECT *
                FROM pagamento
            WHERE id_matricula = (
                SELECT m.id_matricula
                FROM matricula m
                JOIN aluno a ON a.id_aluno = m.id_aluno
                WHERE a.email = 'ascencobuenomiguel@gmail.com'
            )
            """,

            """
           SELECT *
            FROM pagamento
            WHERE vencimento < CURRENT_DATE - 10
            AND status = 'ATRASADO'
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
