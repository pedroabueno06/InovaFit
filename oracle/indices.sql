CREATE INDEX ix_matricula_id_plano
    ON matricula(id_plano);

CREATE INDEX ix_pagamento_id_matricula
    ON pagamento(id_matricula);

CREATE INDEX ix_frequencia_id_matricula_data
    ON frequencia(id_matricula,data_hora_entrada);

CREATE INDEX ix_pagamento_vencimento_status
    ON pagamento(vencimento,status);