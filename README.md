# InovaFit

Sistema de banco de dados para gerenciamento de uma academia, desenvolvido como projeto acadêmico da disciplina de Banco de Dados.

O InovaFit tem como objetivo organizar informações de alunos, planos, matrículas, pagamentos, modalidades e frequências, aplicando regras de negócio diretamente no banco de dados.

## 1. Objetivos do projeto

O sistema foi desenvolvido em **Java** e utiliza JDBC para acessar dois sistemas gerenciadores de banco de dados. O projeto **não foi desenvolvido em Python** e não possui uma aplicação Python como parte de sua implementação.

O sistema trabalha com os seguintes bancos de dados:

- PostgreSQL 17;
- Oracle Database XE 21.

A aplicação Java utiliza JDBC para se conectar aos bancos. O projeto também utiliza Maven para gerenciar as dependências e Docker Compose para configurar os ambientes.

O banco de dados contempla o gerenciamento de:

- alunos;
- planos;
- modalidades;
- matrículas;
- modalidades vinculadas às matrículas;
- pagamentos;
- frequências;
- auditoria das alterações de matrícula.

As regras de negócio são implementadas com recursos próprios dos bancos, como chaves primárias e estrangeiras, restrições (`CHECK` e `UNIQUE`), procedures, triggers, índices e transações.

## 2. Tecnologias utilizadas

| Tecnologia | Utilização |
|---|---|
| PostgreSQL 17 | Banco de dados relacional |
| Oracle Database XE 21 | Segundo banco de dados relacional |
| Java 21 | Linguagem da aplicação e acesso aos bancos |
| JDBC | Conexão entre Java e os bancos |
| Maven | Gerenciamento de dependências e execução Java |
| Docker | Criação dos ambientes dos bancos |
| Docker Compose | Orquestração dos serviços |

## 3. Estrutura original do projeto

```text
InovaFit-master/
├── .agents/
├── docker/
│   ├── oracle-init/
│   │   └── oracle-init.sh
│   └── postgres-init/
│       └── postgres-init.sh
├── java/
│   └── InovaFit/
│       └── src/main/java/com/example/
│           ├── SQLExeception.java
│           └── inovafit.java
├── oracle/
│   ├── consultas.sql
│   ├── ddl.sql
│   ├── indices.sql
│   └── inserts.sql
├── performance/
│   ├── MedirPerformance.java
│   ├── Motor.java
│   ├── Oracle.java
│   ├── Oracle_dados.sql
│   ├── PostgreSQL.java
│   └── Postgresql_dados.sql
├── postgresql/
│   ├── Consultas.sql
│   ├── Ddl.sql
│   ├── Indices.sql
│   └── Inserts.sql
├── procedures/
│   ├── Oracle_procedures.sql
│   └── Postgresql_procedures.sql
├── AGENTS.md
├── docker-compose.yml
├── pom.xml
└── README.md
```

Os nomes e a capitalização acima representam os arquivos da versão original. Em ambientes Linux, letras maiúsculas e minúsculas fazem diferença nos caminhos dos arquivos.

## 4. Modelo de dados

O banco possui as seguintes tabelas:

| Tabela | Finalidade |
|---|---|
| `aluno` | Armazena os dados cadastrais dos alunos |
| `plano` | Armazena os planos oferecidos pela academia |
| `modalidade` | Registra as modalidades disponíveis |
| `matricula` | Relaciona o aluno ao plano contratado |
| `matricula_modalidade` | Relaciona matrículas às modalidades |
| `pagamento` | Registra as mensalidades e seus pagamentos |
| `frequencia` | Registra as entradas dos alunos na academia |
| `auditoria_matricula` | Guarda alterações de status das matrículas |

O relacionamento entre as tabelas é garantido por chaves primárias e estrangeiras. Também existem restrições para validar informações como valores positivos, duração dos planos e estados permitidos para matrículas e pagamentos.

