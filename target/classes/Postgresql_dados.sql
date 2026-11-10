-- São inseridas cargas de dados para testar a performace do nosso projeto "InovaFit"

-- Cadastro de aluno
INSERT INTO aluno (nome,email,data_nascimento)
    VALUES ('Miguel Ascenço Bueno', 'ascencobuenomiguel@gmail.com', DATE '2014-10-16')
    ON CONFLICT (email) DO NOTHING;

-- Cria uma matrícula exclusiva para o aluno cadastrado
INSERT INTO matricula (id_aluno, id_plano, data_inicio, data_fim, status, bloqueda)
    SELECT a.id_aluno, p.id_plano,
        CURRENT_DATE  - 30,
        CURRENT_DATE,
        'CANCELADA'
        FALSE
    FROM aluno a
    CROSS JOIN plano p
WHERE a.email = 'ascencobuenomiguel@gamil.com'
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
WHERE am.email = 'ascencobuenomiguel@gmail.com'
);

-- São inseridos 30 mil pagamentos
INSERT INTO pagamento (id_matricula,competencia,vencimento,data_pagamento,valor,status)
    SELECT 
        m.id_matricula,
        (DATE_TRUNC('month', CURRENT_DATE)
            - (n || ' months')::INTERVAL)::DATE,
        (DATE_TRUNC('month', CURRENT_DATE)
            - (n || ' months')::INTERVAL)::DATE + 5,

        CASE
            WHEN MOD(n, 4) = 0 THEN
                (DATE_TRUNC('month', CURRENT_DATE)
                    -(n || 'months')::INTERVAL)::DATE + 2
            ELSE NULL
        END,

        120.00,

        CASE
            WHEN MOD(n, 4) = 0 THEN 'PAGO'
            WHEN MOD(n, 4) = 1 THEN 'ATRASADO'
            ELSE 'PENDENTE'
        END
    
    FROM generate_series(1, 30000) AS n
    CROSS JOIN matricula m
    JOIN aluno a ON a.id_aluno = m.id_aluno
    WHERE a.email = 'ascencobuenomiguel@gamil.com';

-- Realiza uma atualização nas estatísticas do banco de dados
ANALYZE aluno;
ANALYZE matricula;
ANALYZE pagamento;

-- Realiza a conferência da quantidade registros feitos
SELECT COUNT(*) AS total_pagamentos
FROM pagamento p
JOIN matricula m ON m.id_matricula = p.id_matricula
JOIN aluno a ON a.id_aluno = m.id_aluno
WHERE a.email = 'ascencobuenomiguel@gmail.com'