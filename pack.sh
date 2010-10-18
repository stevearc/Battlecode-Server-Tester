#!/bin/bash
if [ -e battlecode.tar.gz ]; then
  rm battlecode.tar.gz
fi

rm -rf battlecode/*
cp -r Battlecode.jar README.txt scripts etc run.sh battlecode
tar -cvvf battlecode.tar battlecode
gzip battlecode.tar
