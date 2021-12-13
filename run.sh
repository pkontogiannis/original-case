#!/bin/bash

build() {
   echo "building image $1"
   docker build -t $1 $2

   [ $? != 0 ] && \
     echo "Docker image build failed !" && exit 100
}

build "klm_amadeus" "./"

cd ./docker
docker-compose up