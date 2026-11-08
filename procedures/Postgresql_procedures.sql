 ------------------
| ---PROCEDURES--- |
 ------------------

-- 1 Após o aluno realizar o pagamento, o mesmo é atualizado
-- e a matrícula é ativada, caso esteja bloqueada ou é o primeiro
-- pagamento feito pelo aluno, assim podendo entrar na academia.
CREATE OR REPLACE PROCEDURE registrar_pagamento(p_id_pagamento BIGINT)
    LANGUAGE plpgsql AS $$
    DECLARE v_id_matricula BIGINT;
BEGIN
    SELECT id_matricula INTO v_id_matricula
    FROM pagamento
    WHERE id_pagamento=p_id_pagamento 
    FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION USING ERRCODE='P1001', MESSAGE='PAGAMENTO_NAO_ENCONTRADO';
    END IF;
    UPDATE pagamento pg
    SET status='PAGO', data_pagamento=CURRENT_DATE,
        valor=CASE
            WHEN m.status='ATIVA'
                    AND m.data_inicio <= CURRENT_DATE - INTERVAL '12 months'
            THEN pl.valor_mensal * 0.90
            ELSE pg.valor
        END
    FROM matricula m
    JOIN plano pl 
        ON pl.id_plano=m.id_plano
    WHERE pg.id_pagamento=p.id_pagamento
        AND pg.id_matricula=m.id_matricula
        AND pg.status<>'PAGO';
    IF NOT FOUND THEN
        RAISE EXCEPTION USING ERRCODE='P1002', MESSAGE='PAGAMENTO_JA_CONFIRMADO';
    END IF;
    UPDATE matricula SET bloqueada=false WHERE id_matricula
        WHERE id_matricula=v_id_matricula;
    END;
    $$;

-- 2 Após ter a matrícula cancelada, a mesma tem seu estado alterado e vigência encerrada
CREATE OR REPLACE PROCEDURE cancelar_matricula(p_id_matricula BIGINT)
    LANGUAGE plpgsql AS $$
BEGIN
    UPDATE matricula
        SET status='CANCELADA', data_fim=CURRENT_DATE
    WHERE id_matricula=p_id_matricula
        AND status='ATIVA';
    IF NOT FOUND 
        THEN RAISE EXCEPTION USING ERRCODE='P1003',
        MESSAGE='MATRICULA_ATIVA_NAO_ENCONTRADA';
    END IF;
    UPDATE matricula
        SET bloqueada=TRUE
    WHERE id_matricula=p.id_matricula;
    END;
    $$;

 ------------------
|  ---TRIGGERS---  |
 ------------------

-- 1 Impede que o aluno tenha sua frequência registrada na academia
-- sem ter sua matrícula ativa, se estiver bloqueada ou tendo sua mensalidade
-- atrasada em mais de 10 dias
CREATE OR REPLACE FUNCTION fn_validar_frequencia()
    RETURNS trigger
    LANGUAGE psgsql
    AS $$
BEGIN
    UPDATE matricula
        SET status='CANCELADA', data_fim=CURRENT_DATE
    WHERE id_matricula=p_id_matricula
        AND status='ATIVA';
    IF NOT EXISTS (
        SELECT 1 FROM matricula m
    WHERE m.id_matricula=NEW.id_matricula
        AND m.status='ATIVA'
        AND m.bloqueada=FALSE
    ) THEN 
        RAISE EXCEPTION USING ERRCODE='P2001', MESSAGE='MATRICULA_INATIVA_OU_BLQUEADA';
    END IF;
    RETURN NEW;
    END;
    $$;
CREATE TRIGGER trg_validar_frequencia
    BEFORE INSERT ON frequencia
    FOR EACH ROW EXECUTE FUNCTION fn_validar_frequencia();

-- 2 Auditoria para realizar as alterações
-- de estado da matrícula de forma automática
CREATE  OR REPLACE FUNCTION fn_auditar_matricula()
    RETURNS trigger
    LANGUAGE psgsql
    AS $$
BEGIN
    IF OLD.status
        IS DISTINCT FROM NEW.status THEN
        INSERT INTO auditoria_matricula(id_matricula,status_anterior,status_novo)
            VALUES(NEW.id_matricula,OLD.status,NEW.status);
    END IF;
    RETURN NEW;
    END;
    $$
CREATE TRIGGER trg_auditar_matricula
    AFTER UPDATE OF status
    ON matricula
    FOR EACH ROW EXECUTE fn_auditar_matricula();