#!/bin/bash
ID=`id -u`
if [ $ID != 0 ]; then
  echo "Must run as root"
  exit 1
fi

if [ ! -e bs-tester.jar ]; then
  echo "This script must be run locally."
  echo "Try changing to the directory that run.sh is in."
  exit 1
fi

java -jar bs-tester.jar $*
