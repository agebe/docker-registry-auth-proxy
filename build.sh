#!/bin/sh
set -e
set -x
TAG=${1:-latest}
gradle clean docker
( cd build/docker && docker build -t docker-registry-auth-proxy:$TAG . )
docker tag docker-registry-auth-proxy:$TAG agebe/docker-registry-auth-proxy:$TAG
