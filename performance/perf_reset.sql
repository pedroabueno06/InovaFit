-- ============================================================
-- performance/perf_reset.sql
--
-- DESCARTE COMPLETO E IRREVERSÍVEL do ambiente de desempenho.
-- Remove APENAS dados/objetos fictícios de performance:
--   * PostgreSQL: schema "performance" (e tudo dentro dele);
--   * Oracle: usuário/schema INOVAFIT_PERF (e tudo dentro dele).
--
-- NENHUM objeto do schema public/inovafit é afetado.
--
-- ESTE SCRIPT NÃO DEVE SER EXECUTADO SEM AUTORIZAÇÃO EXPLÍCITA.
-- Os comandos estão comentados propositalmente.
-- ============================================================

-- ------------------------------------------------------------
-- PostgreSQL (conectado como inovafit):
-- DROP SCHEMA performance CASCADE;
-- ------------------------------------------------------------

-- ------------------------------------------------------------
-- Oracle (conectado como SYS/SYSTEM no XEPDB1):
-- DROP USER INOVAFIT_PERF CASCADE;
-- ------------------------------------------------------------

SELECT 'perf_reset.sql: nenhum comando foi executado; descomente apenas com autorização explícita.' AS aviso;
