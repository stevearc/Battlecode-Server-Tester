#!/bin/bash
runsql () {
	echo `mysql -u root -p $1 -e "$2" | awk '{split($0,a," ")} END{print a[1]}'`
}

sudo apt-get install apache2 libapache2-mod-php5 php5-mysql mysql-client mysql-server git-core subversion openjdk-6-jre ant

sudo mkdir /etc/apache2/ssl
pushd /etc/apache2/ssl
if [ -e server.crt ]; then
  echo "Already have ssl cert, skipping generation"
else 
  echo "Generating self-signed ssl certificate"
  sudo openssl genrsa -out server.key 1024
  yes "" | sudo openssl req -new -key server.key -out server.csr
  sudo openssl x509 -req -days 60 -in server.csr -signkey server.key -out server.crt
fi
popd
if [ -e /etc/apache2/sites-available/ssl ]; then
  echo "SSL site already enabled, skipping enable"
else
  sudo cp etc/apache2/sites-available/ssl /etc/apache2/sites-available
fi
sudo ln -s ../sites-available/ssl /etc/apache2/sites-enabled
sudo ln -s ../mods-available/ssl.load /etc/apache2/mods-enabled
sudo ln -s ../mods-available/ssl.conf /etc/apache2/mods-enabled
if [ -e /etc/apache2/ssl/mitclientca.crt ]; then
  echo -n ""
else
  sudo cp etc/apache2/ssl/mitclientca.crt /etc/apache2/ssl
fi
sudo mkdir /var/www-ssl
if [ -e /var/www-ssl/battlecode ]; then
  echo "battlecode web directory already created"
else
  echo "unzipping web interface into /var/www-ssl"
  sudo tar -zxf battlecode_web.tar.gz -C /var/www-ssl
fi
sudo invoke-rc.d apache2 restart

echo "Setting up database..."
echo "Please enter your root mysql password"
USER=`runsql mysql "select User from user where User like \"battlecode\""`
if [ "$USER" == "" ]; then
  echo -n "Enter the desired battlecode mysql password: "
  read PASS
  echo "Please enter your root mysql password...again..."
  mysql -u root -p -e "CREATE USER 'battlecode'@'localhost' IDENTIFIED BY '"$PASS"'; CREATE DATABASE 'battlecode'; GRANT ALL ON battlecode.* TO 'battlecode'@'localhost';"
else
  echo "battlecode user already exists, skipping..."
fi

echo "Putting startup script in /etc/init.d"
HOME=`pwd | sed -e 's/\//\\\\\//g'`
cat etc/init.d/battlecode | sed 's/\/home\/steven\/battlecode/'"$HOME"'/' > /tmp/battlecode
sudo mv /tmp/battlecode /etc/init.d/battlecode
sudo update-rc.d battlecode defaults

