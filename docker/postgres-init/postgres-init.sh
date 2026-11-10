set -euo pipefail

PSQL="psql -v ON_ERROR_STOP=1 --username $POSTGRES_USER --dbname $POSTGRES_DB"

$PSQL -f /scripts/postgresql/Ddl.sql
$PSQL -f /scripts/postgresql/Inserts.sql
$PSQL -f /scripts/procedures/Postgresql_procedures.sql
$PSQL -f /scripts/postgresql/Indices.sql

echo "InovaFit: Banco de dados PostgreSQL inicializado com sucesso."