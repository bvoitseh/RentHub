#!/usr/bin/env bash
# Daily PostgreSQL backup (NFR-2, D-47): pg_dump of the database inside the existing PostgreSQL container,
# gzip-compressed, keeping RETENTION_DAYS days. Settings come from the environment or from the root .env:
#   PG_CONTAINER (container name), PG_DB (database), PG_USER (role), BACKUP_DIR, RETENTION_DAYS (default 14).
# Cron example (03:30 every day):
#   30 3 * * * cd /opt/renthub && ./infra/backup/backup.sh >> /var/log/renthub-backup.log 2>&1
set -euo pipefail

ENV_FILE="${ENV_FILE:-$(dirname "$0")/../../.env}"
if [[ -f "$ENV_FILE" ]]; then
  # Only the backup settings are read from the file; nothing else is exported.
  while IFS='=' read -r key value; do
    case "$key" in
      PG_CONTAINER|PG_DB|PG_USER|BACKUP_DIR|RETENTION_DAYS) [[ -z "${!key:-}" ]] && export "$key=$value" ;;
    esac
  done < <(grep -E '^(PG_CONTAINER|PG_DB|PG_USER|BACKUP_DIR|RETENTION_DAYS)=' "$ENV_FILE")
fi

: "${PG_CONTAINER:?set PG_CONTAINER}"
: "${PG_DB:?set PG_DB}"
: "${PG_USER:?set PG_USER}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

mkdir -p "$BACKUP_DIR"
stamp="$(date +%Y-%m-%d_%H%M%S)"
target="$BACKUP_DIR/${PG_DB}_${stamp}.sql.gz"
partial="$target.partial"

# Plain SQL dump without ownership/privileges, so it restores into any database and role.
docker exec "$PG_CONTAINER" pg_dump -U "$PG_USER" -d "$PG_DB" --no-owner --no-privileges | gzip -9 > "$partial"
mv "$partial" "$target"
echo "$(date -Iseconds) backup written: $target ($(du -h "$target" | cut -f1))"

# Rotation: remove dumps of this database older than RETENTION_DAYS days.
find "$BACKUP_DIR" -maxdepth 1 -type f -name "${PG_DB}_*.sql.gz" -mtime "+$RETENTION_DAYS" -print -delete
