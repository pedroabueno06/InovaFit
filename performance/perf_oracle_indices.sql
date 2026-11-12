-- ============================================================
-- performance/perf_oracle_indices.sql
--
-- Cria os TRÊS ÍNDICES DO EXPERIMENTO no usuário/schema
-- INOVAFIT_PERF. Nenhum objeto do schema inovafit é alterado.
--
-- Executar SOMENTE entre as fases "antes" e "depois" da medição:
--   1) Medir "antes" (sem estes índices);
--   2) Executar este script (conectado como INOVAFIT_PERF);
--   3) Medir "depois".
--
-- A criação é protegida para tolerar reexecução (ORA-00955,
-- nome já utilizado). Ainda assim, o índice só deve existir na
-- fase "depois".
--
-- Os três índices:
--   1) pagamento(id_matricula)
--      -> índice da FK do JOIN pagamento -> matricula
--         (Consulta 1). O Oracle não cria índices de FK
--         automaticamente;
--   2) matricula(id_aluno)
--      -> índice da FK do JOIN matricula -> aluno (Consulta 1).
--         Mesmo motivo;
--   3) pagamento(status, vencimento)
--      -> índice de busca da Consulta 2: igualdade em status
--         ('ATRASADO') seguida de range em vencimento. A coluna
--         de igualdade lidera a chave, o range fica por último.
--
-- Observação: NÃO há índice aluno(email) aqui — a busca por
-- e-mail na Consulta 1 já é atendida pelo índice único existente
-- (uk_aluno_email); um índice adicional seria redundante.
-- ============================================================

DECLARE
    PROCEDURE criar_ou_ignorar(p_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
    EXCEPTION
        WHEN OTHERS THEN
            -- ORA-00955: name is already used by an existing object
            IF SQLCODE <> -955 THEN
                RAISE;
            END IF;
    END;
BEGIN
    criar_ou_ignorar('CREATE INDEX ix_pagamento_id_matricula ON pagamento (id_matricula)');
    criar_ou_ignorar('CREATE INDEX ix_matricula_id_aluno ON matricula (id_aluno)');
    criar_ou_ignorar('CREATE INDEX ix_pagamento_status_vencimento ON pagamento (status, vencimento)');
END;


-- Recoleta estatísticas após a criação dos índices
BEGIN
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'MATRICULA');
    DBMS_STATS.GATHER_TABLE_STATS('INOVAFIT_PERF', 'PAGAMENTO');
END;

