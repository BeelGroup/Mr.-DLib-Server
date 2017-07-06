#!/bin/bash
# You need to send your public key to the adminstrator to get access to the dev server
# Tunneling 
ssh -N -L 3306:localhost:3306 admin@api-dev.mr-dlib.org & # solr
ssh -N -L 31415:localhost:31415 admin@api-dev.mr-dlib.org & 
ssh -N -L 8983:localhost:8983 admin@api-dev.mr-dlib.org & # mysql
