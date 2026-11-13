-- Cria os TRÊS ÍNDICES DO EXPERIMENTO no schema "performance".
-- Nenhum objeto do schema public é alterado.

-- Executar SOMENTE entre as fases "antes" e "depois" da medição:
--   1) Medir "antes" (sem estes índices);
--   2) Executar este script;
--   3) Medir "depois".

-- índice da FK do JOIN pagamento:
-- matricula (Consulta 1). O PostgreSQL não cria índices de FK
--  automaticamente;
CREATE INDEX IF NOT EXISTS ix_pagamento_id_matricula
    ON performance.pagamento (id_matricula);

-- índice da FK do JOIN matricula -> aluno (Consulta 1).
CREATE INDEX IF NOT EXISTS ix_matricula_id_aluno
    ON performance.matricula (id_aluno);

-- índice de busca da Consulta 2: igualdade em status
-- ('ATRASADO') seguida de range em vencimento. A coluna
-- de igualdade lidera a chave, o range fica por último.
CREATE INDEX IF NOT EXISTS ix_pagamento_status_vencimento
    ON performance.pagamento (status, vencimento);

-- Recoleta estatísticas após a criação dos índices
ANALYZE performance.pagamento;
ANALYZE performance.matricula;
