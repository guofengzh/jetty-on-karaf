package org.eclipse.jetty.osgi.boot.mvn;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.jetty.osgi.boot.utils.BundleFileLocatorHelper;
import org.ops4j.pax.url.mvn.MavenArtifactResolverService;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference ;

public class MavenFileLocatorHelper implements BundleFileLocatorHelper
{
	private BundleContext m_bundleContext ;

	public MavenFileLocatorHelper(BundleContext bundleContext) throws MalformedURLException
	{
		m_bundleContext = bundleContext ;
	}
    /*
     * @param bundle
     *            The bundle
     * @return Its installation location as a file.
     * @throws Exception
     */
    public File getBundleInstallLocation(Bundle bundle) throws Exception
    {
    	String loc = bundle.getLocation() ;
    	if ( loc.startsWith( "mvn:") )
    	{
			String filter = "(" + MavenArtifactResolverService.URL_RESOLVER_PROTOCOL + "=" + ServiceConstants.PROTOCOL + ")" ;
			ServiceReference[] references = m_bundleContext.getServiceReferences(MavenArtifactResolverService.class.getName(), filter);
			if ( references != null && references.length != 0 )
			{
				MavenArtifactResolverService resolverService = (MavenArtifactResolverService)m_bundleContext.getService(references[0]) ;
 			    File f = resolverService.resolve( new URL( loc ) ) ;
			    m_bundleContext.ungetService( references[0] ) ;
                return f ;
			}
    	}
		return null ;
    }

    public File getFileInBundle(Bundle bundle, String path) throws Exception
    {
    	throw new RuntimeException( "getFileInBundle is not supported here" ) ;
    }

	public Enumeration<URL> findEntries(Bundle bundle, String entryPath)
	{
    	throw new RuntimeException( "findEntries is not supported here" ) ;
	}

    public File[] locateJarsInsideBundle(Bundle bundle) throws Exception
    {
    	throw new RuntimeException( "locateJarsInsideBundle is not supported here" ) ;

    }
}
