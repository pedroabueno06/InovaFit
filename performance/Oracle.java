import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Oracle extends Motor {
    public Oracle() throws SQLException {
        String url = System.getenv().getOrDefault("ORA_URL", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String usuario = System.getenv().getOrDefault("ORA_USER", "inovafit");
        String senha = System.getenv().getOrDefault("ORA_PASSWORD_APP", "inovafit_lab");
        conexao = DriverManager.getConnection(url, usuario, senha);
        System.out.println("Conectado ao banco de dados Oracle!");
    }

    @Override
    protected String[] nomesConsultas() {
        return new String[] {
            "Consulta 1: Pagamentos por matrícula",
            "Consulta 2: Pagamentos por vencimentos e status"
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
                WHERE a.email = 'perf777@exemplo.test'
            )
            """,

            """
            SELECT *
            FROM pagamento
            WHERE vencimento < TRUNC(SYSDATE) - 10
            AND status = 'ATRASADO'
            """
        };
    }

    @Override
    protected void exibirPlano(String sql) throws SQLException {
        try (Statement statement = conexao.createStatement()) {
            statement.execute("EXPLAIN PLAN FOR " + sql);

            try (ResultSet resultSet = statement.executeQuery("SELECT PLAN_TABLE_OUTPUT " + "FROM TABLE(DBMS_XPLAN.DISPLAY)")) {

                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }
        }
    }
}
