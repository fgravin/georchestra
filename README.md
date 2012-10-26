geOrchestra
===========

geOrchestra is a complete **Spatial Data Infrastructure** solution.

It features a **metadata catalog** (GeoNetwork), an **OGC server** (GeoServer), an **advanced viewer** (aka "mapfishapp"), an **extractor** (aka "extractorapp") and **many more** (security and auth system based on proxy/CAS/LDAP, analytics, admin UIs, ...)

How to build ?
==============

    $ git clone --recursive https://github.com/georchestra/georchestra.git
    $ cd georchestra
    $ ./mvn -Dmaven.test.skip=true -Ptemplate install

How to customize ?
==================
 
Copy the "template" config directory and edit "yourown" to match your needs:

    $ cp -r config/configuration/template config/configuration/yourown
    (edit config/configuration/yourown)
    (declare "yourown" profile in the root pom.xml)
    $ ./mvn -Dmaven.test.skip=true -Pyourown install

Want to run the advanced viewer locally ?
=========================================

This mode is useful for demo or dev purposes.

    $ cd mapfishapp
    $ ../mvn -Ptemplate jetty:run

Point your browser to [http://localhost:8080/mapfishapp/?noheader=true](http://localhost:8080/mapfishapp/?noheader=true) 

Want to trick the viewer into thinking you're logged in ?
---------------------------------------------------------

Install the [Modify Headers](https://addons.mozilla.org/en-US/firefox/addon/modify-headers/) Firefox extension, and set the headers to:
 * sec-username = your_desired_login
 * sec-roles = ROLE_SV_USER or ROLE_SV_EDITOR or ROLE_SV_ADMIN
