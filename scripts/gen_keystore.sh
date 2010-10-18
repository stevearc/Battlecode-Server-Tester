#!/bin/bash
FILE=$1
PASS=$2

echo -e "$2\n$2\nname\norg_unit\norg\ncity\nst\nus\nyes\n" | keytool -keystore $1 -selfcert -validity 300 -genkey
