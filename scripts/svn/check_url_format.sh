#!/bin/bash
URL=$1
STATUS=`expr match $URL 'svn+ssh://.*@[^/]*/.*'`
echo $STATUS
