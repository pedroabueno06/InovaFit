import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Oracle extends Motor {
    public Oracle() throws SQLException {
        String url = System.getenv().getOrDefault("ORA_PERF_URL", "jdbc:oracle:thin:@localhost:1521/XEPDB1");
        String usuario = System.getenv().getOrDefault("ORA_PERF_USER", "INOVAFIT_PERF");
        String senha = System.getenv().getOrDefault("ORA_PERF_PASSWORD", "inovafit_perf_lab");
        conexao = DriverManager.getConnection(url, usuario, senha);
        System.out.println("Conectado ao schema INOVAFIT_PERF (Oracle)!");
    }

    @Override
    protected String[] nomesConsultas() {
        return new String[] {
            "Consulta 1: Pagamentos por matrícula (schema INOVAFIT_PERF)",
            "Consulta 2: Pagamentos atrasados por vencimento e status (schema INOVAFIT_PERF)"
        };
    }

    @Override
    protected String[] consultas() {
        return new String[] {
            
            """
            SELECT p.*
            FROM pagamento p
            JOIN matricula m 
                ON m.id_matricula = p.id_matricula
            JOIN aluno a 
                ON a.id_aluno = m.id_aluno
            WHERE a.email = 'perf777@exemplo.test'
            AND m.status = 'ATIVA'
            """,

            """
            SELECT *
            FROM pagamento
            WHERE status = 'ATRASADO'
            AND vencimento < TRUNC(SYSDATE) - 10
            """
        };
    }

    @Override
    protected void exibirPlano(String sql) throws SQLException {
        try (Statement statement = conexao.createStatement()) {
            System.out.println("-- Plano estimado (EXPLAIN PLAN FOR + DBMS_XPLAN.DISPLAY):");
            statement.execute("EXPLAIN PLAN FOR " + sql);

            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT PLAN_TABLE_OUTPUT FROM TABLE(DBMS_XPLAN.DISPLAY)")) {
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }

            String sqlComHint = sql.replaceFirst("(?i)SELECT", "SELECT /*+ GATHER_PLAN_STATISTICS */");
            try (ResultSet resultSet = statement.executeQuery(sqlComHint)) {
                while (resultSet.next()) {
                }
            }

            System.out.println("-- Plano real com estatísticas (DBMS_XPLAN.DISPLAY_CURSOR, ALLSTATS LAST):");
            try (ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM TABLE(DBMS_XPLAN.DISPLAY_CURSOR(NULL, NULL, 'ALLSTATS LAST'))")) {
                while (resultSet.next()) {
                    System.out.println(resultSet.getString(1));
                }
            }
        }
    }
}
