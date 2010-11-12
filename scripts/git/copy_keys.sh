#!/bin/bash
URL=$1
ROOT_URL=${URL%%:*}
echo "Host BSTesterServer" >> $HOME/.ssh/config
echo "  Hostname=$ROOT_URL" >> $HOME/.ssh/config
echo "  IdentityFile=$HOME/.ssh/id_rsa" >> $HOME/.ssh/config
ssh-copy-id -i $HOME/.ssh/id_rsa $ROOT_URL > /dev/null
