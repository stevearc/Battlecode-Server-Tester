#!/bin/bash
if [ -e battlecode.tar.gz ]; then
  rm battlecode.tar.gz
fi

cp -r Battlecode.jar README.txt scripts etc run.sh battlecode
tar -cvvf battlecode.tar battlecode
gzip battlecode.tar
rm -rf battlecode/*
