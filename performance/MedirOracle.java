//Executa os testes de perfomance do banco de dados Oracle separadamente.
public class MedirOracle {
    public static void main(String[] args) {
        try (Oracle oracle = new Oracle()) {
            oracle.executarTestes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
