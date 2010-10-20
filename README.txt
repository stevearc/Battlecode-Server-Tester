##################
## REQUIREMENTS ##
##################
You need two things in order for this system to work properly:
	* Git or SVN
	* Java and Ant (to run the battlecode client)

#####################
## VERSION CONTROL ##
#####################
To run a client or server you need to set up a copy of your repository that will be dedicated to the client/server (no user access).  
You will need to set up ssh keys such that the repository can update without entering a password.  The simplest way to do this is:
  ssh-keygen # don't enter a passphrase
  ssh-copy-id [server_with_repo]
I would not recommend having a client and server share the same repository if you are running both on the same computer.

############
## CONFIG ##
############
The java client/server first tries to read from /etc/battlecode.conf and if that fails it will attempt to read from ./etc/battlecode.conf.  The web interface can only read from /etc/battlecode.conf.  Make sure the file you are using has all the appropriate information.

#############
## RUNNING ##
#############
To run the client do:
sudo ./run.sh
To run the server do:
sudo ./run.sh -s

################
## INSTALLING ##
################
To run on bootup, configure the parameters at the top of the etc/init.d/battlecode file and place it in your /etc/init.d directory.  Then do:
  sudo update-rc.d battlecode defaults
Now it will start on bootup and you can manage it with invoke-rc.d
