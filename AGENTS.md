
# AGENTS.md — InovaFit

## 1. Sobre o projeto

O InovaFit é um projeto acadêmico de banco de dados para gerenciamento de uma academia, desenvolvido para a A3 da disciplina de Banco de Dados (2026/2).

O sistema controla alunos, planos, matrículas, mensalidades, pagamentos, modalidades, inscrições, frequências e histórico de alterações das matrículas.

O objetivo é implementar regras de negócio diretamente nos bancos de dados, utilizando recursos de SQL, constraints, procedures, triggers, transações, índices e consultas analíticas.

## 2. Tecnologias utilizadas

- PostgreSQL 17;
- Oracle Database XE 21;
- Java 21;
- JDBC para conexão com os bancos;
- Maven para gerenciamento do projeto Java;
- Docker e Docker Compose;
- OpenCode como agente de IA;
- OpenRouter como provedor do modelo de IA.

## 3. Estrutura do projeto

- `oracle/`: scripts SQL específicos do banco de dados Oracle;
- `postgresql/`: scripts SQL específicos do banco de dados PostgreSQL;
- `procedures/`: procedures e triggers dos dois bancos de dados;
- `performance/`: classes Java e scripts SQL utilizados nos testes de desempenho;
- `java/`: aplicação Java com JDBC;
- `docker/`: scripts de inicialização dos bancos de dados;
- `tests/`: testes automatizados, testes e mutação e resultados dos mesmos;
- `tests/java/com/example/`: testes de estrutura, dados, consultas, índices, procedures e triggers;
- `tests/java/mutacao`: implementação dos testes de mutação;
- `tests/resultados`: relatórios gerados a partir dos testes de mutação;
- `.agents/skills/`: skills utilizadas pelo OpenCode;
- `der/`: destinada aos arquivos e tabelas do modelo desenvolvido no Architect;
- `docker-compose.yml`: configuração dos serviços Docker;
- `README.md`: documentação do projeto;
- `AGENTS.md`: instruções e regras para o agente de IA;
- `opencode.json`: configuração de permissões e segurança do OpenCode.

## 4. Regras de desenvolvimento

1. Sempre analisar os arquivos existentes antes de propor alterações;
2. Preservar a estrutura e o funcionamento atual do projeto;
3. Não substituir arquivos completos sem necessidade;
4. Explicar as alterações propostas antes de realizá-las;
5. Não inventar tabelas, colunas, relacionamentos ou regras de negócio;
6. Quando houver dúvida sobre uma regra, perguntar antes de implementá-la;
7. Manter os scripts de PostgreSQL e Oracle separados;
8. Utilizar a sintaxe específica de cada SGBD;
9. Não considerar um script correto sem verificar sua execução;
10. Escrever comentários claros nos scripts SQL;
11. Não remover testes ou funcionalidades existentes no projeto sem a devida autorização;
12. Não alterar configurações de conexão ou portas sem verificar como a aplicação está sendo executada.

## 5. Cuidados com os bancos de dados

Os bancos utilizados no projeto são ambientes acadêmicos de laboratório.

Nunca executar operações destrutivas sem autorização explícita do usuário.

Isso inclui:

- DROP;
- TRUNCATE;
- DELETE;
- UPDATE sem WHERE;
- exclusão de tabelas, schemas ou volumes Docker.

Nunca executar comandos contra bancos externos ou de produção.

Antes de qualquer alteração no banco:

1. Explicar o que será executado;
2. Informar quais tabelas serão afetadas;
3. Aguardar autorização do usuário;
4. Verificar se a operação corresponde ao ambiente de laboratório.

Não armazenar senhas reais, credenciais ou chaves de API no Git.

## 6. PostgreSQL e Oracle

Todo SQL deve ser compatível com o banco de destino.

PostgreSQL:
- Utilizar sintaxe PL/pgSQL quando necessário.
- Utilizar EXPLAIN (ANALYZE, BUFFERS) para análise de execução.
- Respeitar as particularidades de tipos, funções e transações do PostgreSQL.

Oracle:
- Utilizar PL/SQL quando necessário.
- Utilizar EXPLAIN PLAN FOR e DBMS_XPLAN.DISPLAY.
- Respeitar as particularidades de tipos, funções e transações do Oracle.

Nunca copiar um script de um banco para o outro sem revisar as diferenças de sintaxe.

## 7. Índices e testes de performance

Objetivos:

- Avaliar índices utilizados nas consultas e relacionamentos;
- Preparar uma massa de dados de teste suficiente;
- Coletar planos de execução antes da criação dos índices;
- Criar e testar os índices propostos;
- Coletar novamente os planos depois da criação;
- Comparar os resultados dos dois bancos.

Regras obrigatórias:

1. Nunca inventar tempos de execução;
2. Nunca afirmar que houve melhoria sem evidências;
3. Utilizar os planos reais coletados nos bancos;
4. Manter as condições dos testes antes e depois equivalentes;
5. Separar o tempo de execução do banco do tempo medido pela aplicação Java;
6. Explicar por que cada índice foi escolhido;
7. Não criar índices desnecessários apenas para aumentar a quantidade.

