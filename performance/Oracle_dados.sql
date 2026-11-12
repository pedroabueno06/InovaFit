--- São inseridas cargas de dados para testar a performace do nosso projeto "InovaFit"
-- Os dados de cargas de dados podem ser identificados pelo email 'perf<N>@exemplo.test'
-- no qual são todos fictícios, com isso será possível apagar apenas a carga sem mexer nos dados de exemplo.

-- Realiza a remoção de uma carga anterior (pagamento), assim
-- é possível rodar o script mais de uma vez.
DELETE FROM pagamento;
DELETE FROM matricula;
DELETE FROM aluno;

-- São cadastrados de 50 mil alunos.
INSERT INTO aluno (nome, email, data_nascimento)
    SELECT 'Aluno Perf ' || LEVEL, 'perf' || LEVEL || '@exemplo.test',
        DATE '2000-01-01'
    FROM dual
    CONNECT BY LEVEL <= 50000;

--- Cria uma matrícula ativa para cada aluno da carga de dados no plano mensal.
INSERT INTO matricula (id_aluno, id_plano, data_inicio, status, bloqueada)
    SELECT a.id_aluno, p.id_plano, TRUNC(SYSDATE) - 90, 'ATIVA', 0
    FROM aluno a
    CROSS JOIN plano p
    WHERE a.email LIKE 'perf%@exemplo.test'
        AND UPPER(p.nome) = 'MENSAL';

-- São inseridos 150 mil pagamentos, além de 3 competências (do mês atual e 2 anteriores)
-- por matrícula cadastrada. Foi deixado cerca de 1% das matrículas como ATRASADAS,
-- o restante das mesmas foi deixado como PAGO.
INSERT INTO pagamento (id_matricula, competencia, vencimento, data_pagamento, valor, status)
    SELECT x.id_matricula, x.competencia, x.competencia + 5,
        CASE WHEN MOD(x.id_matricula, 100) = 0 THEN NULL ELSE x.competencia + 2 END,
        120,
        CASE WHEN MOD(x.id_matricula, 100) = 0 THEN 'ATRASADO' ELSE 'PAGO' END
    FROM (
        SELECT m.id_matricula,
            ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -k.n) AS competencia
        FROM matricula m
        JOIN aluno a ON a.id_aluno = m.id_aluno
        CROSS JOIN (SELECT LEVEL - 1 AS n FROM dual CONNECT BY LEVEL <= 3) k
        WHERE a.email LIKE 'perf%@exemplo.test'
    ) x;

COMMIT;

-- Realiza uma atualização nas estatísticas do banco de dados.
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'PLANO');
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'ALUNO');
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'MATRICULA');
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'PAGAMENTO');
END;


-- Realiza a conferência da quantidade registros feitos,
-- sendo esperados cerca de 150000 registros feitos.
SELECT 'aluno'     AS tabela, COUNT(*) AS total FROM aluno      WHERE email LIKE 'perf%@exemplo.test'
UNION ALL
SELECT 'matricula' AS tabela, COUNT(*) AS total FROM matricula m
    JOIN aluno a ON a.id_aluno = m.id_aluno
    WHERE a.email LIKE 'perf%@exemplo.test'
UNION ALL
SELECT 'pagamento' AS tabela, COUNT(*) AS total FROM pagamento p
    JOIN matricula m ON m.id_matricula = p.id_matricula
    JOIN aluno a ON a.id_aluno = m.id_aluno
    WHERE a.email LIKE 'perf%@exemplo.test';
