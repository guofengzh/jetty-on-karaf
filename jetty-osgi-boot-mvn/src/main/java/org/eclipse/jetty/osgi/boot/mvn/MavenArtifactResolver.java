package org.eclipse.jetty.osgi.boot.mvn;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.BundleContext;

public class MavenArtifactResolver {
	
	private MavenConfiguration m_configuration ;
	private BundleContext m_bundleContext ;

    private Parser m_parser;
    private AetherBasedResolver m_aetherBasedResolver;
    
	public MavenArtifactResolver(final URL url, BundleContext bundleContext ) throws MalformedURLException
	{
		m_bundleContext = bundleContext ;
		/*
		PropertyResolver propertyResolver =new BundleContextPropertyResolver( m_bundleContext ) ;
		MavenConfigurationImpl config = new MavenConfigurationImpl( propertyResolver, ServiceConstants.PID );
		URL u = config.getSettingsFileUrl() ;
		System.err.println( u ) ;
		URL uu = new URL ( "file:/c:/.m2/settings.xml" ) ;
		config.setSettings(new MavenSettingsImpl( uu, config.useFallbackRepositories() ) );
        m_configuration = config ;
*/
		m_configuration = Activator.getInstance().getMavenConfiguration() ;
        m_parser = new Parser( url.getPath() );
        m_aetherBasedResolver = new AetherBasedResolver( m_configuration );
        
	}

	public File resolve() throws IOException
	{
        return m_aetherBasedResolver.resolve( m_parser.getGroup(), m_parser.getArtifact(), m_parser.getClassifier(), m_parser.getType(), m_parser.getVersion() );
	}
}
