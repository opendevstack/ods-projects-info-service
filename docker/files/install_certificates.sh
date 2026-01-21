#!/bin/sh
set -eu

if [[ -d /tmp/application-certificates ]] ; then
    for file in /tmp/application-certificates/*; do
        if [ -f "$file" ]; then
            filename=$(basename "$file")
            echo "Installing $filename certificate..."
            cp $file /usr/local/share/ca-certificates/${filename}.cer
            keytool -importcert -cacerts -file /usr/local/share/ca-certificates/${filename}.cer -storepass changeit -noprompt -alias ${filename}
            echo "$filename certificate installed."
        fi
    done
    update-ca-certificates 2>/dev/null || true
else
    echo "Application certificates don't exist in the pod. Skipping installation"
fi
exec "$@"