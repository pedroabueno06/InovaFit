#Roda uma única vez, na primeira vez em que o container subir.
set -euo pipefail

PSQL="psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER --dbname $POSTGRES_DB"

$PSQL -f /scripts/postgresql/ddl.sql
$PSQL -f /scripts/postgresql/inserts.sql
$PSQL -f /scripts/procedures/postgresql_procedures.sql
$PSQL -f /scripts/postgresql/indices.sql

echo "InovaFit: Banco de dados PostgreSQL inicializado com sucesso."