Os arquivos de referência dessa etapa são:

- `performance/MedirPerformance.java`
- `performance/Motor.java`
- `performance/PostgreSQL.java`
- `performance/Oracle.java`
- `performance/Postgresql_dados.sql`
- `performance/Oracle_dados.sql`
- `postgresql/Indices.sql`
- `oracle/Indices.sql`

O resultado final deverá ser documentado em:

`performance/explain_antes_depois.txt`

## 8. Aplicação Java

A aplicação utiliza JDBC para conectar ao PostgreSQL e Oracle.

Regras:

- Preservar as conexões existentes;
- Utilizar PreparedStatement e parâmetros bind;
- Tratar SQLException adequadamente;
- Não expor credenciais;
- Não remover funcionalidades existentes sem autorização;
- Verificar a compatibilidade com Java 21 e Maven;
- Não criar classes vazias ou desnecessárias.

## 8.1. Configuração das conexões

As conexões devem respeitar o ambiente de execução.

Quando a aplicação Java é executada diretamente no Windows, utilizar as portas publicadas pelo Docker:

- PostgreSQL: localhost:5433;
- Oracle: localhost:1521.

Quando a aplicação ou os testes são executados dentro da rede do Docker Compose, utilizar os nomes dos serviços:

- PostgreSQL: postgres:5432;
- Oracle: oracle:1521.

As variáveis de ambiente de conexão devem ter prioridade sobre os valores padrão do código.

Não alterar uma URL de conexão sem verificar se ela é utilizada localmente ou dentro do Docker.

## 9. Testes automatizados

O projeto possui testes automatizados com framework JUnit 5 para verificar o funcionamento dos bancos de dados PostgreSQL e Oracle.

Os testes abrangem:

- Estrutura das tabelas e relacionamentos;
- Existência de chaves primárias e estrangeiras;
- Dados de carga;
- Consultas SQL;
- Índices;
- Procedures;
- Triggers;
- Conexões com os dois bancos

Regras:

1. Não remover testes existentes para fazer com que uma execução passe no teste;
2. Não modificar resultados esperados apenas para eliminar falhas sem verificar a regra de negócio;
3. Verificar se os testes funcionam nos dois SGBDs;
4. Preservar o isolamento dos testes e o rollback quando aplicável;
5. identificar se uma falha é causada pelo código, pelo banco de dados, pela conexão ou pelo ambiente de execução;
6. não afirmar que os testes passaram sem executar e verificar o resultado.

Comando para executar os testes:

- docker-compose run --rm testes

## 10 Testes de mutação

O projeto possui uma implementação própria de testes de mutação em Java.

Os testes realizam modificações propositais nas regras de negócio no scripts SQL para verificar se os testes automatizados conseguem detectar comportamentos incorretos.

A implementação está em:

- `testes/Java/mutacao/ExecutarMutacoes.java`
- `testes/Java/mutacao/MutantesTes.java`
- `testes/Java/mutacao/ResultadoMutacao.java`

Os resultados são registrados em:

- `testes/resultados/resultado_mutacao.md`

Atualmente, existem cinco mutantes, avaliados nos dois bancos de dados:

1. Inversão da validação de matrícula ativa e desbloqueada.
2. Alteração do limite de dez dias de atraso.
3. Remoção da restrição de uma matrícula ativa por aluno.
4. Permissão de valor mensal igual a zero.
5. Geração de auditoria mesmo quando o status não muda.

Regras:

1. Não executar alterações de mutação diretamente nos bancos de dados principais.
2. Utilizar as cópias temporárias e os ambientes isolados previstos na implementação.
3. Não remover volumes ou containers principal do projeto.
4. Preservar a limpeza dos ambientes temporários após cada execução.
5. Executar os testes correspondentes a cada mutante.
6. Registrar os resultados reais, sem inventar mutantes detectados ou não detectados.
7. Não interpretar uma taxa de detecção de 100% como garantia de ausência de defeitos no sistema.

Comando para executar todos os testes de mutação:

- mvn exec:java -Dexec.mainClass=mutacao.ExecutarMutacoes -Dexec.classpathScope=test

## 11. Skills disponíveis

O projeto possui as seguintes skills:

- grilling: questionar e revisar decisões técnicas;
- diagnosing-bugs: investigar e corrigir problemas;
- code-review: revisar código e identificar possíveis falhas;
- tdd: orientar a criação e execução de testes.

## 12. Uso do agente de IA

O agente deve atuar como auxiliar técnico, não como substituto do entendimento do grupo.

Sempre:

- Explicar o motivo das sugestões;
- Apontar possíveis erros e limitações;
- Diferenciar fatos verificados de hipóteses;
- Não inventar resultados de testes;
- Não afirmar que executou comandos sem realmente executá-los;
- Solicitar autorização antes de operações potencialmente destrutivas;
- Não criar, excluir ou modificar arquivos e bancos de dados sem autorização do usuário.