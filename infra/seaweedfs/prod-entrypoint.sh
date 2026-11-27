#!/bin/sh
# Production SeaweedFS (D-47): writes the S3 identities from S3_ACCESS_KEY / S3_SECRET_KEY (never committed) and
# starts the server. Anonymous users may only read, so photos can be served publicly through Nginx at /photos/.
set -eu
: "${S3_ACCESS_KEY:?S3_ACCESS_KEY is required}"
: "${S3_SECRET_KEY:?S3_SECRET_KEY is required}"
cat > /tmp/s3.json <<EOF
{
  "identities": [
    {
      "name": "renthub",
      "credentials": [{ "accessKey": "${S3_ACCESS_KEY}", "secretKey": "${S3_SECRET_KEY}" }],
      "actions": ["Admin", "Read", "List", "Write", "Tagging"]
    },
    { "name": "anonymous", "actions": ["Read"] }
  ]
}
EOF
exec weed server -dir=/data -s3 -s3.port=8333 -s3.config=/tmp/s3.json
