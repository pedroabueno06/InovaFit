CREATE INDEX IF NOT EXISTS ix_matricula_id_plano
    ON matricula(id_plano);

CREATE INDEX IF NOT EXISTS ix_pagamento_id_matricula
    ON pagamento(id_matricula);

CREATE INDEX IF NOT EXISTS ix_frequencia_id_matricula_data
    ON frequencia(id_matricula,data_hora_entrada);

CREATE INDEX IF NOT EXISTS ix_pagamento_vencimento_status
    ON pagamento(vencimento,status);