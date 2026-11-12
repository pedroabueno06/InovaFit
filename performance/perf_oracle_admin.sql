
-- Cria o usuário/schema separado INOVAFIT_PERF, utilizado pela
-- massa de dados e pelos índices de desempenho do InovaFit.
SET VERIFY OFF

-- Usuário de desempenho, isolado do schema da aplicação
CREATE USER INOVAFIT_PERF IDENTIFIED BY "&1"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA 500M ON USERS;

-- Privilégios mínimos: sessão + criação de tabelas/índices
GRANT CREATE SESSION TO INOVAFIT_PERF;
GRANT CREATE TABLE   TO INOVAFIT_PERF;

SET VERIFY ON
