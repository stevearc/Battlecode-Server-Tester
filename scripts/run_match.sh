#!/bin/bash
DIR=$1
FILE=$2
START=`pwd`
cd $DIR
rm *.rms
ant file -f build.xml > $START/$FILE
