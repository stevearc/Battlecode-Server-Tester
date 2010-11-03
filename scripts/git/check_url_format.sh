#!/bin/bash
URL=$1
STATUS=`expr match $URL '.*:.*'`
echo $STATUS
