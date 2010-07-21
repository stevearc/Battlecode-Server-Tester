#!/bin/bash
if [ -e battlecode.tar ]; then
  rm battlecode.tar
fi
if [ -e battlecode.tar.gz ]; then
  rm battlecode.tar.gz
fi
rm -rf battlecode/*
cp -r battlecode_web.tar.gz Battlecode.jar README.txt scripts etc run.sh install.sh battlecode
tar -cvvf battlecode.tar battlecode
gzip battlecode.tar
scp battlecode.tar.gz thick.mit.edu:/var/www/public
