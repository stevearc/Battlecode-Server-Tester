#!/bin/bash
if [ ! -e bs-tester.jar ]; then
  echo "This script must be run locally."
  echo "Try changing to the directory that run.sh is in."
  exit 1
fi


START_EXIT_STATUS=121
status=$START_EXIT_STATUS
while [ "$status" = "$START_EXIT_STATUS" ]
do
    java -jar bs-tester.jar $*
    status=$?
done
