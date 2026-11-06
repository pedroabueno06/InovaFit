set - euo pipefail

export NLS_LANG=.AL32UTF8

CONNECTION="%APP_USER/$APP_USER_PASSWORD@//localhost/XEPDB1"

rodar() {
    sqlplus -s "$CONNECTION" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
WHENEVER OSERROR EXIT FAILURE
SET DEFINE OFF
@$1
EXIT
SQL
}

rodar /scripts/oracle/01_ddl.sql
rodar /scripts/oracle/02_inserts.sql
rodar /scripts/procedures/oracle_procedures.sql
rodar /scripts/oracle/04_indices.sql

erros=$(sqlplus -s -L "$CONNECTION" <<SQL
SET HEADING OFF FEEDBACK OFF PAGESIZE 0
SELECT COUNT(*) FROM user_errors;
EXIT
SQL
)

if [ "$(echo "$erros" | tr -d '[:space:]')" != "0" ]; then
    echo "ERRO: objetos PL/SQL com erro de compilação" >&2
    exit 1
fi

echo "FitControl: Oracle inicializado com sucesso."