// Realiza execução dos testes de desempenho do InovaFit nos dois bancos.
public class MedirPerformance {
    public static void main(String[] args) {
        System.out.println("=====================");
        System.out.println("PERFORMANCE - INOVAFIT");
        System.out.println("=====================");

        //Testes no banco de dados PostgreSQL.
        try (PostgreSQL postgreSQL = new PostgreSQL()) {
            System.out.println("\nTestes no banco de dados PostgreSQL");
            postgreSQL.executarTestes();

        } catch (Exception e) {
            e.printStackTrace();
        }

        //Testes no banco de dados Oracle.
        try (Oracle oracle = new Oracle()) {
            System.out.println("\nTestes no banco de dados Oracle");
            oracle.executarTestes();

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nTestes finalizados!");
    }
}
