-- Regra de negócio 3 pois localiza as
-- matrículas de determinado aluno.
CREATE INDEX IF NOT EXISTS ix_matricula_id_plano
    ON matricula(id_plano);

-- Regras de negócio 1 e 4 pois localiza os
-- pagamentos que possam estar atrsados em uma matrícula.
CREATE INDEX IF NOT EXISTS ix_pagamento_id_matricula
    ON pagamento(id_matricula);

-- Regras de negócio 1 e 4 pois localiza pagamentos
-- que possam estar atrasados há mais de 10 dias.
CREATE INDEX IF NOT EXISTS ix_pagamento_vencimento_status
    ON pagamento(vencimento,status);
