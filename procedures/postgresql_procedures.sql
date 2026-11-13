-- ------------------
--| ---PROCEDURES--- |
-- ------------------

-- 1 Retorna PAGO, PENDENTE OU ATRASADO pelo parâmetro OUT,
-- Se o pagamento não existir, é gerado um erro de negócio.
CREATE OR REPLACE PROCEDURE registrar_pagamento(
    IN p_id_pagamento BIGINT,
    OUT p_status_pagamento VARCHAR(20)
)
LANGUAGE plpgsql
AS $$
BEGIN
    SELECT status
    INTO p_status_pagamento
    FROM pagamento
    WHERE id_pagamento = p_id_pagamento;
 
    IF NOT FOUND THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P1001',
            MESSAGE = 'PAGAMENTO_NAO_ENCONTRADO';
    END IF;
END;
$$;
 
-- 2 Após ter a matrícula cancelada, a mesma tem seu estado alterado e vigência encerrada
CREATE OR REPLACE PROCEDURE cancelar_matricula(p_id_matricula BIGINT)
    LANGUAGE plpgsql
    AS $$
BEGIN
    UPDATE matricula
        SET status='CANCELADA',
        data_fim=CURRENT_DATE,
        bloqueada = TRUE
    WHERE id_matricula = p_id_matricula
        AND status='ATIVA';
 
    IF NOT FOUND THEN
        RAISE EXCEPTION USING 
        ERRCODE='P1003',
        MESSAGE='MATRICULA_ATIVA_NAO_ENCONTRADA';
    END IF;
END;
$$;
 
--  ------------------
-- |  ---TRIGGERS---  |
--  ------------------
 
-- 1 Impede que o aluno tenha sua frequência registrada na academia
-- sem ter sua matrícula ativa, se estiver bloqueada ou tendo sua mensalidade
-- atrasada em mais de 10 dias
CREATE OR REPLACE FUNCTION fn_validar_frequencia()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM matricula m
        WHERE m.id_matricula = NEW.id_matricula
          AND m.status = 'ATIVA'
          AND m.bloqueada = FALSE
    ) THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P2001',
            MESSAGE = 'MATRICULA_INATIVA_OU_BLOQUEADA';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM pagamento p
        WHERE p.id_matricula = NEW.id_matricula
          AND p.status <> 'PAGO'
          AND p.vencimento < CURRENT_DATE - 10
    ) THEN
        RAISE EXCEPTION USING
            ERRCODE = 'P2002',
            MESSAGE = 'ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS';
    END IF;
 
    RETURN NEW;
END;
$$;
 
CREATE TRIGGER trg_validar_frequencia
    BEFORE INSERT ON frequencia
    FOR EACH ROW
EXECUTE FUNCTION fn_validar_frequencia();
 
-- 2 Auditoria para realizar as alterações
-- de estado da matrícula de forma automática
CREATE OR REPLACE FUNCTION fn_auditar_matricula()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.status IS DISTINCT FROM NEW.status THEN
        INSERT INTO auditoria_matricula (
            id_matricula,
            status_anterior,
            status_novo
        )
        VALUES (
            NEW.id_matricula,
            OLD.status,
            NEW.status
        );
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_auditar_matricula
    AFTER UPDATE OF status ON matricula
    FOR EACH ROW
EXECUTE FUNCTION fn_auditar_matricula();
