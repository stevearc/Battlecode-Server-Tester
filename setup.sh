#!/bin/bash
if [[ `which ant` == "" ]]; then
    echo "ant not found"
    exit 1
fi

if [[ `which java` == "" ]]; then
    echo "java not found"
    exit 1
fi

if [[ `which tar` == "" ]]; then
    echo "tar not found"
    exit 1
fi

if [[ `which gzip` == "" ]]; then
    echo "gzip not found"
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

MASTER=1;

INSTALL_DIR=`pwd`

set_param() {
    KEY=$1
    VAL=${2//\//\\\/}
    sed -i -e 's/^'"$KEY"'=.*/'"$KEY"'='"$VAL"'/' etc/bs-tester.conf
}

setup_worker() {
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

    set_param INSTALL_DIR $INSTALL_DIR
    set_param CORES $CORES
    if [ "$INSTALL_DAEMON" == "1" ]; then
        sudo ./scripts/install_daemon.sh 0 $INSTALL_DIR
        sudo cp etc/bs-tester.conf /etc
    fi
}

setup_master() {
    # Get admin name
    ADMIN=""
    while [ "$ADMIN" == "" ]; do
        echo -n "Admin username? "
        read ADMIN
    done

    VERIFIED=0
    while [ "$VERIFIED" == "0" ]; do
        # Get admin password
        ADMIN_PASS=""
        while [ "$ADMIN_PASS" == "" ]; do
            echo -n "Admin password? "
            read -s ADMIN_PASS
        done
        echo ""
        CONFIRMATION=""
        echo -n "Confirm password? "
        read -s CONFIRMATION
        if [ "$ADMIN_PASS" == "$CONFIRMATION" ]; then
            VERIFIED=1
            echo ""
        else
            echo ""
            echo "ERROR: Passwords do not match"
        fi
    done

    # Specify server address
    SERVER=""
    while [ "$SERVER" == "" ]; do
        echo -n "IP address/hostname of this machine? "
        read SERVER
    done

    # Get existing battlecode files
    BATTLECODE_DIR=""
    while [ ! -e $BATTLECODE_DIR/lib/battlecode-server.jar ]; do
        if [ "$BATTLECODE_DIR" != "" ]; then
            echo "ERROR: Could not find $BATTLECODE_DIR/lib/battlecode-server.jar";
            BATTLECODE_DIR=""
        fi
        while [ "$BATTLECODE_DIR" == "" ]; do
            echo -n "Existing battlecode install directory? "
            read BATTLECODE_DIR
        done
    done
    mkdir -p battlecode/lib
    mkdir -p battlecode/teams
    mkdir matches
    cp -p scripts/build.xml battlecode/teams
    cp -p $BATTLECODE_DIR/lib/battlecode-server.jar battlecode/lib
    cp -pr $BATTLECODE_DIR/bc.conf $BATTLECODE_DIR/build.xml $BATTLECODE_DIR/idata $BATTLECODE_DIR/maps battlecode


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
        sudo ./scripts/install_daemon.sh 1 $INSTALL_DIR
    fi
  
    set_param INSTALL_DIR $INSTALL_DIR
    set_param SERVER $SERVER
    set_param ADMIN $ADMIN
    set_param ADMIN_PASS $ADMIN_PASS
    set_param KEYSTORE_PASS $KEYSTORE_PASS
    if [ "$INSTALL_DAEMON" == "1" ]; then
        sudo cp etc/bs-tester.conf /etc
    fi
    # Remove the ADMIN and ADMIN_PASS fields from bs-tester.conf
    set_param ADMIN " "
    set_param ADMIN_PASS " "

    # Generate bs-worker.tar.gz
    DIR=`pwd | sed -e 's/.*\///'`
    pushd .. > /dev/null
    # adjust setup script to run for workers
    sed -i -e 's/^MASTER=1/MASTER=0/' $DIR/setup.sh
    tar -cf $DIR/bs-worker.tar $DIR/bs-tester.jar $DIR/etc $DIR/keystore $DIR/README $DIR/COPYING $DIR/run.sh $DIR/setup.sh $DIR/scripts $DIR/uninstall.sh $DIR/battlecode
    gzip $DIR/bs-worker.tar
    # reset setup script
    sed -i -e 's/^MASTER=0/MASTER=1/' $DIR/setup.sh

    if [ "$INSTALL_DAEMON" != "1" ]; then
        popd > /dev/null
        set_param ADMIN $ADMIN
        set_param ADMIN_PASS $ADMIN_PASS
    fi

    echo "Setup completed!  All files for setting up a worker are in bs-worker.tar.gz"
}

# if the -f option is passed, remove the current repo and config file
if [ "$1" == "-f" ]; then
    echo "Cleaning out old files..."
    rm -rf repo > /dev/null 2> /dev/null
    rm -f /etc/bs-tester.conf > /dev/null 2> /dev/null
    rm -rf hsqldb* > /dev/null 2> /dev/null
    if [ "$MASTER" == "1" ]; then
        rm keystore > /dev/null 2> /dev/null
        rm bs-worker.tar > /dev/null 2> /dev/null
        rm bs-worker.tar.gz > /dev/null 2> /dev/null
    fi
fi

if [ "$MASTER" == "1" ]; then
    setup_master
else
    setup_worker
fi
