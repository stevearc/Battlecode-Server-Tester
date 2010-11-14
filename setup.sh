#!/bin/bash
ID=`id -u`
if [ $ID != 0 ]; then
  echo "Must run as root"
  exit 1
fi

if [[ `which git` == "" && `which svn` == "" ]]; then
  echo "version control not found.  Install git or svn."
  exit 1
fi

if [[ `which ant` == "" ]]; then
  echo "ant not found"
  exit 1
fi

if [[ `which java` == "" ]]; then
  echo "java not found"
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

if [ -e "$INSTALL_DIR/repo" ]; then
  if [ "$1" != "-f" ]; then
    echo "Setup completed.  To reconfigure type ./setup.sh -f"
    exit 0
  fi
fi

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
  
  INSTALL_DAEMON=1
  DAEMON="w"
  while [[ 1 ]]; do
    echo -ne "Would you like to install the daemon?\nbs-tester will start when you boot your computer [Y/n] "
    read DAEMON
    if [[ "$DAEMON" == "n" || "$DAEMON" == "N" || "$DAEMON" == "no" || "$DAEMON" == "No" || "$DAEMON" == "NO" ]]; then
      INSTALL_DAEMON=0
      break
    fi
    if [[ "$DAEMON" == "" || "$DAEMON" == "y" || "$DAEMON" == "Y" || "$DAEMON" == "yes" || "$DAEMON" == "Yes" || "$DAEMON" == "YES" ]]; then
      INSTALL_DAEMON=1
      break
    fi
  done
  if [ "$INSTALL_DAEMON" == "1" ]; then
    ./scripts/install_daemon.sh 0 $INSTALL_DIR
  fi

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

    # Specify team name
  TEAM=""
  while [ "$TEAM" == "" ]; do
    echo -n "Team id (ex team049)? "
    read TEAM
  done

  # Specify server address
  SERVER=""
  while [ "$SERVER" == "" ]; do
    echo -n "IP address/hostname of this machine? "
    read SERVER
  done

  KEYSTORE_PASS=`uuidgen | cut -c-8`
  # Generate keystore
  echo -e "$KEYSTORE_PASS\n$KEYSTORE_PASS\nname\norg_unit\norg\ncity\nst\nus\nyes\n" | keytool -keystore keystore -selfcert -validity 300 -genkey > /dev/null 2> /dev/null

  # Specify to install daemon or not
  INSTALL_DAEMON=1
  DAEMON="w"
  while [[ 1 ]]; do
    echo -ne "Would you like to install the daemon?\nbs-tester will start when you boot your computer [Y/n] "
    read DAEMON
    if [[ "$DAEMON" == "n" || "$DAEMON" == "N" || "$DAEMON" == "no" || "$DAEMON" == "No" || "$DAEMON" == "NO" ]]; then
      INSTALL_DAEMON=0
      break
    fi
    if [[ "$DAEMON" == "" || "$DAEMON" == "y" || "$DAEMON" == "Y" || "$DAEMON" == "yes" || "$DAEMON" == "Yes" || "$DAEMON" == "YES" ]]; then
      INSTALL_DAEMON=1
      break
    fi
  done
  if [ "$INSTALL_DAEMON" == "1" ]; then
    ./scripts/install_daemon.sh 1 $INSTALL_DIR
  fi
  
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
  tar -cf $DIR/bs-client.tar $DIR/bs-tester.jar $DIR/etc $DIR/keystore $DIR/README.txt $DIR/run.sh $DIR/setup.sh $DIR/scripts $DIR/uninstall.sh
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
