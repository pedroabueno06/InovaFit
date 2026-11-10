# InovaFit

Sistema acadêmico de gerenciamento de academia, desenvolvido para a disciplina de Banco de Dados. O projeto demonstra modelagem relacional, constraints, consultas SQL, procedures, triggers, índices e acesso JDBC em dois SGBDs: PostgreSQL 17 e Oracle Database XE 21c.

## Objetivos

- Gerenciar alunos, planos, modalidades e matrículas.
- Registrar pagamentos e controlar inadimplência.
- Registrar frequências respeitando as regras de matrícula e pagamento.
- Auditar mudanças de status das matrículas.
- Implementar o mesmo domínio em PostgreSQL e Oracle.
- Comparar planos e tempos de consultas com e sem índices.

## Tecnologias

| Tecnologia | Uso |
|---|---|
| PostgreSQL 17 | Banco relacional e PL/pgSQL |
| Oracle Database XE 21c | Banco relacional e PL/SQL |
| Java 21 | Aplicação de console e medição de desempenho |
| Python 3.12 | Aplicação de console alternativa |
| JDBC | Conexão com os bancos |
| Maven | Compilação e dependências |
| Docker Compose | Ambiente local dos bancos e execução da aplicação/testes |

## Modelo de dados

- `aluno`: dados cadastrais e e-mail único.
- `plano`: nome, valor, duração e situação.
- `modalidade`: modalidades oferecidas.
- `matricula`: vínculo do aluno com um plano, período, status e bloqueio.
- `matricula_modalidade`: modalidades associadas à matrícula.
- `pagamento`: competência, vencimento, valor, data e status.
- `frequencia`: registros de entrada.
- `auditoria_matricula`: histórico de alterações de status.

### Regras de negócio implementadas

- E-mail de aluno, nome de plano e nome de modalidade são únicos.
- Valores monetários são positivos e duração de plano fica entre 1 e 24 meses.
- Status e datas de matrícula são validados por constraints.
- Um aluno não pode ter duas matrículas `ATIVA` simultaneamente.
- Uma modalidade não pode se repetir na mesma matrícula.
- Não pode haver dois pagamentos da mesma competência para a mesma matrícula.
- Frequência só é permitida para matrícula ativa, desbloqueada e sem pagamento não quitado vencido há mais de 10 dias.
- Alterações no status da matrícula são registradas em `auditoria_matricula`.

## Procedures e triggers

Scripts específicos de cada banco estão em `procedures/`.

### Procedures

- `registrar_pagamento`: bloqueia a linha do pagamento durante a operação; confirma pagamentos `PENDENTE` ou `ATRASADO`; registra `data_pagamento`; impede confirmação duplicada; e desbloqueia matrícula ativa bloqueada somente se não houver outra dívida com mais de 10 dias de atraso.
- `cancelar_matricula`: cancela somente matrícula ativa, define `data_fim` e bloqueia a matrícula.

As procedures não executam `COMMIT`; a transação fica sob controle da aplicação ou do chamador.

### Triggers

- `trg_validar_frequencia`: aplica as regras de acesso antes de inserir uma frequência.
- `trg_auditar_matricula`: registra a mudança de status da matrícula.

## Consultas e índices

As dez consultas analíticas estão em `postgresql/Consultas.sql` e `oracle/consultas.sql`. Elas cobrem JOIN, agregação, HAVING, funções de janela, anti-join, subconsulta, pagamentos em atraso e receita por competência.

Os índices de apoio estão em `postgresql/Indices.sql` e `oracle/indices.sql`. O índice de unicidade de matrícula ativa é criado no DDL e faz parte da integridade do modelo; não deve ser removido durante os testes de desempenho.

## Aplicação Java

Código em `java/InovaFit/src/main/java/com/example/Inovafit.java`.

O menu permite escolher PostgreSQL ou Oracle, consultar alunos com matrícula ativa e confirmar pagamento pelo ID. A aplicação usa `PreparedStatement`/`CallableStatement`, `commit` e `rollback`.

Variáveis de ambiente:

| Variável | Padrão local |
|---|---|
| `PG_URL` | `jdbc:postgresql://localhost:5432/inovafit` |
| `PG_USER` | `inovafit` |
| `PG_PASSWORD` | `inovafit_lab` |
| `ORA_URL` | `jdbc:oracle:thin:@//localhost:1521/XEPDB1` |
| `ORA_USER` | `inovafit` |
| `ORA_PASSWORD_APP` | `inovafit_lab` |

