#!/bin/bash
URL=$1
ROOT_URL=${URL%%:*}
ssh-copy-id -i $HOME/.ssh/id_bs_tester.pub $ROOT_URL > /dev/null
