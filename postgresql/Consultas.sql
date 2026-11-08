-- 1 JOIN: Verificação de alunos e planos que estão ativos
-- Significa que será realizada uma junção entre as tabelas de alunos,
-- matrículas e planos, para verificar quais alunos possuem planos ativos.
SELECT a.nome,p.nome plano,m.status
    FROM aluno a
    JOIN matricula m
    ON m.id_aluno=a.id_aluno
    JOIN plano p
    ON p.id_plano=m.id_plano
    WHERE m.status='ATIVA';

-- 2 JOIN: Listagem de modalidade por aluno
-- Significa que será realizada uma junção entre as tabelas de alunos,
-- matrículas e planos, para verificar quais alunos possuem planos ativos.
SELECT a.nome,mo.nome modalidade
    FROM aluno a
    JOIN matricula m
    ON m.id_aluno=a.id_aluno
    JOIN matricula_modalidade mm
    ON mm.id_matricula=m.id_matricula
    JOIN modalidade mo
    ON mo.id_modalidade=mm.id_modalidade;

-- 3 Agregação: Contagem de matrículas por plano
-- Significa que serão combinadas várias linhas de dados em uma única linha de resultado.
SELECT p.nome,COUNT(m.id_matricula) total
    FROM plano p
    LEFT JOIN matricula m
    ON m.id_plano=p.id_plano
    GROUP BY p.nome;

-- 4 Agregação com Having: planos que possuem matrícula
-- Significa que serão filtrados grupos de dados após o processo de agregação,
-- ou seja, será filtrado o resultado final da contagem de matrícula por plano.
SELECT p.nome,COUNT(*) total
    FROM plano p
    JOIN matricula m
    ON m.id_plano=p.id_plano
    GROUP BY p.nome
    HAVING COUNT(*)>=1;

-- 5 WINDOW: Ranking de frequência dos alunos
--Significa que será realizado um cálculo de linhas de dados em relação a outras linhas de dados.
SELECT a.nome, COUNT(f.id_frequencia) presencas,
    RANK() OVER(ORDER BY COUNT(f.id_frequencia) DESC) posicao
    FROM aluno a
    JOIN matricula m
    ON m.id_aluno=a.id_aluno
    LEFT JOIN frequencia f
    ON f.id_matricula=m.id_matricula
    GROUP BY a.nome;

-- 6 WINDOW: Acúmulo de pagamentos por matrícula
-- Significa que será realizado um cálculo de linhas de dados em relação a outras linhas de dados
SELECT id_matricula,competencia,valor,
    SUM(valor) OVER(PARTITION BY id_matricula ORDER BY competencia) acumulado
    FROM pagamento;

-- 7 ANTI-JOIN: Alunos que não possuem suas matrículas ativas
-- Significa que será realizado uma verificação de linhas de dados que não possuem correspondência em outra tabela.
SELECT a.id_aluno,a.nome
    FROM aluno a
    WHERE NOT EXISTS(
        SELECT 1
        FROM matricula m
        WHERE m.id_aluno=a.id_aluno
        AND m.status='ATIVA'
    );

-- 8 Subconsulta: Pagamentos de mensalidade que estão acima da média
-- Significa que será realizado uma consulta dentro de outra consulta,
-- ou seja, será realizada uma consulta para calcular a média de pagamentos,
-- após isso, será feita outra consulta para buscar os pagamentos que estão acima dessa média.
SELECT id_pagamento,id_matricula,valor
    FROM pagamento
    WHERE valor>(SELECT AVG(valor) FROM pagamento);

-- 9 Alunos que estão a mais de 10 dias com sua mensalidade atrasada
SELECT DISTINCT a.nome,p.vencimento,p.valor
    FROM aluno a
    JOIN matricula m
    ON m.id_aluno=a.id_aluno
    JOIN pagamento p
    ON p.id_matricula=m.id_matricula
    WHERE p.status<>'PAGO'
    AND p.vencimento<CURRENT_DATE-10;

-- 10 Receita recebida por competência
-- Serão feitas as somas dos valores que foram feitos por competência
SELECT competencia,SUM(valor) receita
    FROM pagamento
    WHERE status='PAGO'
    GROUP BY competencia
    ORDER BY competencia;