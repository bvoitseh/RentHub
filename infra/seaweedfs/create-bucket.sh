#!/bin/sh
# Creates the photos bucket in the SeaweedFS container started by docker-compose (service "s3") and uploads the
# demo photos from infra/seed/photos.
# Safe to re-run: an existing bucket counts as success. Retries while the server is still starting.
bucket="${S3_BUCKET:-renthub-photos}"
for attempt in $(seq 1 30); do
  out=$(echo "s3.bucket.create -name $bucket" | weed shell -master=s3:9333 2>&1)
  if echo "$out" | grep -qE 'created bucket|already exists'; then
    echo "bucket $bucket ready"
    # Placeholder photos referenced by infra/seed/demo-data.sql (overwritten on every run, harmless).
    if ls /seed-photos/*.png >/dev/null 2>&1; then
      weed filer.copy /seed-photos/*.png "http://s3:8888/buckets/$bucket/seed/" >/dev/null
      echo "demo photos uploaded to $bucket/seed/"
    fi
    exit 0
  fi
  sleep 2
done
echo "$out"
exit 1
