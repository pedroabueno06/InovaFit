# InovaFit

Sistema acadêmico de gerenciamento de academia, desenvolvido para demonstrar modelagem relacional, implementação de regras de negócio em bancos de dados e integração de uma aplicação Java com dois SGBDs: **PostgreSQL** e **Oracle**.

O projeto permite trabalhar com dados de alunos, planos, matrículas, modalidades, pagamentos e frequências. As regras de negócio são implementadas principalmente no banco de dados, por meio de constraints, procedures, triggers e índices.

## Sumário

- [Objetivos](#objetivos)
- [Tecnologias](#tecnologias)
- [Funcionalidades](#funcionalidades)
- [Modelo de dados](#modelo-de-dados)
- [Regras de negócio](#regras-de-negócio)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Configuração do ambiente](#configuração-do-ambiente)
- [Execução da aplicação Java](#execução-da-aplicação-java)
- [Testes automatizados](#testes-automatizados)
- [Testes de mutação](#testes-de-mutação)
- [Análise de desempenho](#análise-de-desempenho)
- [Observações técnicas](#observações-técnicas)

## Objetivos

O InovaFit tem como objetivos:

- Modelar um banco de dados relacional para o cenário de uma academia;
- Implementar e validar regras de negócio diretamente no banco de dados;
- Manter versões dos scripts SQL compatíveis com PostgreSQL e Oracle;
- Integrar a aplicação Java aos dois bancos utilizando JDBC;
- Desenvolver consultas SQL para extração e análise de informações;
- Avaliar o uso de índices e o desempenho de consultas;
- Verificar o comportamento do sistema por meio de testes automatizados e testes de mutação.

## Tecnologias

| Tecnologia | Aplicação no projeto |
|---|---|
| Java 21 | Aplicação e ferramentas de teste de desempenho |
| JDBC | Comunicação entre Java e os bancos de dados |
| Maven | Dependências, compilação e execução |
| PostgreSQL 17 | Banco de dados relacional |
| Oracle Database XE 21 | Banco de dados relacional |
| Docker | Ambientes isolados dos bancos |
| Docker Compose | Inicialização e integração dos serviços |
| JUnit 5 | Testes automatizados |

## Funcionalidades

A aplicação Java disponibiliza um menu para selecionar o banco de dados que será utilizado: PostgreSQL ou Oracle. Após estabelecer a conexão, o usuário pode:

1. Consultar alunos com matrícula ativa, exibindo nome, plano, status e informação de bloqueio;
2. Consultar o status de um pagamento informando seu identificador;
3. Voltar ao menu de seleção do banco de dados;
4. Encerrar a aplicação.

A consulta de alunos utiliza `PreparedStatement`. A consulta do pagamento chama a procedure `registrar_pagamento`, utilizando os recursos JDBC apropriados para cada SGBD.

## Modelo de dados

Os scripts DDL definem as seguintes tabelas:

| Tabela | Finalidade |
|---|---|
| `aluno` | Cadastro dos alunos |
| `plano` | Planos oferecidos pela academia |
| `modalidade` | Modalidades disponíveis |
| `matricula` | Matrículas dos alunos nos planos |
| `matricula_modalidade` | Associação entre matrículas e modalidades |
| `pagamento` | Mensalidades e seus estados de pagamento |
| `frequencia` | Registros de frequência dos alunos |
| `auditoria_matricula` | Histórico de alterações de status das matrículas |

As relações entre as entidades são definidas por chaves primárias e estrangeiras. Os scripts também utilizam restrições para validar os dados inseridos.

O modelo visual disponibilizado no projeto está em `der/inova.png`.

## Regras de negócio

As regras estão distribuídas entre os scripts SQL de PostgreSQL e Oracle.

### Procedures

O projeto implementa duas procedures em cada banco:

- **`registrar_pagamento`**: recebe o identificador de um pagamento e retorna seu status. Caso o pagamento não exista, gera um erro de negócio.
- **`cancelar_matricula`**: cancela uma matrícula ativa, registra a data de encerramento e bloqueia a matrícula. Caso não encontre uma matrícula ativa com o identificador informado, gera um erro.

### Triggers

- **`trg_validar_frequencia`**: valida as condições da matrícula antes de permitir a inclusão de uma frequência. A matrícula precisa estar ativa e desbloqueada; também é verificada a existência de pagamento não pago com vencimento superior a dez dias.
- **`trg_auditar_matricula`**: registra na tabela `auditoria_matricula` as alterações realizadas no status de uma matrícula.

### Restrições e índices

Os arquivos DDL definem a estrutura e as restrições das tabelas. Os arquivos de índices contêm índices criados para apoiar consultas e operações do banco.

Os scripts de cada SGBD são mantidos separadamente para respeitar as diferenças entre PostgreSQL e Oracle.

## Consultas SQL

Cada banco possui um arquivo próprio de consultas:

- `postgresql/consultas.sql`
- `oracle/consultas.sql`

As consultas exploram operações como:

- Junções (`JOIN`);
- Agrupamentos e filtros com `GROUP BY` e `HAVING`;
- Funções de janela, como `RANK()` e `SUM() OVER`;
- Subconsultas;
- Consultas com `NOT EXISTS`;
- Identificação de pagamentos atrasados;
- Cálculo de valores recebidos por competência.

## Estrutura do projeto

```text
InovaFit-master/
├── der/
│   └── inova.png
├── docker/
│   ├── dockerfile.testes
│   ├── oracle-init/
│   │   └── oracle-init.sh
│   └── postgres-init/
│       └── postgres-init.sh
├── java/
│   └── InovaFit/
│       └── src/main/java/com/example/
│           └── inovafit.java
├── oracle/
│   ├── consultas.sql
│   ├── ddl.sql
│   ├── indices.sql
│   └── inserts.sql
├── postgresql/
│   ├── consultas.sql
│   ├── ddl.sql
│   ├── indices.sql
│   └── inserts.sql
├── procedures/
│   ├── oracle_procedures.sql
│   └── postgresql_procedures.sql
├── performance/
│   ├── MedirOracle.java
│   ├── MedirPerformance.java
│   ├── MedirPostgreSQL.java
│   ├── Motor.java
│   ├── Oracle.java
│   ├── PostgreSQL.java
│   ├── Oracle_dados.sql
│   ├── Postgresql_dados.sql
│   ├── perf_oracle.sql
│   ├── perf_oracle_admin.sql
│   ├── perf_oracle_indices.sql
│   ├── perf_postgresql_indices.sql
│   ├── perf_postgresql_schema.sql
│   ├── perf_reset.sql
│   └── explain_antes_depois.txt
├── tests/
│   ├── java/com/example/
│   │   ├── BancoTest.java
│   │   ├── ConsultasTest.java
│   │   ├── DadosTest.java
│   │   ├── DdlTest.java
│   │   ├── IndicesTest.java
│   │   ├── InovafitTest.java
│   │   ├── ProceduresTest.java
│   │   └── TriggerTest.java
│   ├── java/mutacao/
│   │   ├── ExecutarMutacoes.java
│   │   ├── MutantesTest.java
│   │   └── ResultadoMutacao.java
│   └── resultados/
│       └── resultado_mutacao.md
├── docker-compose.yml
├── pom.xml
└── README.md
```

## Configuração do ambiente

### Pré-requisitos

- Docker instalado e em execução;
- Docker Compose;
- Java 21 e Maven, caso a aplicação seja executada fora do Docker.

### Inicializar os bancos de dados

Na pasta raiz do projeto, execute:

```bash
docker compose up -d --wait
```

O Compose configura os serviços PostgreSQL e Oracle, com volumes persistentes e scripts de inicialização.

Para verificar os serviços:

```bash
docker compose ps
```

Para acompanhar os logs:

```bash
docker compose logs -f postgres
docker compose logs -f oracle
```

### Dados de conexão

**PostgreSQL**

| Configuração | Valor |
|---|---|
| Host | `localhost` |
| Porta publicada | `5433` |
| Banco | `inovafit` |
| Usuário | `inovafit` |
| Senha de laboratório | `inovafit_lab` |

**Oracle**

| Configuração | Valor |
|---|---|
| Host | `localhost` |
| Porta | `1521` |
| Serviço | `XEPDB1` |
| Usuário da aplicação | `inovafit` |
| Senha de laboratório | `inovafit_lab` |

As portas internas dos containers são diferentes das portas publicadas no computador. Por isso, a aplicação executada diretamente no computador utiliza `localhost:5433` para PostgreSQL. Os serviços executados dentro da rede do Docker utilizam `postgres:5432`.

As credenciais apresentadas são destinadas ao ambiente acadêmico local. Não utilize essas configurações em produção.

## Execução da aplicação Java

Com Java 21 e Maven instalados, e os bancos já inicializados, execute na raiz do projeto:

```bash
mvn compile
mvn exec:java
```

Também é possível iniciar a aplicação pelo serviço configurado no perfil `apps`:

```bash
docker compose --profile apps run --rm app-java
```

A aplicação utiliza as seguintes variáveis de ambiente, quando definidas:

| Variável | Finalidade |
|---|---|
| `PG_URL` | URL JDBC do PostgreSQL |
| `PG_USER` | Usuário do PostgreSQL |
| `PG_PASSWORD` | Senha do PostgreSQL |
| `ORA_URL` | URL JDBC do Oracle |
| `ORA_USER` | Usuário do Oracle |
| `ORA_PASSWORD_APP` | Senha do Oracle |

Quando as variáveis não são definidas, o código utiliza valores padrão para o ambiente local.

## Testes automatizados

Os testes automatizados estão em `tests/java/` e utilizam JUnit 5. Eles verificam aspectos como:

- Conexão com os bancos;
- Estrutura das tabelas;
- Dados inseridos;
- Consultas SQL;
- Índices;
- Procedures;
- Triggers;
- Integração da aplicação.

Para executar os testes pelo serviço Docker configurado no projeto:

```bash
docker compose run --rm testes
```

A execução depende de os serviços PostgreSQL e Oracle estarem disponíveis e saudáveis.

## Testes de mutação

O projeto inclui uma implementação própria de testes de mutação em `tests/java/mutacao/`. O objetivo é verificar se os testes automatizados conseguem identificar alterações indevidas nas regras de negócio.

Os cenários registrados incluem alterações na validação de matrícula, no limite de atraso, na restrição de matrícula ativa, na validação de valor mensal e na geração de auditoria.

Para executar o programa de mutação com Maven:

```bash
mvn exec:java -Dexec.mainClass=mutacao.ExecutarMutacoes -Dexec.classpathScope=test
```

O relatório versionado está em:

```text
tests/resultados/resultado_mutacao.md
```

Consulte esse arquivo para verificar os resultados registrados. A execução de testes de mutação não substitui a validação funcional do sistema.

## Análise de desempenho

A pasta `performance/` contém classes Java e scripts SQL utilizados para preparar dados, executar consultas e comparar planos de execução antes e depois da criação de índices.

Arquivos principais:

- `MedirPerformance.java`: ponto de entrada da medição;
- `Motor.java`: estrutura de apoio à execução;
- `PostgreSQL.java` e `Oracle.java`: rotinas específicas de cada banco;
- `Postgresql_dados.sql` e `Oracle_dados.sql`: preparação da massa de dados;
- `explain_antes_depois.txt`: registro de planos e medições.

O arquivo `explain_antes_depois.txt` contém resultados registrados para consultas no PostgreSQL e no Oracle. Os dados devem ser interpretados considerando que o plano, o volume de dados e as condições de execução influenciam os tempos medidos. Não se deve generalizar os resultados para outros ambientes sem repetir os testes.

Para iniciar a execução de desempenho pelo serviço configurado no Compose, o próprio arquivo `docker-compose.yml` documenta o seguinte comando:

```bash
docker compose --profile apps run --rm app-java mvn clean compile exec:java -Dexec.mainClass=MedirPerformance
```

## Encerrar os bancos

Para parar e remover os containers, preservando os volumes:

```bash
docker compose down
```

Para remover também os volumes e os dados persistidos:

```bash
docker compose down -v
```

**Atenção:** `docker compose down -v` apaga os dados armazenados nos volumes dos bancos. Utilize somente se desejar excluir os dados e reinicializar o ambiente.

## Observações técnicas

- O projeto contém scripts SQL separados para PostgreSQL e Oracle. Não execute o script de um banco no outro sem adaptar e validar a sintaxe.
- O arquivo Java presente no ZIP está nomeado `inovafit.java`, enquanto a classe pública declarada nele é `Inovafit`. Em Java, o nome do arquivo de uma classe pública deve corresponder ao nome da classe, inclusive quanto a maiúsculas e minúsculas. Esse ponto precisa ser corrigido ou validado para que a compilação funcione de forma consistente.
- O `pom.xml` configura `com.example.Inovafit` como classe principal.
- A documentação descreve os arquivos e recursos encontrados no projeto. A presença de um teste ou script não significa, por si só, que ele foi executado com sucesso no ambiente atual.
- O escopo documentado neste README se limita às tabelas, procedimentos, triggers e funcionalidades identificados nos arquivos do projeto.

## Finalidade acadêmica

Projeto desenvolvido para fins acadêmicos, com foco em banco de dados relacionais, SQL, integração Java/JDBC, testes e análise de desempenho.
