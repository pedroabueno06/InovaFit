package mutacao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ResultadoMutacao {
    public enum Situacao {
        DETECTADO,
        SOBREVIVEU,
        ERRO
    }

    //Armazena os dados de forma resumida.
    public record Resultado(
        String mutante,
        String descricao,
        String banco,
        Situacao situacao,
        String detalhe
    ) {}
    
    private final List<Resultado> resultados = new ArrayList<>();

    public void adicionar(String mutante, String descricao, String  banco, Situacao situacao, String detalhe) {
        resultados.add(new Resultado(mutante, descricao, banco, situacao, detalhe));
    }

    public void salvar(Path arquivo) throws IOException {
        Path pasta = arquivo.getParent();

        if (pasta != null) {
            Files.createDirectories(pasta);
        }

        Files.writeString(arquivo, gerarRelatorio(), StandardCharsets.UTF_8);
    }

    private String gerarRelatorio() {
        StringBuilder texto = new StringBuilder();
            texto.append("# Resultado dos testes de mutação\n\n");
            texto.append("| Mutante | Alteração | Banco | Resultado | Detalhe |\n");
            texto.append("|---|---|---|---|---|\n");

            for (Resultado resultado : resultados) {
                texto.append("| ")
                    .append(resultado.mutante()).append(" | ")
                    .append(resultado.descricao()).append(" | ")
                    .append(resultado.banco()).append(" | ")
                    .append(resultado.situacao()).append(" | ")
                    .append(resultado.detalhe())
                    .append(" |\n");
            }
            return texto.toString();
    }
}
