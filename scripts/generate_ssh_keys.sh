#!/bin/bash
VERSION_CONTROL=$1
REPO_ADDR=$2

#source $HOME/.bashrc
# Generate an ssh key
if [ ! -e $HOME/.ssh/id_rsa ]; then
  ssh-keygen -N '' -f $HOME/.ssh/id_rsa -q
fi
./scripts/$VERSION_CONTROL/copy_keys.sh $REPO_ADDR

./scripts/$VERSION_CONTROL/init.sh $REPO_ADDR
