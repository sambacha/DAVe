#!/usr/bin/env bash

DAVE_VERSION="1.0-SNAPSHOT"
DAVE_APPLICATION_NAME="dave"
DAVE_CONFIG_FILE="${DAVE_APPLICATION_NAME}-${DAVE_VERSION}/etc/dave.conf"

# Copy the DAVe binaries
cp -r -v ./target/"${DAVE_APPLICATION_NAME}-${DAVE_VERSION}"/"${DAVE_APPLICATION_NAME}-${DAVE_VERSION}" ./docker/"${DAVE_APPLICATION_NAME}-${DAVE_VERSION}"

sed -i 's/\(\s\+sslKey\s\+=\).*/\1\ \"\"/' ./docker/"${DAVE_CONFIG_FILE}"
sed -i 's/\(\s\+sslCert\s\+=\).*/\1\ \"\"/' ./docker/"${DAVE_CONFIG_FILE}"
sed -i 's/\(\s\+sslTrustCerts\s\+=\).*/\1\ \[\]/' ./docker/"${DAVE_CONFIG_FILE}"
sed -i 's/\(\s\+jwtPublicKey\s\+=\).*/\1\ \[\]/' ./docker/"${DAVE_CONFIG_FILE}"

docker login -e="$DOCKER_EMAIL" -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker build -t dbgdave/dave-api:${CIRCLE_SHA1} ./docker/
docker tag -f dbgdave/dave-api:${CIRCLE_SHA1} docker.io/dbgdave/dave-api:${CIRCLE_SHA1}
docker push dbgdave/dave-api:${CIRCLE_SHA1}
docker tag -f dbgdave/dave-api:${CIRCLE_SHA1} docker.io/dbgdave/dave-api:${CIRCLE_BRANCH}
docker push dbgdave/dave-api:${CIRCLE_BRANCH}
