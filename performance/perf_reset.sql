-- DESCARTE COMPLETO E IRREVERSÍVEL do ambiente de desempenho.
-- Remove APENAS dados/objetos fictícios de performance:
--   * PostgreSQL: schema "performance" (e tudo dentro dele);
--   * Oracle: usuário/schema INOVAFIT_PERF (e tudo dentro dele).

SELECT 'perf_reset.sql: nenhum comando foi executado; descomente apenas com autorização explícita.' AS aviso;
