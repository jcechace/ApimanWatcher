#!/usr/bin/env bash

OPTS=`getopt -o pnd: --long push,name:,dist:  -- $@`
eval set -- "$OPTS"

PUSH_TO_HUB=false
IMAGE_LOCAL=jcechace/apiman-sidekick
WORKSPACE=$(dirname "${BASH_SOURCE[0]}")
DIST_SOURCE="${WORKSPACE}/../../build/distributions/sidekick-shadow.zip"

# Error codes
EXIT_DIST_NOT_FOUND=1

while true; do
    case "$1" in
    -p | --push) PUSH_TO_HUB=true; shift ;;
    -n | --name) IMAGE_LOCAL=$2; shift; shift;;
    -d | --dist) DIST_SOURCE=$2; shift; shift;;
    *) break ;;
    esac
done


if [ ! -f ${DIST_SOURCE} ]; then
    echo "Distribution file not found!"
    exit ${EXIT_DIST_NOT_FOUND}
fi

echo "Copying shadow distribution zip"
cp ${DIST_SOURCE} .

echo "Building docker image with tag '${IMAGE_LOCAL}'."
docker build -t ${IMAGE_LOCAL} ${WORKSPACE}


if $PUSH_TO_HUB; then
    echo "Pushing to docker hub.:"
    docker push ${IMAGE_LOCAL}:latest
fi


