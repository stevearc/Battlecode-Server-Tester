#!/bin/bash
source /etc/bs-tester.conf

REPO=$INSTALL_DIR/repo
USER=`ls -ld $REPO | awk '{split($0,a," ")} END{print a[3]}'`
CMD_PREFIX="sudo -u $USER"

cd $REPO
$CMD_PREFIX svn revert -R . > /dev/null
$CMD_PREFIX svn up > /dev/null
OUT=`$CMD_PREFIX svn up`
REVISION=${OUT##At revision }
REVISION=${REVISION%%\.}
VAR=1
while [[ $VAR < $REVISION ]]; do
  echo $VAR
  let "VAR=$VAR+1"
done
echo $VAR
chown -R $USER .
