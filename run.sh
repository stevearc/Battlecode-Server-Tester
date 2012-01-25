#!/bin/bash
if [ ! -e bs-tester.jar ]; then
  echo "This script must be run locally."
  echo "Try changing to the directory that run.sh is in."
  exit 1
fi

VMARGS=""
BSARGS=""
for i in $*; do
    if [[ $i == -X* ]]; then
        VMARGS="$VMARGS $i"
    else
        BSARGS="$BSARGS $i"
    fi
done

START_EXIT_STATUS=121
status=$START_EXIT_STATUS
while [ "$status" = "$START_EXIT_STATUS" ]
do
    java $VMARGS -jar bs-tester.jar $BSARGS
    status=$?
done
