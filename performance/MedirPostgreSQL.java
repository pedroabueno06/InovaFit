//Realiza os testes de perfomance do banco de dados PostgreSQL separadamente.
public class MedirPostgreSQL {
    public static void main(String[] args) {
        try (PostgreSQL postgreSQL = new PostgreSQL()) {
            postgreSQL.executarTestes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}