#!/bin/bash
rm battlecode.tar
rm battlecode.tar.gz
tar -cvvf battlecode.tar battlecode_web.tar.gz Battlecode.jar README.txt scripts etc maps run.sh
gzip battlecode.tar
scp battlecode.tar.gz thick.mit.edu:/var/www/public