**Observação sobre o escopo:** embora a proposta inicial mencione professores, turmas, inscrições e avaliações físicas, essas entidades não aparecem no DDL original analisado. Portanto, não são descritas aqui como funcionalidades já implementadas.

## 5. Regras de negócio e objetos do banco

### Procedures

Os arquivos `procedures/Postgresql_procedures.sql` e `procedures/Oracle_procedures.sql` contêm as procedures:

- `registrar_pagamento`: recebe o identificador de um pagamento e verifica sua situação;
- `cancelar_matricula`: altera o status da matrícula para cancelada, registra a data de encerramento e bloqueia a matrícula.

### Triggers

Os mesmos arquivos de procedures também definem triggers:

- `trg_validar_frequencia`: impede o registro de frequência quando a matrícula não está ativa ou está bloqueada. No Oracle, também verifica pagamentos em atraso há mais de dez dias;
- `trg_auditar_matricula`: registra alterações no status da matrícula na tabela `auditoria_matricula`.

### Índices

Os índices estão separados nos arquivos:

- `postgresql/Indices.sql`;
- `oracle/indices.sql`.

Eles foram definidos para apoiar consultas e relacionamentos envolvendo matrículas, pagamentos e frequências.

## 6. Consultas SQL

O projeto contém dez consultas SQL em cada banco.

No PostgreSQL, elas estão em `postgresql/Consultas.sql`. No Oracle, estão em `oracle/consultas.sql`.

As consultas trabalham com:

1. JOIN entre alunos, matrículas e planos;
2. JOIN entre alunos e modalidades;
3. agregação e contagem de matrículas por plano;
4. agregação com `HAVING`;
5. função de janela `RANK()` para ranking de frequência;
6. função de janela `SUM()` para acumulado de pagamentos;
7. consulta anti-join com `NOT EXISTS`;
8. subconsulta para pagamentos acima da média;
9. identificação de mensalidades atrasadas há mais de dez dias;
10. cálculo da receita recebida por competência.

## 7. Aplicação Java

A aplicação do projeto foi desenvolvida em Java. Não há módulo de aplicação em Python. A aplicação principal está localizada em:

```text
java/InovaFit/src/main/java/com/example/inovafit.java
```

O nome original da classe pública é `Inovafit`, dentro do pacote `com.example`.

A aplicação apresenta um menu para escolher entre PostgreSQL e Oracle. Depois da conexão, permite:

- consultar alunos com matrícula ativa e suas informações de plano;
- informar o identificador de um pagamento para chamar a procedure `registrar_pagamento`;
- retornar à seleção do banco ou encerrar o programa.

As conexões utilizam variáveis de ambiente, com valores padrão destinados ao ambiente local de laboratório:

| Variável | Valor padrão |
|---|---|
| `PG_URL` | `jdbc:postgresql://localhost:5432/inovafit` |
| `PG_USER` | `inovafit` |
| `PG_PASSWORD` | `inovafit_lab` |
| `ORA_URL` | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| `ORA_USER` | `inovafit` |
| `ORA_PASSWORD_APP` | `inovafit_lab` |

O arquivo `pom.xml` define Java 21 e as dependências JDBC do PostgreSQL e do Oracle.

> **Atenção à compilação:** na versão original, o arquivo chama-se `inovafit.java`, mas a classe pública chama-se `Inovafit`. Em Java, o nome do arquivo de uma classe pública deve corresponder exatamente ao nome da classe. Esse ponto deve ser conferido caso a compilação apresente erro. Este README mantém os nomes originais, conforme solicitado.

## 8. Análise de desempenho

A pasta `performance/` contém os arquivos utilizados na preparação e execução da análise de desempenho:

- `MedirPerformance.java`: classe principal que inicia os testes nos dois bancos;
- `Motor.java`: classe de apoio para a execução dos testes;
- `PostgreSQL.java`: implementação dos testes no PostgreSQL;
- `Oracle.java`: implementação dos testes no Oracle;
- `Postgresql_dados.sql`: script de carga de dados para PostgreSQL;
- `Oracle_dados.sql`: script de carga de dados para Oracle.

