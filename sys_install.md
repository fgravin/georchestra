LDAP
=====

* Installation des paquets

        apt-get install slapd ldap-utils

* Configuration de l'arborescence

    Dans :

        vi /etc/ldap/slapd.d/cn=config/olcDatabase={1}hdb.ldif

    Changer les valeurs :
     
            olcRootDN= cn=admin,dc=georchestra,dc=org
            olcSuffix= dc=georchestra,dc=org
		
* Redémarrage

        /etc/init.d/slapd restart

* Import des données

 * Récupération des scripts
 
            apt-get install git-core
            git clone git://github.com/georchestra/LDAP.git
	
 * Création objet Root	 

            ldapadd -Dcn=admin,dc=georchestra,dc=org -f georchestra-root.ldif -x -c -W

 * Peuplement de la base
 
            ldapadd -Dcn=admin,dc=georchestra,dc=org -f georchestra.ldif -x -c -W

 * Vérification
 
            ldapsearch -x -bdc=georchestra,dc=org | less

Postgres
==========

* Installation 

        apt-get install postgresql postgresql-8.4-postgis postgis	
	
* Création de la base de données Geonetwork

        su postgres
        createdb geonetwork
        createlang plpgsql geonetwork
        psql -f /usr/share/postgresql/8.4/contrib/postgis-1.5/postgis.sql geonetwork
        psql -f /usr/share/postgresql/8.4/contrib/postgis-1.5/spatial_ref_sys.sql geonetwork

        createuser www-data
        psql geonetwork
        > ALTER TABLE spatial_ref_sys   OWNER TO "www-data";
        > ALTER TABLE geometry_columns  OWNER TO "www-data";
        > ALTER TABLE geography_columns OWNER TO "www-data";
        > ALTER USER "www-data" WITH PASSWORD 'www-data';

* Création bases downloadform et ogcstatistics

 * downloadform

            createdb downloadform
            psql downloadform

    Puis exécuter le script :  
    https://github.com/georchestra/georchestra/blob/master/downloadform/samples/sample.sql

 * ogcstatistics

            createdb ogcstatistics
            psql ogcstatistics

    Puis exécuter le script :  
    https://github.com/georchestra/georchestra/blob/master/ogc-server-statistics/ogc_statistics_table.sql
    
Apache
=========

* Chargement des modules

        apt-get install apache2 libapache2-mod-auth-cas 

* liste des modules disponibles pour apache

        ls /etc/apache2/mods-enabled
        a2enmod proxy_ajp proxy_connect proxy_http proxy
        a2enmod ssl rewrite
        /etc/init.d/apach2 restart

