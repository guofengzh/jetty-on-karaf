package org.eclipse.jetty.osgi.boot.utils;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.osgi.boot.utils.internal.DefaultFileLocatorHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference ;
import org.ops4j.pax.url.mvn.MavenArtifactResolverService;
import org.ops4j.pax.url.mvn.ServiceConstants;

/**
 * From a bundle to its location on the filesystem. Assumes the bundle is not a
 * jar.
 *
 * @author hmalphettes
 */
public class FileLocatorHelperImpl extends DefaultFileLocatorHelper
{

    public File getBundleInstallLocation(Bundle bundle) throws Exception
    {
    	String loc = bundle.getLocation() ;
    	if ( loc.startsWith( "mvn:") )
    	{
			BundleContext bundleContext = bundle.getBundleContext() ;
			String filter = "(" + MavenArtifactResolverService.URL_RESOLVER_PROTOCOL + "=" + ServiceConstants.PROTOCOL + ")" ;
			ServiceReference[] references = bundleContext.getServiceReferences(MavenArtifactResolverService.class.getName(), filter);
			if ( references != null && references.length != 0 )
			{
				MavenArtifactResolverService resolverService = (MavenArtifactResolverService)bundleContext.getService(references[0]) ;
 			    File f = resolverService.resolve( new URL( loc ) ) ;
			    bundleContext.ungetService( references[0] ) ;
                return f ;
			} else
			    return null ;
    	} else
    	   return super.getBundleInstallLocation(bundle) ;
    }
}