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
$CMD_PREFIX svn revert .
LATEST=`$CMD_PREFIX svn up | grep revision | sed 's/.*revision \([0-9]*\)./\1/'`
CUR=1
if [ "$1" == "server" ]; then
	while [ "$CUR" -le "$LATEST" ]; do
		runsql "INSERT IGNORE INTO tags (tag) VALUES (\"$CUR\")"
		let "CUR=$CUR+1"
	done
fi
chown -R $USER .
