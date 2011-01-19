#!/bin/bash
URL=$1
STATUS=`expr match $URL 'ssh://.*@.*/.*'`
echo $STATUS
