#!/bin/sh
# Production SeaweedFS (D-47): writes the S3 identities from S3_ACCESS_KEY / S3_SECRET_KEY (never committed) and
# starts the server through the image's own /entrypoint.sh, which fixes /data ownership and drops root privileges to
# the seaweed user. Anonymous users may only read the photos bucket (S3_BUCKET), so photos can be served publicly
# through Nginx at /photos/; the chat bucket (D-57) stays private.
set -eu
: "${S3_ACCESS_KEY:?S3_ACCESS_KEY is required}"
: "${S3_SECRET_KEY:?S3_SECRET_KEY is required}"
: "${S3_BUCKET:=renthub-photos}"
umask 022
cat > /tmp/s3.json <<EOF
{
  "identities": [
    {
      "name": "renthub",
      "credentials": [{ "accessKey": "${S3_ACCESS_KEY}", "secretKey": "${S3_SECRET_KEY}" }],
      "actions": ["Admin", "Read", "List", "Write", "Tagging"]
    },
    { "name": "anonymous", "actions": ["Read:${S3_BUCKET}"] }
  ]
}
EOF
exec /entrypoint.sh server -dir=/data -s3 -s3.port=8333 -s3.config=/tmp/s3.json