## Aplicação Python

A aplicação de console em `python/inovafit.py` oferece as mesmas operações principais da versão Java: consulta de matrículas ativas e confirmação de pagamento. Utiliza `psycopg` no PostgreSQL e `python-oracledb` no Oracle. Para executar localmente, instale `python/requirements.txt` e rode `python python/inovafit.py`. Pelo Docker: `docker compose --profile python run --rm app-python`.

## Diagrama entidade-relacionamento

O diagrama das oito tabelas existentes está em `der/DER.md` (formato Mermaid). Ele documenta o esquema implementado, sem representar módulos ainda ausentes.

## Estrutura

```text
InovaFit-master/
├── docker-compose.yml
├── docker/
│   ├── postgres-init/postgres-init.sh
│   └── oracle-init/oracle-init.sh
├── java/InovaFit/src/main/java/com/example/Inovafit.java
├── python/            # Cliente Python e dependências
├── der/DER.md         # Diagrama ER do esquema implementado
├── postgresql/       # DDL, inserts, consultas e índices
├── oracle/           # DDL, inserts, consultas e índices
├── procedures/       # Procedures e triggers dos dois bancos
├── performance/      # Java, carga de dados e instruções de medição
├── tests/run_tests.sh
├── pom.xml
└── README.md
```

## Como executar

### Pré-requisitos

Docker Desktop com Docker Compose. Para execução fora do Docker: Java 21 e Maven.

### 1. Subir os bancos

Na raiz do projeto:

```bash
docker compose up -d
```

A inicialização executa DDL, dados básicos, procedures/triggers e índices. Aguarde os serviços ficarem saudáveis.

### 2. Executar a aplicação

```bash
docker compose --profile apps run --rm app-java
```

Para compilar/executar localmente, com os dois bancos disponíveis:

```bash
mvn compile
mvn exec:java
```

### 3. Executar testes automatizados

```bash
docker compose --profile test run --rm testes
```

A suíte PostgreSQL verifica a estrutura essencial, frequência válida, rejeição de matrícula cancelada, confirmação de pagamento, auditoria de cancelamento e a regra de inadimplência no limite de 10/11 dias. Os testes rodam em transação revertida para preservar os dados iniciais. A inicialização do Oracle verifica erros de compilação PL/SQL. Para o teste funcional Oracle, conecte como `inovafit` e execute `@tests/oracle_smoke_tests.sql`; o script também reverte as alterações ao final.

### 4. Executar a aplicação Python

```bash
docker compose --profile python run --rm app-python
```

Para execução local, instale as dependências com `pip install -r python/requirements.txt` e execute `python python/inovafit.py`.

### 5. Encerrar

```bash
docker compose down
```

Para apagar também os volumes e os dados dos bancos de laboratório:

```bash
docker compose down -v
```

As credenciais incluídas são exclusivamente para laboratório; não as utilize em produção.

## Análise de desempenho

Consulte `performance/README.md`. Os scripts de carga preparam 30.000 pagamentos para um aluno exclusivo de teste. A aplicação Java executa dez aquecimentos e cem medições por consulta e apresenta a mediana. Os scripts `indices_antes_*.sql` removem somente índices secundários e preservam o índice de integridade. O roteiro documenta a comparação com e sem índices; registre os dados em `performance/explain_antes_depois.txt`.

**Não há resultados de tempo ou percentuais de ganho inventados neste README.** Execute as medições nos dois bancos, salve a saída e registre hardware, versões, volume de dados e planos de execução antes de preencher a análise do relatório.

## Pendências para a entrega acadêmica

1. Executar `docker compose up -d` em uma máquina com Docker e confirmar a inicialização limpa dos dois bancos.
2. Executar `docker compose --profile test run --rm testes` e guardar a saída como evidência.
3. Validar manualmente no Oracle os cenários de pagamento, frequência e auditoria; a suíte automatizada incluída cobre PostgreSQL.
4. Executar a carga e as medições de desempenho em ambos os bancos e preencher o relatório com resultados reais.
5. Conferir se o escopo exigido pelo enunciado inclui módulos como turmas, inscrições e avaliações físicas. Esses módulos não constam nos DDLs recebidos e não foram inventados nesta correção.
6. Completar o relato de uso de IA com as ferramentas efetivamente usadas pelo grupo, prompts/atividades, verificações realizadas e limitações percebidas. Não inventar esse relato.

## Licença

Projeto acadêmico. Não foi encontrado arquivo de licença no material recebido.
