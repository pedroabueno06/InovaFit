DECLARE
    PROCEDURE criar_ou_ignorar(p_sql IN VARCHAR2) IS
    BEGIN
        EXECUTE IMMEDIATE p_sql;
    EXCEPTION
        WHEN OTHERS THEN
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

