#!/bin/bash
if [ -e /etc/battlecode.conf ]; then
	source /etc/battlecode.conf
else
	source etc/battlecode.conf
fi

runsql () {
	echo `mysql -u $DB_USER -p$DB_PASS $DATABASE -e "$1" | awk '{split($0,a," ")} END{print a[1]}'`
}

USER=`ls -l $REPO | awk '{split($0,a," ")} END{print a[3]}'`
CMD_PREFIX="sudo -u $USER"

cd $REPO
$CMD_PREFIX git reset --hard
$CMD_PREFIX git checkout -f master
$CMD_PREFIX git pull
if [ "$1" == "server" ]; then
	for TAG in `git tag`; do
		runsql "INSERT IGNORE INTO tags (tag) VALUES (\"$TAG\")"
	done
fi
chown -R $USER .
