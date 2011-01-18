#!/bin/bash
SERVER=$1
DIR=${2//\//\\\/}
cat etc/init.d/bs-tester | sed -e 's/MASTER => 1/MASTER => '"$SERVER"'/' -e 's/BS_TESTER_DIR => .*/BS_TESTER_DIR => "'"$DIR"'";/' > /etc/init.d/bs-tester
chmod +x /etc/init.d/bs-tester
update-rc.d bs-tester defaults
