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
		 ServerName georchestra.georchestra.inra.fr
		 DocumentRoot /var/www/georchestra/htdocs
		 LogLevel warn
		 ErrorLog /var/www/georchestra/logs/error.log
		 CustomLog /var/www/georchestra/logs/access.log "combined"
		 Include /var/www/georchestra/conf/*.conf
		 ServerSignature Off
	</VirtualHost>
	<VirtualHost *:443>
		 ServerName georchestra.georchestra.inra.fr
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

    copier les configs sur la VM

        cd /var/www/georchestra/conf
        rm auth-base-basic-file-user-phpldapadmin.conf
        rm directive-phpldapadmin.conf
 
Apache - Certificat SSL
-----------------------

* on génère la clé privée

        cd /var/www/georchestra/ssl
        openssl genrsa -des3 -out georchestra.key 1024

* on génère le certificat pour cette clé

        openssl req -new -key georchestra.key -out georchestra.csr

* on rentre les informations sans mettre de mot de pass

        Common Name (eg, YOUR name) []: mettre geoserver.georchestra.inra.fr, cela correspondra à l'adresse du site

* Creer une clé non protégée

        openssl rsa -in georchestra.key -out georchestra-unprotected.key
        openssl x509 -req -days 365 -in georchestra.csr -signkey georchestra.key -out georchestra.crt

* Test
	* $ dhclient eth0
	* Modification du hosts
	* http://georchestra.georchestra.inra.fr
	* https://georchestra.georchestra.inra.fr
