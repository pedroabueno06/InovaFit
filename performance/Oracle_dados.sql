-- São inseridas cargas de dados para testar a performace do nosso projeto "InovaFit"

-- Cadastro de aluno
MERGE INTO aluno a
USING (
    SELECT 'Miguel Ascenc Bueno' AS NOME,
           'ascencobuenomiguel@gmail.com' AS email
    FROM dual
) dados
ON (a.email = dados.email)
WHEN NOT MATCHED THEN
    INSERT (nome,email,data_nascimento)
        VALUES (dados.nome, dados.email, DATE '2014-10-16');

-- Cria uma matrícula exclusiva para o aluno cadastrado
INSERT INTO matricula (id_aluno,id_plano,data_inicio,data_fim,status,bloqueada)
    SELECT a.id_aluno,p.id_plano,
        TRUNC(SYSDATE) - 30,
        TRUNC(SYSDATE),
        'CANCELADA',
        0
    FROM alunoa
    CROSS JOIN plano p
WHERE a.email = 'ascencobuenomiguel@gmail.com'
    AND p.nome = 'Mensal'
    AND NOT EXISTS (
        SELECT 1
        FROM matricula m
    WHERE m.id_aluno = a.id_aluno
    );

-- Realiza a remoção de pagaemntos anteriores deste aluno
DELETE FROM pagamento
WHERE id_matricula = (
    SELECT m.id_matricula
    FROM matricula m 
    JOIN aluno a ON a.id_aluno = m.id_aluno
    WHERE a.email = 'ascencobuenomiguel@gmail.com'
);

-- São inseridos 30 mil pagamentos
INSERT INTO pagamento (id_matricul,competencia,vencimento,data_pagamento,valor,status)
    SELECT m.id_matricula
        ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -LEVEL),
        ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -LEVEL) + 5,

        CASE
            WHEN MOD(LEVEL, 4) = 0 THEN
                ADD_MONTHS(TRUNC(SYSDATE, 'MM'), -LEVEL) +2
            END NULL
        END,

        10.00,

        CASE
            WHEN MOD(LEVEL, 4) = 0 THEN 'PAGO'
            WHEN MOD(LEVEL, 4) = 1 THEN 'ATRASADO'
            ELSE 'PENDENTE'
        END
    
    FROM dual
    CROS JOIN matricula m
    JOIN aluno a ON a.id_aluno = m.id_aluno
WHERE a.email = 'ascencobuenomiguel@gmail.com'
    CONECT BY LEVEL <= 30000;

-- Realiza uma atualização nas estatísticas do banco de dados
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS(USER, 'ALUNO');
    DBMS_STATS.GATHER_TABLE_STATS(USER, 'MATRICULA');
    DBMS_STATS.GATHER_TABLE_STATS(USER, 'PAGEMENTO');
END;
/
COMMIT;

-- Realiza a conferência da quantidade registros feitos
SELECT COUNT(*) AS total_pagamentos
    FROM pagamentos p
    JOIN matricula m ON m.id_matricula = p.id_matricula
    JOIN aluno a ON a.id_aluno = m.id_aluno
WHERE a.email = 'ascencobuenomigue@gmail.com';