public class MedirPerformace {
    public static void main(String[] args) {
        System.out.println("====================");
        System.out.println("PERFORMACE - INOVAFIT");
        System.out.println("=====================");

        //Testes no banco de dados PostgreSQL
        try (PostgreSQL postgreSQL = new PostgreSQL()) {
            System.out.println("\nTestes no banco de dados PostgreSQL");
            postgreSQL.executarTestes();

        } catch (Exception e) {
            System.out.println("Erro no banco de dados PostgreSQL: " + e.getMessage());
        }

        //Testes no banco de dados Oracle
        try (Oracle oracle = new Oracle()) {
            System.out.println("//Testes no banco de dados Oracle");
            oracle.executarTestes();

        } catch (Exception e) {
            System.out.println("Erro no banco de dados Oracle: " + e.getMessage());
        }

        System.out.println("\nTestes finalizados!");
    }
}