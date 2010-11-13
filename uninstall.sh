#!/bin/bash
ID=`id -u`
if [ $ID != 0 ]; then
  echo "Must run as root"
  exit 1
fi

if [ -e /etc/bs-tester.conf ]; then
  rm /etc/bs-tester.conf
fi

if [ -e /etc/init.d/bs-tester ]; then
  rm /etc/init.d/bs-tester
  update-rc.d bs-tester remove 
fi
