
# AGENTS.md — InovaFit

## 1. Sobre o projeto

O InovaFit é um projeto acadêmico de banco de dados para gerenciamento de uma academia, desenvolvido para a A3 da disciplina de Banco de Dados (2026/2).

O sistema controla alunos, planos, matrículas, mensalidades, pagamentos, modalidades, inscrições, frequências, avaliações físicas e histórico de alterações das matrículas.

O objetivo é implementar regras de negócio diretamente nos bancos de dados, utilizando recursos de SQL, constraints, procedures, triggers, transações, índices e consultas analíticas.

## 2. Tecnologias utilizadas

- PostgreSQL 17.
- Oracle Database XE 21.
- Java 21.
- JDBC para conexão com os bancos.
- Maven para gerenciamento do projeto Java.
- Docker e Docker Compose.
- OpenCode como agente de IA.
- OpenRouter como provedor do modelo de IA.

## 3. Estrutura do projeto

- `oracle/`: scripts SQL específicos do Oracle.
- `postgresql/`: scripts SQL específicos do PostgreSQL.
- `procedures/`: procedures e triggers dos dois bancos.
- `performance/`: classes Java e scripts SQL utilizados nos testes de desempenho.
- `java/`: aplicação Java com JDBC.
- `docker/`: scripts de inicialização dos bancos.
- `tests/`: testes automatizados.
- `.agents/skills/`: skills utilizadas pelo OpenCode.
- `der/`: arquivos relacionados ao diagrama entidade-relacionamento.
- `docker-compose.yml`: configuração dos serviços Docker.
- `README.md`: documentação do projeto.

## 4. Regras de desenvolvimento

1. Sempre analisar os arquivos existentes antes de propor alterações.
2. Preservar a estrutura e o funcionamento atual do projeto.
3. Não substituir arquivos completos sem necessidade.
4. Explicar as alterações propostas antes de realizá-las.
5. Não inventar tabelas, colunas, relacionamentos ou regras de negócio.
6. Quando houver dúvida sobre uma regra, perguntar antes de implementá-la.
7. Manter os scripts de PostgreSQL e Oracle separados.
8. Utilizar a sintaxe específica de cada SGBD.
9. Não considerar um script correto sem verificar sua execução.
10. Escrever comentários claros nos scripts SQL.

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

1. Explicar o que será executado.
2. Informar quais tabelas serão afetadas.
3. Aguardar autorização do usuário.
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

A etapa de performance deve atender ao critério 5 da A3.

Objetivos:

- Avaliar índices utilizados nas consultas e relacionamentos.
- Preparar uma massa de dados de teste suficiente.
- Coletar planos de execução antes da criação dos índices.
- Criar e testar os índices propostos.
- Coletar novamente os planos depois da criação.
- Comparar os resultados dos dois bancos.

Regras obrigatórias:

1. Nunca inventar tempos de execução.
2. Nunca afirmar que houve melhoria sem evidências.
3. Utilizar os planos reais coletados nos bancos.
4. Manter as condições dos testes antes e depois equivalentes.
5. Separar o tempo de execução do banco do tempo medido pela aplicação Java.
6. Explicar por que cada índice foi escolhido.
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

- Preservar as conexões existentes.
- Utilizar PreparedStatement e parâmetros bind.
- Tratar SQLException adequadamente.
- Não expor credenciais.
- Não remover funcionalidades existentes sem autorização.
- Verificar a compatibilidade com Java 21 e Maven.

## 9. Skills disponíveis

O projeto possui as seguintes skills:

- `grilling`: questionar e revisar decisões técnicas.
- `diagnosing-bugs`: investigar e corrigir problemas.
- `code-review`: revisar código e identificar possíveis falhas.
- `tdd`: orientar a criação e execução de testes.

Utilizar essas skills quando forem pertinentes à tarefa.

## 10. Uso do agente de IA

O agente deve atuar como auxiliar técnico, não como substituto do entendimento do grupo.

Sempre:

- Explicar o motivo das sugestões.
- Apontar possíveis erros e limitações.
- Diferenciar fatos verificados de hipóteses.
- Não inventar resultados de testes.
- Não afirmar que executou comandos sem realmente executá-los.
- Solicitar autorização antes de operações potencialmente destrutivas.

Todo código sugerido pela IA deverá ser revisado e executado pelo grupo nos dois SGBDs, quando aplicável.

## 11. Prioridades da A3

As entregas devem seguir os critérios do enunciado:

1. DER.
2. DDL Oracle e PostgreSQL.
3. Dez consultas SQL nos dois bancos.
4. Duas procedures e uma trigger nos dois bancos.
5. Três índices e planos de execução antes e depois.
6. Conexão Java e Python.
7. README, documentação e registro de uso da IA.

A prioridade é entregar um projeto funcional, testado, organizado e que todos os integrantes consigam explicar na apresentação.
