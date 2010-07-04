##################
## REQUIREMENTS ##
##################
You need four things in order for this system to work properly:
	* Apache server with php and php-mysql module
	* MySQL server
	* Git or SVN
	* Java and Ant (to run the battlecode client)
On Ubuntu, the packages should be:
sudo apt-get install apache2 libapache2-mod-php5 php5-mysql git-core subversion openjdk-6-jre ant

##############
## DATABASE ##
##############
Create a new user and database in MySQL.
  mysql -u root -p
  [enter password]
  create user 'user'@'localhost' identified by 'password';
  create database 'dbname';
  grant all on dbname.* to 'user'@'localhost';
  quit
After you do that, the setup commands are all in etc/db.sql, so you can simply run:
	mysql -u root -p dbname < etc/db.sql

################
## WEB SERVER ##
################
This section makes many references to the files in etc/apache2.
Make sure the configuration in the included ports.conf file is copied to your /etc/apache2 folder.

These instructions will help you generate a personal X.509 certificate
Go to /etc/apache/ssl (make the directory if necessary)
sudo openssl genrsa -out server.key 1024
sudo openssl req -new -key server.key -out server.csr
(The information you enter is unimportant.  Just make sure the password is blank)
sudo openssl x509 -req -days 60 -in server.csr -signkey server.key -out server.crt
You can change the days field to however long you want the certificate to be valid

Copy the included file in sites-available to /etc/apache2/sites-available and make a symlink to it from /etc/apache2/sites-enabled.
make a symlink in mods-enabled to mods-available/ssl.*
Copy the etc/apache2/ssl/mitclientca.crt file to /etc/apache2/ssl
By default the https root directory is /var/www-ssl.  If you want to change it, the settings are in the /etc/apache2/sites-available/ssl file.
Restart apache (sudo invoke-rc.d apache2 restart)
Note that if you set up ssl you will need to unzip battlecode_web.tar.gz into /var/www-ssl
You will need to create a symbolic link from the directory you are storing the match files in to /var/www-ssl/battlecode/matches


#####################
## VERSION CONTROL ##
#####################
To run a client or server you need to set up a copy of your repository that will be dedicated to the client/server (no user access).  
You will need to set up ssh keys such that the repository can update without entering a password.  The simplest way to do this is:
  ssh-keygen # don't enter a passphrase
  ssh-copy-id [server_with_repo]
I would not recommend having a client and server share the same repository.

#############
## RUNNING ##
#############
First make sure that you copied the battlecode.conf file to /etc and edited the values appropriately
You must run all commands as root.  To run the client, do 
sudo ./run.sh
To run the server, add a -s argument 
To run as a daemon, configure the parameters at the top of the etc/init.d/battlecode file and place it in your /etc/init.d directory.  Then do:
  sudo update-rc.d battlecode defaults
Now it will start on bootup and you can manage it with invoke-rc.d
