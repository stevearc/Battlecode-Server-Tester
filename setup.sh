#!/bin/bash
ID=`id -u`
if [ $ID != 0 ]; then
  echo "Must run as root"
  exit 1
fi

if [ -e /etc/bs-tester.conf ]; then
  if [ "$1" != "-f" ]; then
    echo "Setup completed.  To reconfigure type ./setup.sh -f"
    exit 0
  fi
fi

if [ ! -e ./run.sh ]; then
  echo "Must run setup.sh locally (./setup.sh)"
  exit 1
fi

INSTALL_DIR=`pwd`

set_param() {
  KEY=$1
  VAL=${2//\//\\\/}
  sed -e 's/^'"$KEY"'=.*/'"$KEY"'='"$VAL"'/' etc/bs-tester.conf > etc/tmp.conf
  mv etc/tmp.conf etc/bs-tester.conf
}

setup_client() {
  source etc/bs-tester.conf
  INSTALL_DIR=`pwd`

  # Specify number of cores
  VALID=0
  while [ $VALID == 0 ]; do
    echo -n "Number of cores? "
    read CORES
    if [ "`echo $CORES | egrep ^[[:digit:]]+$`" == "" ]; then
      VALID=0;
    else
      VALID=1;
    fi
  done
  
  sudo -u $SUDO_USER ./scripts/generate_ssh_keys.sh $VERSION_CONTROL $REPO_ADDR

  if [ ! -e repo ]; then
    echo "WARNING: Failed to initialize repository.  Either try again with ./setup.sh -f or manually clone your repository into $INSTALL_DIR/repo"
  fi
  set_param INSTALL_DIR $INSTALL_DIR
  set_param CORES $CORES

  cp etc/bs-tester.conf /etc
}

setup_server() {
  VERSION_OPTIONS=""
  for DIR in `find scripts/* -type d | sed -e 's/scripts\///'`; do
    VERSION_OPTIONS=$VERSION_OPTIONS"/"$DIR
  done

  # Specify version control
  VALID=0
  while [ $VALID == 0 ]; do
    echo -n "Version control ("${VERSION_OPTIONS:1}")? "
    read VERSION_CONTROL
    VALID=`expr match "$VERSION_OPTIONS" '.*'"$VERSION_CONTROL"'.*'`
  done

  # Specify version control URL
  VALID=0
  while [ "$VALID" == "0" ]; do
    echo -n "URL of repo (ex steven@server.mit.edu:/var/git/repo): "
    read REPO_ADDR
    VALID=`./scripts/$VERSION_CONTROL/check_url_format.sh $REPO_ADDR`
  done

  sudo -u $SUDO_USER ./scripts/generate_ssh_keys.sh $VERSION_CONTROL $REPO_ADDR

  if [ ! -e repo ]; then
    echo "WARNING: Failed to initialize repository.  Either try again with ./setup.sh -f or manually clone your repository into $INSTALL_DIR/repo"
  fi

  TEAM=""
  while [ "$TEAM" == "" ]; do
    echo -n "Team id (ex team049)? "
    read TEAM
  done
  SERVER=""
  while [ "$SERVER" == "" ]; do
    echo -n "IP address of this machine? "
    read SERVER
  done

  KEYSTORE_PASS=`uuidgen | cut -c-8`
  # Generate keystore
  echo -e "$KEYSTORE_PASS\n$KEYSTORE_PASS\nname\norg_unit\norg\ncity\nst\nus\nyes\n" | keytool -keystore keystore -selfcert -validity 300 -genkey > /dev/null 2> /dev/null

  set_param INSTALL_DIR $INSTALL_DIR
  set_param VERSION_CONTROL $VERSION_CONTROL
  set_param REPO_ADDR $REPO_ADDR
  set_param TEAM $TEAM
  set_param SERVER $SERVER
  set_param KEYSTORE_PASS $KEYSTORE_PASS
  cp etc/bs-tester.conf /etc

  # Generate bs-client.tar.gz
  DIR=`pwd | sed -e 's/.*\///'`
  cd ..
  tar -cf $DIR/bs-client.tar $DIR/bs-tester.jar $DIR/etc $DIR/keystore $DIR/README.txt $DIR/run.sh $DIR/setup.sh $DIR/scripts
  gzip $DIR/bs-client.tar
}

SERVER=0
if [ -e bs-client.tar.gz ]; then
  SERVER=1
elif [ ! -e keystore ]; then
  SERVER=1
fi

# if the -f option is passed, remove the current repo
if [ "$1" == "-f" ]; then
  rm -rf repo > /dev/null 2> /dev/null
  if [ "$SERVER" == "1" ]; then
    rm keystore > /dev/null 2> /dev/null
    rm bs-client.tar > /dev/null 2> /dev/null
    rm bs-client.tar.gz > /dev/null 2> /dev/null
  fi
fi

if [ "$SERVER" == "1" ]; then
  setup_server
else
  setup_client
fi