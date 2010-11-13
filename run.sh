#!/bin/bash
ID=`id -u`
if [ $ID != 0 ]; then
  echo "Must run as root"
  exit 1
fi

java -jar bs-tester.jar $*