Os scripts de carga e as classes devem ser revisados e executados nos respectivos bancos antes de registrar resultados. Os tempos de execução e os ganhos de desempenho não devem ser informados sem medições reais.

## 9. Como configurar o ambiente

### Pré-requisitos

- Docker;
- Docker Compose;
- Java 21, para compilar e executar a aplicação;
- Maven, para gerenciar dependências e compilar/executar a aplicação;
- Cliente SQL, como DBeaver ou SQL Developer, opcional.

### Subir os bancos

Na pasta raiz do projeto, execute:

```bash
docker compose up -d
```

Verifique os serviços:

```bash
docker compose ps
```

Para acompanhar os logs:

```bash
docker compose logs -f postgres
docker compose logs -f oracle
```

### Acessar os bancos

**PostgreSQL**

- Host: `localhost`
- Porta: `5432`
- Banco: `inovafit`
- Usuário: `inovafit`
- Senha: `inovafit_lab`

**Oracle**

- Host: `localhost`
- Porta: `1521`
- Serviço: `XEPDB1`
- Usuário da aplicação: `inovafit`
- Senha da aplicação: `inovafit_lab`

As credenciais são destinadas ao ambiente acadêmico local. Não devem ser reutilizadas em produção.

### Executar a aplicação Java

Na raiz do projeto, com os bancos disponíveis e Maven instalado:

```bash
mvn compile
mvn exec:java
```

A classe principal configurada no `pom.xml` é `com.example.Inovafit`.

### Encerrar os serviços

```bash
docker compose down
```

Para remover também os volumes e os dados persistidos dos bancos:

```bash
docker compose down -v
```

> O comando `down -v` apaga os dados armazenados nos volumes. Utilize-o somente se desejar reiniciar os bancos do zero.

## 10. Pontos que precisam de atenção

Esta documentação preserva a estrutura original e não significa que todos os itens abaixo já estejam funcionando. Na revisão dos arquivos originais, foram identificados pontos que precisam ser verificados:

- O serviço `testes` do `docker-compose.yml` referencia uma pasta `tests/`, que não aparece na estrutura original do ZIP.
- O script de inicialização do Oracle utiliza caminhos com nomes como `Ddl.sql` e `Inserts.sql`, enquanto os arquivos originais estão nomeados `ddl.sql` e `inserts.sql`. Em sistemas sensíveis a maiúsculas e minúsculas, isso pode impedir a inicialização.
- A procedure original `registrar_pagamento` verifica o status e gera erros para pagamentos pendentes ou atrasados, mas não executa a atualização do pagamento para `PAGO`.
- Os scripts originais de carga de dados em `performance/` apresentam inconsistências de nomes de tabelas, colunas e comandos SQL e precisam ser corrigidos antes de uma medição confiável.
- A classe `Inovafit` e o arquivo `inovafit.java` possuem capitalização diferente, o que pode causar erro de compilação.
- Não foram encontrados no DDL original as tabelas de professores, turmas, inscrições e avaliações físicas mencionadas na descrição inicial.
- É necessário executar os scripts nos dois bancos e registrar evidências dos resultados para confirmar a compatibilidade.

## 11. Considerações finais

O InovaFit demonstra o uso de recursos de banco de dados em um cenário de gerenciamento de academia, incluindo modelagem relacional, consultas SQL, procedures, triggers, índices e integração com Java por JDBC.

A existência de scripts separados para PostgreSQL e Oracle permite comparar as particularidades de cada SGBD e observar como regras de negócio podem ser implementadas diretamente no banco de dados.

O projeto deve ser considerado concluído após a revisão dos pontos pendentes, execução dos scripts nos dois bancos e validação da aplicação.

## Licença

Projeto desenvolvido para fins acadêmicos.
