#!/usr/bin/env bash

# Copy the DAVe binaries
cp -r -v ./target/dave-1.0-SNAPSHOT/dave-1.0-SNAPSHOT ./dockerfile/dave-1.0-SNAPSHOT

# Delete the prefilled
rm -r ./dockerfile/dave-1.0-SNAPSHOT/etc/dave.json ./dockerfile/dave-1.0-SNAPSHOT/etc/*.keystore ./dockerfile/dave-1.0-SNAPSHOT/etc/*.truststore ./dockerfile/dave-1.0-SNAPSHOT/etc/truststore
docker login -e="$DOCKER_EMAIL" -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker build -t scholzj/dave:${CIRCLE_SHA1} ./dockerfile/
docker tag -f scholzj/dave:${CIRCLE_SHA1} docker.io/scholzj/dave:${CIRCLE_SHA1}
docker push scholzj/dave:${CIRCLE_SHA1}
docker tag -f scholzj/dave:${CIRCLE_SHA1} docker.io/scholzj/dave:${CIRCLE_BRANCH}
docker push scholzj/dave:${CIRCLE_BRANCH}
