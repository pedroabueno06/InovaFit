 ------------------
| ---PROCEDURES--- |
 ------------------

-- 1 Após o aluno realizar o pagamento, o mesmo é atualizado
-- e a matrícula é ativada, caso esteja bloqueada ou é o primeiro
-- pagamento feito pelo aluno, assim podendo entrar na academia.
CREATE OR REPLACE PROCEDURE registrar_pagamento(p_id_pagamento IN NUMBER)
    AS v_id_matricula NUMBER;
    v_data_inicio DATE;
    V_VALOR_PLANO NUMBER(10,2);
    v_status VARCHAR(20);
BEGIN
    SELECT pg.id_matricula,m.data_inicio,pl.valor_mensal,m.status
        INTO v_id_matricula,v_data_inicio,v_valor_plano,v_status
        FROM pagamento pg
        JOIN matricula m
        ON m.id_matricula=pg.id_matricula
        JOIN plano pl
        ON pl.id_plano=m.id_planno
    WHERE pg.id_pagamento=p_id_pagamento
        FOR UPDATE OF pg.status;
        UPDATE pagamento
            SET status='PAGO', data_pagamento=TRUNC(SYSDATE),
                valor=CASE WHEN v_status='ATIVA'
            AND ADD_MONTHS(v_data_inicio,12)<=TRUNC(SYSDATE)
            THEN v_valor_plano*0.90
            ELSE valor
        END
    WHERE id_pagamento=p_id_pagamento
        AND status<>'PAGO';
    IF SQL%ROWCOUNT=0 THEN
        RAISE_APPLICATION_ERROR(-2002,'PAGAMENTO_JA_CONFIRMADO');
    END IF;
    UPDATE matricula
        SET bloqueada=0
    WHERE id_matricula=v_id_matricula;
        EXCEPTION WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20001,'PAGAMENTO_NAO_ENCONTRADO');
END;
/

-- 2 Após ter a matrícula cancelada, a mesma tem seu estado alterado e vigência encerrada
CREATE OR REPLACE PROCEDURE cancelar_matricula(p_id_matricula IN NUMBER) 
    AS
BEGIN
    UPDATE matricula
        SET status='CANCELADA', data_fim=TRUNC(SYSDATE), bloqueda=1
    WHERE id_matricula=p_id_matricula
        AND status='ATIVA';
    IF SQL%ROWCOUNT=0 THEN
        RAISE_APPLICATION_ERROR(-20003,'MATRICULA_ATIVA_NAO_ENCONTRADA');
    END IF;
END;
/

 ------------------
|  ---TRIGGERS---  |
 ------------------

-- 1 Impede que o aluno tenha sua frequência registrada na academia
-- sem ter sua matrícula ativa, se estiver bloqueada ou tendo sua mensalidade
-- atrasada em mais de 10 dias
CREATE OR REPLACE TRIGGER trg_validar_frequencia
    BEFORE INSERT ON frequencia
        FOR EACH ROW
    DECLARE v_qtd NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_qtd
        FROM matricula
    WHERE id_matricula=:NEW.id_matricula
        AND status='ATIVA'
        AND bloqueda=0;
    IF v_qtd=0 THEN
        RAISE_APPLICATION_ERROR(-20011,'MATRICULA_INATIVA_OU_BLOQUEADA');
    END IF;
    SELECT COUNT(*) INT v_qtd
        FROM pagamento
    WHERE id_matricula=:NEW.id_matricula
        AND status<>'PAGO'
        AND vencimento<TRUNC(SYSDATE)-10;
    IF v_qtd>0 THEN
        RAISE_APPLICATION_ERROR(-20012,'ALUNO_INADIMPLENTE_HA_MAIS_DE_10_DIAS');
    END IF;
END;
/

-- 2 Auditoria para realizar as alterações de estado
-- de estado da matrícula de forma automática

CREATE OR REPLACE TRIGGER trg_auditar_matricula
    AFTER UPDATE OF status
    ON matricula
    FOR EACH ROW
BEGIN
    IF :OLD.status <> :NEW.status THEN
    INSERT INTO auditoria_matricula(id_matricula,status_anterior,status_novo)
        VALUES(:NEW.id_matricula,:OLD.status,:NEW.status);
    END IF;
END;
/