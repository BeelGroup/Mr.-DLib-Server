#!/bin/bash
# You need to send your public key to the adminstrator to get access to the dev server
# Tunneling 
target=$1
ssh -N -L 3306:localhost:3306 "admin@api-${target}.mr-dlib.org" & # solr
ssh -N -L 31415:localhost:31415 "admin@api-${target}.mr-dlib.org" & 
ssh -N -L 8983:localhost:8983 "admin@api-${target}.mr-dlib.org" & # mysql
ssh -N -L 9000:localhost:9000 "admin@api-${target}.mr-dlib.org" & # tomcat/dev
ssh -N -L 9001:localhost:9001 "admin@api-${target}.mr-dlib.org" & # doc2vec service
