#!/bin/bash
sed -i -e 's/ADMIN=.*/ADMIN=/g' /etc/bs-tester.conf
sed -i -e 's/ADMIN_PASS=.*/ADMIN_PASS=/g' /etc/bs-tester.conf
