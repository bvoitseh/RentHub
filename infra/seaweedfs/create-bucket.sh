#!/bin/sh
# Creates the photos bucket and the private chat bucket (D-57) in the SeaweedFS container started by docker-compose
# (service "s3") and uploads the demo photos from infra/seed/photos to the photos bucket.
# Safe to re-run: an existing bucket counts as success. Retries while the server is still starting.
photos="${S3_BUCKET:-renthub-photos}"
chat="${S3_CHAT_BUCKET:-renthub-chat}"
create() {
  for attempt in $(seq 1 30); do
    out=$(echo "s3.bucket.create -name $1" | weed shell -master=s3:9333 2>&1)
    if echo "$out" | grep -qE 'created bucket|already exists'; then
      echo "bucket $1 ready"
      return 0
    fi
    sleep 2
  done
  echo "$out"
  return 1
}
create "$photos" || exit 1
create "$chat" || exit 1
# Placeholder photos referenced by infra/seed/demo-data.sql (overwritten on every run, harmless).
if ls /seed-photos/*.png >/dev/null 2>&1; then
  weed filer.copy /seed-photos/*.png "http://s3:8888/buckets/$photos/seed/" >/dev/null
  echo "demo photos uploaded to $photos/seed/"
fi
