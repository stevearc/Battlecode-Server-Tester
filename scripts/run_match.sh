#!/bin/bash
DIR=$1
FILE=$2
cd $DIR
rm *.rms
ant file -f $DIR/build.xml > $FILE
