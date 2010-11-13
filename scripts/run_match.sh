#!/bin/bash
REPO=$1
FILE=$2
ant file -f $REPO/build.xml > $FILE
