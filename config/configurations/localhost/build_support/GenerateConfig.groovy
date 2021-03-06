/* 
 * This file can optionally generate configuration files.  The classic example
 * is when a project has both a integration and a production server.  
 * 
 * The configuration my be in a subdirectory of build_support (which is not copied into the configuration by default)
 * Depending on serverId, this script can copy the files to the outputDir and copy a shared.maven.filters with the parameters that
 * are needed depending on serverId.  More can be done but that is the classic example
 */
class GenerateConfig {
	/**
	 * @param project The maven project.  you can get all information about the project from this object
	 * @param log a logger for logging info
	 * @param ant an AntBuilder (see groovy docs) for executing ant tasks
	 * @param basedirFile a File object that references the base directory of the conf project
	 * @param target the server property which is normally set by the build profile.  It indicates the project that is being built
	 * @param subTarget the "subTarget" that the project is being deployed to.  For example integration or production
	 * @param targetDir a File object referencing the targetDir
	 * @param buildSupportDir a File object referencing the build_support dir of the target project
	 * @param outputDir the directory to copy the generated configuration files to
	 */
	def generate(def project, def log, def ant, def basedirFile, 
							def target, def subTarget, def targetDir, 
							def buildSupportDir, def outputDir) {
		def SEP = File.separator
		// make geonetwork use the development xslt transformer
		new PropertyUpdate(
		  from: 'defaults'+SEP+'geonetwork'+SEP+'maven.filter', 
		  to: 'geonetwork'+SEP+'maven.filter').update { properties ->
  		  properties['transformFactory'] = 'net.sf.saxon.TransformerFactoryImpl'
  		  properties['wfs.host'] = '@wfs_ldap_host@'
        properties['wfsRegionsCapabilities'] = 'http://ids.pigma.org/geoserver/pigma_loc/wfs?REQUEST=GetCapabilities&amp;SERVICE=WFS&amp;VERSION=1.0.0'
        properties['wfsRegionsCredentials'] = '<param name="user" value="@shared.privileged.geoserver.user@" /><param name="pass" value="@shared.privileged.geoserver.pass@" />'
        properties['geoserver.node.namespace.prefix'] = 'pigma_pub'
        properties['geoserver.node.namespace.url'] = 'http://ids.pigma.org/geoserver/pigma_pub'
        properties['config.xml.typenames'] = 
'''         <typename name="Canton" typename="pigma_loc:recentrage_cantons" nameAtt="nom_canton" />
            <typename name="Commune" typename="pigma_loc:recentrage_communes" nameAtt="nom_commun" />
            <typename name="Departement" typename="pigma_loc:recentrage_departements" nameAtt="nom_depart" />
            <typename name="EPCI" typename="pigma_loc:recentrage_epci" nameAtt="nom_epci" />
            <typename name="Lieu-dit" typename="pigma_loc:recentrage_lieudits" nameAtt="nom_lieu_d" />'''
        properties['config.xml.wfsUrl'] = 'http://@shared.server.name@/geoserver/wfs?REQUEST=GetCapabilities&amp;SERVICE=WFS&amp;VERSION=1.0.0'
        properties['dlform.pdf_url'] = '/static/'
        properties['dlform.activated'] = 'true'
  	}
	}
}