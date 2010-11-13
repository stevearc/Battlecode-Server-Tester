##################
## REQUIREMENTS ##
##################
You need two things in order for this system to work properly:
	* Git or SVN
	* Java and Ant (to run the battlecode client)

#######################
## INSTALLING SERVER ##
#######################
To install the server, simply run
sudo ./setup.sh
It should prompt you for a few parameters and automatically set up the /etc/bs-tester.conf file.

########################
## INSTALLING CLIENTS ##
########################
After running setup.sh, the script should have generated a bs-client.tar.gz file.  Copy this file
to the client computer (using scp or some other tool).  Then unzip it on the client and run
sudo ./setup.sh
It should prompt you for just a couple fields and generate the client config file.  

#############
## RUNNING ##
#############
The server and client can be run with the run.sh file.  Type
./run.sh -h
to display the available commandline arguments.  
If you installed the daemon in the setup script, you can start the service with 
sudo invoke-rc.d bs-tester start
