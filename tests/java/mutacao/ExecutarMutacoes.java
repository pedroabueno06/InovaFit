package mutacao;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class ExecutarMutacoes {

    //Faz representação de uma alteração
    // proposital no projeto.
    private record Mutante(
        String codigo,
        String descricao,
        String metodoTeste,
        String arquivoPostgres,
        String originalPostgres,
        String alteradoPostgres,
        String arquivoOracle,
        String originalOracle,
        String alteradoOracle
    ){}
    
    //Mutantes construídos no projeto InovaFit.
    private static final List<Mutante> MUTANTES = List.of(
        new Mutante(
            "Mutante1",
            "Faz a inversão da validação de matrícula ativa e desbloqueada",
            "Mutante1_matriculaInativaOuBloqueada",
            "procedures/postgresql_procedures.sql",
            "IF NOT EXISTS (",
            "IF EXISTS (",
            "procedures/oracle_procedures.sql",
            "IF v_qtd=0 THEN",
            "IF v_qtd>0 THEN"
        ),

        new Mutante(
            "Mutante2",
            "Realiza a alteração do limite de inadimplência de 10 para 11 dias",
            "Mutante2_limiteDeDezDiasDeAtraso",
            "procedures/postgresql_procedures.sql",
            "CURRENT_DATE - 10",
            "CURRENT_DATE - 11",
            "procedures/oracle_procedures.sql",
            "TRUNC(SYSDATE)-10",
            "TRUNC(SYSDATE)-11"
        ),

        new Mutante(
            "Mutante3",
            "Realiza a remoção do índice único de matrícula ativa",
            "Mutante3_UmaMatriculaAtivaPorAluno",
            "postgresql/ddl.sql",
            "CREATE UNIQUE INDEX uk_matricula_ativa_aluno ON matricula(id_aluno) WHERE status = 'ATIVA';",
            "",
            "oracle/ddl.sql",
            "CREATE UNIQUE INDEX uk_matricula_ativa_aluno ON matricula(CASE WHEN status='ATIVA' THEN id_aluno END);",
            ""
        ),

        new Mutante(
            "Mutante4",
            "Permitir plano com valor mensal igual a zero",
            "Mutante4_valorMensalDeveSerMaiorQueZero",
            "postgresql/ddl.sql",
            "CHECK (valor_mensal > 0)",
            "CHECK (valor_mensal >= 0)",
            "oracle/ddl.sql",
            "CHECK(valor_mensal>0)",
            "CHECK(valor_mensal>=0)"
        ),

        new Mutante(
            "Mutante5",
            "Faz o registro da auditoria mesmo sem mudança de status",
            "Mutante5_auditoriaSomenteQuandoStatusMudar",
            "procedures/postgresql_procedures.sql",
            "IF OLD.status IS DISTINCT FROM NEW.status THEN",
            "IF TRUE THEN",
            "procedures/oracle_procedures.sql",
            "IF :OLD.status <> :NEW.status THEN",
            "IF 1=1 THEN"
        )
    );

    //Faz as alterações nos scripts SQL.
    private static void aplicarMutacao(Path projeto, String arquivo, String original, String alterado) throws  IOException {

        Path caminho = projeto.resolve(arquivo);
        String conteudo = Files.readString(caminho, StandardCharsets.UTF_8);
        int primeiraOcorrencia = conteudo.indexOf(original);

            if (primeiraOcorrencia == -1) {
                throw new IllegalStateException("Trecho original não encontrado: " + arquivo);
            }

            if (conteudo.indexOf(original, primeiraOcorrencia + 1) != -1) {
                throw  new IllegalStateException("Trecho original aparece mais de uma vez: " + arquivo);
            }

            conteudo = conteudo.replace(original, alterado);
            Files.writeString(caminho, conteudo, StandardCharsets.UTF_8);
        
    }

    //Realiza uma cópia do projeto para
    // uma pasta temporária.
    private static void copiarProjeto(Path origem, Path destino) throws IOException {

        Files.walkFileTree(origem, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path pasta, BasicFileAttributes atributos) throws IOException {

                Path relativa = origem.relativize(pasta);
                    if(relativa.startsWith(".git") || relativa.startsWith("target")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    
                    Files.createDirectories(destino.resolve(relativa));
                        return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributo) throws IOException {

                Path relativa = origem.relativize(arquivo);
                    Files.copy(arquivo, destino.resolve(relativa), StandardCopyOption.REPLACE_EXISTING);
                        return FileVisitResult.CONTINUE;
            }
        });
    }

    //Método para executar os comandos Docker e
    // do Maven para assim obtermos os resultados da execuções.

    //Realiza o armazenamento do código de sáida e a mensagem do comando.
    private record Execucao(int codigo, String saida) {
    }

    //Exceuta um comando dentro da pasta informada.
    private static Execucao executarComando(Path pasta, String... comando) throws Exception {
        Process processo = new ProcessBuilder(comando)
            .directory(pasta.toFile())
            .redirectErrorStream(true)
            .start();

        String saida = new String(processo.getInputStream().readAllBytes(), Charset.defaultCharset());
            int codigo = processo.waitFor();
                return new Execucao(codigo, saida);
    }

    //É criado um docker-compose para os testes de mutação
    private static void prepararDockerTemporario(Path projeto) throws IOException {
        Path arquivoCompose = projeto.resolve("docker-compose.yml");
            String conteudo = Files.readString(arquivoCompose, StandardCharsets.UTF_8);
            
            conteudo = conteudo.replace("    container_name: inovafit-postgres", "");
            conteudo = conteudo.replace("    container_name: inovafit-oracle", "");
            conteudo = conteudo.replace("    ports: [\"5433:5432\"]", "");
            conteudo = conteudo.replace("    ports: [\"1521:1521\"]", "");

            Path composeTemporario = projeto.resolve("docker-compose.mutacao.yml");
            Files.writeString(composeTemporario, conteudo, StandardCharsets.UTF_8);
    }

    //Método para inciar os bancos de dados temporários.
    private static Execucao iniciarBancosDeDadosTemporarios(Path projeto, String nomeProjeto) throws Exception {
        return executarComando(projeto,"docker", "compose", "-p", nomeProjeto, "-f", "docker-compose.mutacao.yml", "up", "-d", "--wait", "postgres", "oracle");
    }

    //Método que executa os testes de mutação em apenas um dos bancos de dados.
    private static Execucao executarTestesDeMutacao(Path projeto, String nomeProjeto, String banco, Mutante mutante) throws Exception {
        return executarComando(projeto, "docker", "compose", "-p", nomeProjeto, "-f", "docker-compose.mutacao.yml", "--profile", "testes", "run", "--rm", "-e", "MUTACAO_BANCO=" + banco, "testes", "mvn","-B", "-Dtest=MutantesTest#" + mutante.metodoTeste(), "test");
    }

    //Método para enecerrar e remvoer os
    // recursos do projeto que foi criado temporariamente.
    private static Execucao limparBancosDeDadosTemporarios(Path projeto, String nomeProjeto) throws Exception {
        return executarComando(projeto, "docker", "compose", "-p", nomeProjeto, "-f", "docker-compose.mutacao.yml", "down", "-v", "--remove-orphans");
    }
    
    //Método que aplica as alterações de um mutante nos dois bancos de daods.
    private static void aplicarMutanteNosBancosDeDados(Path projeto, Mutante mutante) throws  IOException {
        aplicarMutacao(projeto, mutante.arquivoPostgres(), mutante.originalPostgres(), mutante.alteradoPostgres());
            aplicarMutacao(projeto, mutante.arquivoOracle(), mutante.originalOracle(), mutante.alteradoOracle());
                System.out.println("Mutante aplicado: " + mutante.codigo());
    }

    //Método que gera um nome exclusivo pro projeto.
    private static String gerarNomeProjeto(String codigoMutante) {
        return "inovafit-" + codigoMutante.toLowerCase();
    }

    //Método que verifica se os bancos de dados  temporários
    // foram iniciados de maneira correta.
    private static boolean bancoDeDadosTemporariosIniciados(Execucao execucao) {
        if (execucao.codigo() != 0) {
            System.out.println("Erro ao iniciar os bancos de dados:");
            System.out.println(execucao.saida());
                return false;
        }
        return true;
    }

    //Método que registra erro quandos os bancos
    // de dados naão iniciam.
    private static void registrarErrosDeInicializacaoDosBancosDeDados(ResultadoMutacao resultados, Mutante mutante, String detalhe) {
        String[] bancos = {"PostgreSQL", "Oracle"};
            for (String banco : bancos) {
                resultados.adicionar(mutante.codigo(), mutante.descricao(), banco, ResultadoMutacao.Situacao.ERRO, detalhe.replace("\n", " ").replace("|", "/"));
            }
    }

    //Método que classifica o resultado da
    // execução dos testes de mutação.
    private static ResultadoMutacao.Situacao classificarResultado(Execucao execucao) {

        if (execucao.codigo() == 0) {
            return ResultadoMutacao.Situacao.SOBREVIVEU;
        }

        if (execucao.saida().contains("Failures:") && execucao.saida().contains("Errors:")) {
            if (execucao.saida().matches("(?s).*Failures: [1-9][0-9]*, Errors: 0.*")) {
                return ResultadoMutacao.Situacao.DETECTADO;
            }
        }
        return ResultadoMutacao.Situacao.ERRO;
    }

    //Método para executar os testes de mutação nos
    // dois bancos de dados, assim registrandos os resultados.
    private static void testarMutantes(Path projeto, String nomeProjeto, Mutante mutante, ResultadoMutacao resultados) throws Exception {
        String[] bancos = {"PostgreSQL", "Oracle"};
            for (String banco : bancos) {
                System.out.println("\nTestando " + mutante.codigo() + " no banco de dados " + banco);
                    try {
                        Execucao execucao = executarTestesDeMutacao(projeto, nomeProjeto, banco, mutante);
                            ResultadoMutacao.Situacao situacao = classificarResultado(execucao);
                                resultados.adicionar(mutante.codigo(), mutante.descricao(), banco, situacao, execucao.saida().replace("\n", " ").replace("|", "/"));
                                    System.out.println("Resultado: " + situacao);
                    } catch (Exception erro) {
                        resultados.adicionar(mutante.codigo(), mutante.descricao(), banco, ResultadoMutacao.Situacao.ERRO, erro.getMessage());
                            System.out.println("Erro ao testar o banco de dados " + banco + ": " + erro.getMessage());
                    }
            }
    }
    public static void main(String[] args) {
        Path projetoOriginal = Path.of("").toAbsolutePath();
        ResultadoMutacao resultados = new ResultadoMutacao();

        System.out.println("==================================");
        System.out.println(" INICIANDO TESTES DE MUTAÇÃO");
        System.out.println("==================================");
        System.out.println("Projeto: " + projetoOriginal);

        for (Mutante mutante : MUTANTES) {
            Path copiaTemporaria = null;
                String nomeProjeto = gerarNomeProjeto(mutante.codigo());
                    boolean composePreparado = false;

                    System.out.println("\n==================================");
                    System.out.println(" EXECUTANDO " + mutante.codigo());
                    System.out.println("==================================");

                    try {
                        copiaTemporaria = criarCopiaDoInovafitTemporaria(projetoOriginal);
                            aplicarMutanteNosBancosDeDados(copiaTemporaria, mutante);
                                prepararDockerTemporario(copiaTemporaria);
                                    composePreparado = true;

                                    Execucao inicializacao = iniciarBancosDeDadosTemporarios(copiaTemporaria, nomeProjeto);
                                    if (bancoDeDadosTemporariosIniciados(inicializacao)) {
                                        testarMutantes(copiaTemporaria, nomeProjeto, mutante, resultados);

                                    } else {
                                        registrarErrosDeInicializacaoDosBancosDeDados(resultados, mutante, inicializacao.saida());
                                    }
                    } catch (Exception erro) {
                        System.out.println("Erro na execução do " + mutante.codigo());
                        System.out.println(erro.getMessage());
                        registrarErrosDeInicializacaoDosBancosDeDados(resultados, mutante, erro.getMessage());

                    } finally {
                        if (copiaTemporaria != null) {
                            if (composePreparado) {
                                try {
                                    Execucao limpeza = limparBancosDeDadosTemporarios(copiaTemporaria, nomeProjeto);
                                        if (limpeza.codigo() != 0) {
                                            System.out.println("Aviso: erro ao limpar os containers:");
                                            System.out.println(limpeza.saida());
                                        }
                                } catch (Exception erro) {
                                    System.out.println("Erro na limpeza do docker: " + erro.getMessage());
                                }
                            }

                            try {
                                excluirPastaTemporaria(copiaTemporaria);
                                    System.out.println("Cópia temporária do InovaFit excluída com sucesso!");
                            } catch (Exception erro) {
                                System.out.println("Erro ao tentar excluir a cópia temporária do InovaFit: " + erro.getMessage());
                            }
                        }
                    }
        }

        try {
            Path relatorio = projetoOriginal.resolve("tests")
                                            .resolve("resultados")
                                            .resolve("resultado_mutacao.md");
                resultados.salvar(relatorio);

                System.out.println("\n==================================");
                System.out.println(" TESTES DE MUTAÇÃO FINALIZADOS");
                System.out.println("==================================");
                System.out.println("Relatório dos testes de mutação salvo em: " + relatorio);

        } catch (Exception erro) {
            System.out.println("Erro ao tentar salvar o relatório dos testes de mutação: " + erro.getMessage());
        }
    }

    //Método que cria uma cópia temporária do InovaFit.
    private static Path criarCopiaDoInovafitTemporaria(Path projetoOriginal) throws IOException {
        Path pastaTemporaria = Files.createTempDirectory("inovafit-mutacao-");
            copiarProjeto(projetoOriginal, pastaTemporaria);
                return pastaTemporaria;
    }

    //Método para excluir a pasta temporária do InovaFit.
    private static void excluirPastaTemporaria(Path pasta) throws IOException {
        Files.walkFileTree(pasta, new SimpleFileVisitor<>() {
            
            @Override
            public FileVisitResult visitFile(Path arquivo, BasicFileAttributes atributos) throws IOException {
                Files.delete(arquivo);
                    return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path diretorio, IOException erro) throws IOException {
                if (erro != null) {
                    throw erro;
                }

                Files.delete(diretorio);
                return FileVisitResult.CONTINUE;
        }
        });
    }
}
