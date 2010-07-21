#################
## QUICK START ##
#################
If you want to set up an server on an ubuntu machine, you can simply
run install.sh and skip down to the section on Version Control

##################
## REQUIREMENTS ##
##################
You need four things in order for this system to work properly:
	* Apache server with ssl, php, and php-mysql module
	* MySQL server
	* Git or SVN
	* Java and Ant (to run the battlecode client)

##############
## DATABASE ##
##############
Create a new user and database in MySQL.
  mysql -u root -p
  [enter password]
  CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';
  CREATE DATABASE 'dbname';
  GRANT ALL ON dbname.* TO 'user'@'localhost';
  QUIT
After this setup, you need to put the database user/password information into the etc/battlecode.conf file

################
## WEB SERVER ##
################
This section makes many references to the files in etc/apache2.

These instructions will help you generate a personal X.509 certificate
sudo mkdir /etc/apache2/ssl
cd /etc/apache2/ssl
sudo openssl genrsa -out server.key 1024
sudo openssl req -new -key server.key -out server.csr
(The information you enter is unimportant.  Just make sure the password is blank)
sudo openssl x509 -req -days 60 -in server.csr -signkey server.key -out server.crt
You can change the days field to however long you want the certificate to be valid

Go back to the folder with all the battlecode files
sudo cp etc/apache2/sites-available/ssl /etc/apache2/sites-available
sudo ln -s ../ssl /etc/apache2/sites-enabled
sudo ln -s ../mods-available/ssl.load /etc/apache2/mods-enabled
sudo ln -s ../mods-available/ssl.conf /etc/apache2/mods-enabled
sudo cp etc/apache2/ssl/mitclientca.crt /etc/apache2/ssl
sudo mkdir /var/www-ssl
Restart apache (sudo invoke-rc.d apache2 restart)
unzip battlecode_web.tar.gz into /var/www-ssl
You will need to create a symbolic link from the directory you are storing the match files in to /var/www-ssl/battlecode/matches

#####################
## VERSION CONTROL ##
#####################
To run a client or server you need to set up a copy of your repository that will be dedicated to the client/server (no user access).  
You will need to set up ssh keys such that the repository can update without entering a password.  The simplest way to do this is:
  ssh-keygen # don't enter a passphrase
  ssh-copy-id [server_with_repo]
I would not recommend having a client and server share the same repository.

############
## CONFIG ##
############
The java program first tries to read from /etc/battlecode.conf and if that fails it will attempt to read from ./etc/battlecode.conf.  The web interface can only read from /etc/battlecode.conf.  Make sure the file you are using has all the appropriate information.

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