* Création du VirtualHost

        cd /etc/apache2/site-available
        a2dissite default default-ssl
        vi georchestra

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   	<VirtualHost *:80>
		 ServerName vm-georchestra
		 DocumentRoot /var/www/georchestra/htdocs
		 LogLevel warn
		 ErrorLog /var/www/georchestra/logs/error.log
		 CustomLog /var/www/georchestra/logs/access.log "combined"
		 Include /var/www/georchestra/conf/*.conf
		 ServerSignature Off
	</VirtualHost>
	<VirtualHost *:443>
		 ServerName vm-georchestra
		 DocumentRoot /var/www/georchestra/htdocs
		 LogLevel warn
		 ErrorLog /var/www/georchestra/logs/error.log
		 CustomLog /var/www/georchestra/logs/access.log "combined"
		 Include /var/www/georchestra/conf/*.conf
		 SSLEngine On
		 SSLCertificateFile /var/www/georchestra/ssl/georchestra.crt
		 SSLCertificateKeyFile /var/www/georchestra/ssl/georchestra-unprotected.key
		 SSLCACertificateFile /etc/ssl/certs/ca-certificates.crt
		 ServerSignature Off
	</VirtualHost>
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

        a2ensite georchestra
   
* Création de l'arborescence apache pour georchestra

        cd /var/www
        mkdir georchestra
        cd georchestra
        mkdir conf htdocs logs ssl

    l'user apache sous Debien = www-data

        id www-data

    il faut donner les droits d'ecriture a www-data dans logs

        chgrp www-data logs/
        chmod g+w logs/

* Création de la configuration Apache

        vim proxypass.conf
        
    Y copier la configuration suivante :
        
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    <IfModule !mod_proxy.c>
        LoadModule proxy_module /usr/lib/apache2/modules/mod_proxy.so
    </IfModule>
    <IfModule !mod_proxy_http.c>
        LoadModule proxy_http_module /usr/lib/apache2/modules/mod_proxy_http.so
    </IfModule>

    <Proxy *>
        Order deny,allow
        Allow from all
    </Proxy>

    RewriteLog /tmp/rewrite.log
    RewriteLogLevel 3

    SetEnv no-gzip on
    ProxyTimeout 999999999

    RewriteEngine On
    RewriteRule ^/analytics$ /analytics/ [R]
    RewriteRule ^/cas$ /cas/ [R]
    RewriteRule ^/catalogapp$ /catalogapp/ [R]
    RewriteRule ^/downloadform$ /downloadform/ [R]
    RewriteRule ^/extractorapp$ /extractorapp/ [R]
    RewriteRule ^/extractorapp$ /extractorapp/ [R]
    RewriteRule ^/geonetwork$ /geonetwork/ [R]
    RewriteRule ^/geoserver2/(.*)$ /geoserver/$1 [R]
    RewriteRule ^/geoserver$ /geoserver/ [R]
    RewriteRule ^/geowebcache$ /geowebcache/ [R]
    RewriteRule ^/mapfishapp$ /mapfishapp/ [R]
    RewriteRule ^/proxy$ /proxy/ [R]
    RewriteRule ^/static$ /static/ [R]

    ProxyPass /analytics/ ajp://localhost:8009/analytics/ 
    ProxyPassReverse /analytics/ ajp://localhost:8009/analytics/

    ProxyPass /cas/ ajp://localhost:8009/cas/ 
    ProxyPassReverse /cas/ ajp://localhost:8009/cas/

    ProxyPass /casfailed.jsp ajp://localhost:8009/casfailed.jsp 
    ProxyPassReverse /casfailed.jsp ajp://localhost:8009/casfailed.jsp

    ProxyPass /catalogapp/ ajp://localhost:8009/catalogapp/ 
    ProxyPassReverse /catalogapp/ ajp://localhost:8009/catalogapp/

    ProxyPass /downloadform/ ajp://localhost:8009/downloadform/ 
    ProxyPassReverse /downloadform/ ajp://localhost:8009/downloadform/

    ProxyPass /extractorapp/ ajp://localhost:8009/extractorapp/ 
    ProxyPassReverse /extractorapp/ ajp://localhost:8009/extractorapp/

    ProxyPass /geonetwork/ ajp://localhost:8009/geonetwork/ 
    ProxyPassReverse /geonetwork/ ajp://localhost:8009/geonetwork/

    ProxyPass /geoserver/ ajp://localhost:8009/geoserver/ 
    ProxyPassReverse /geoserver/ ajp://localhost:8009/geoserver/

    ProxyPass /geowebcache/ ajp://localhost:8009/geowebcache/ 
    ProxyPassReverse /geowebcache/ ajp://localhost:8009/geowebcache/

    ProxyPass /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check 
    ProxyPassReverse /j_spring_cas_security_check ajp://localhost:8009/j_spring_cas_security_check

    ProxyPass /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout 
    ProxyPassReverse /j_spring_security_logout ajp://localhost:8009/j_spring_security_logout

    ProxyPass /mapfishapp/ ajp://localhost:8009/mapfishapp/ 
    ProxyPassReverse /mapfishapp/ ajp://localhost:8009/mapfishapp/

    ProxyPass /proxy/ ajp://localhost:8009/proxy/ 
    ProxyPassReverse /proxy/ ajp://localhost:8009/proxy/

    ProxyPass /static/ ajp://localhost:8009/static/ 
    ProxyPassReverse /static/ ajp://localhost:8009/static/


    AddType application/vnd.ogc.context+xml .wmc
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

 
Apache - Certificat SSL
-----------------------

* on génère la clé privée

        cd /var/www/georchestra/ssl
        openssl genrsa -des3 -out georchestra.key 1024

* on génère le certificat pour cette clé

        openssl req -new -key georchestra.key -out georchestra.csr

* on rentre les informations sans mettre de mot de pass

        Common Name (eg, YOUR name) []: mettre vm-georchestra, cela correspondra à l'adresse du site

* Creer une clé non protégée

        openssl rsa -in georchestra.key -out georchestra-unprotected.key
        openssl x509 -req -days 365 -in georchestra.csr -signkey georchestra.key -out georchestra.crt

* Redémarrer apache

        sudo /etc/init.d/apache2 restart
        
* Test
	* Modification du hosts
	
            vim /etc/hosts

        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        127.0.0.1       vm-georchestra
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	* http://vm-georchestra
	* https://vm-georchestra

Tomcat
=========

Keystore/Trustore
-------------------

* Création du keystore

        cd /srv/tomcat/tomcat1/conf/
        keytool -genkey -alias georchestra_localhost -keystore keystore -storepass mdpstore -keypass mdpstore -keyalg RSA -keysize 2048

    Il faut mettre la valeur "localhost" dans la variable "prénom et nom" puisqu'il s'agit du trust entre le security-proxy, 
    le CAS qui sont généralement sur le même tomcat. 

    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    Quels sont vos pr?nom et nom ?
      [Unknown] :  localhost
    Quel est le nom de votre unité organisationnelle ?
      [Unknown] :
    Quelle est le nom de votre organisation ?
      [Unknown] :
    Quel est le nom de votre ville de résidence ?
      [Unknown] :
    Quel est le nom de votre état ou province ?
      [Unknown] :
    Quel est le code de pays ? deux lettres pour cette unit? ?
      [Unknown] :
    Est-ce CN=localhost, OU=Unknown, O=Unknown, L=Unknown, ST=Unknown, C=Unknown ?
      [non] :  oui
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
        keytool -keystore keystore -list
       
* Configuration du truststore 

        vim /srv/tomcat/tomcat1/bin/setenv.sh
        
    ~~~~~~~~~~~~~~
    export JAVA_OPTS="$JAVA_OPTS -Djavax.net.ssl.trustStore=/srv/tomcat/tomcat1/conf/keystore -Djavax.net.ssl.trustStorePassword=mdpstore"
    ~~~~~~~~~~~~~~~

* Configuration des connectors

        vim /srv/tomcat/tomcat1/conf/server.xml
        
    ~~~~~~~~~~~~~~~~~~~~~~~~~    
    <Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
       URIEncoding="UTF-8"
       maxThreads="150" scheme="https" secure="true"
       clientAuth="false"
       keystoreFile="/srv/tomcat/tomcat1/conf/keystore"
       keystorePass="mdpstore"
       compression="on"
       compressionMinSize="2048"
       noCompressionUserAgents="gozilla, traviata"
       compressableMimeType="text/html,text/xml,text/javascript,application/x-javascript,application/javascript,text/css" />
    ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    ~~~~~~~~~~~~~~~~~~~~~~
    <Connector URIEncoding="UTF-8"
           port="8009"
           protocol="AJP/1.3"
           connectionTimeout="20000"
           redirectPort="8443" />
    ~~~~~~~~~~~~~~~~~~~~~~
    
* Redémarrage de Tomcat
 
        sudo /etc/init.d/tomcat-tomcat1 restart
    
