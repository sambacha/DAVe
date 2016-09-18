#!/usr/bin/env bash
cp -r -v ./target/dave-1.0-SNAPSHOT/dave-1.0-SNAPSHOT ./dockerfile/dave-1.0-SNAPSHOT
cp -v ./dockerfile/dave.json ./dockerfile/dave-1.0-SNAPSHOT/etc/dave.json
#cp -v ./dockerfile/abcfr.keystore ./dockerfile/dave-1.0-SNAPSHOT/etc/abcfr.keystore
#cp -v ./dockerfile/deffr.keystore ./dockerfile/dave-1.0-SNAPSHOT/etc/deffr.keystore
#cp -v ./dockerfile/ersd01.truststore ./dockerfile/dave-1.0-SNAPSHOT/etc/ersd01.truststore
docker login -e="$DOCKER_EMAIL" -u="$DOCKER_USERNAME" -p="$DOCKER_PASSWORD"
docker build -t scholzj/dave:${CIRCLE_SHA1} ./dockerfile/
docker tag -f scholzj/dave:${CIRCLE_SHA1} docker.io/scholzj/dave:${CIRCLE_SHA1}
docker push scholzj/dave:${CIRCLE_SHA1}