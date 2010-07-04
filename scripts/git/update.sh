#!/bin/bash
if [ -e /etc/battlecode.conf ]; then
	source /etc/battlecode.conf
else
	source etc/battlecode.conf
fi

runsql () {
	echo `mysql -u $DB_USER -p$DB_PASS $DATABASE -e "$1" | awk '{split($0,a," ")} END{print a[1]}'`
}

CMD_PREFIX=""
CUR_USER=`whoami`
if [ "$CUR_USER" != "$USER" ]; then
	CMD_PREFIX="sudo -u $USER"
fi

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
