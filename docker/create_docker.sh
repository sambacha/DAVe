#!/usr/bin/env bash

# Copy the DAVe binaries
cp -r -v ./target/dave-1.0-SNAPSHOT/dave-1.0-SNAPSHOT ./docker/dave-1.0-SNAPSHOT

sed -i 's/sslKey.*/sslKey\ =\ \"\"/' ./docker/dave-1.0-SNAPSHOT/etc/dave.conf
sed -i 's/sslCert.*/sslKey\ =\ \"\"/' ./docker/dave-1.0-SNAPSHOT/etc/dave.conf
sed -i 's/jwtPublicKey.*/jwtPublicKey\ =\ \"\"/' ./docker/dave-1.0-SNAPSHOT/etc/dave.conf

docker login -e="$DOCKER_EMAIL" -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker build -t dbgdave/dave-api:${CIRCLE_SHA1} ./docker/
docker tag -f dbgdave/dave-api:${CIRCLE_SHA1} docker.io/dbgdave/dave-api:${CIRCLE_SHA1}
docker push dbgdave/dave-api:${CIRCLE_SHA1}
docker tag -f dbgdave/dave-api:${CIRCLE_SHA1} docker.io/dbgdave/dave-api:${CIRCLE_BRANCH}
docker push dbgdave/dave-api:${CIRCLE_BRANCH}
