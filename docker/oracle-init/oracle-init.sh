#Roda uma única vez, na primeira vez em que o container subir.
set -euo pipefail

export NLS_LANG=.AL32UTF8

CONNECTION="${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/XEPDB1"

rodar() {
sqlplus -s -L "$CONNECTION" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
WHENEVER OSERROR EXIT FAILURE
SET DEFINE OFF
@$1
EXIT
SQL
}

rodar /scripts/oracle/ddl.sql
rodar /scripts/oracle/inserts.sql
rodar /scripts/procedures/oracle_procedures.sql
rodar /scripts/oracle/indices.sql

erros=$(sqlplus -s -L "$CONNECTION" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET HEADING OFF FEEDBACK OFF PAGESIZE 0
SELECT COUNT(*) FROM user_errors;
EXIT
SQL
)

if [ "$(echo "$erros" | tr -d '[:space:]')" != "0" ]; then
echo "ERRO: objetos PL/SQL com erro de compilação" >&2
exit 1
fi

echo "InovaFit: Banco de dados Oracle inicializado com sucesso."