package org.eclipse.jetty.osgi.boot.utils;

import java.io.File;
import java.net.URL;

import org.eclipse.jetty.osgi.boot.utils.internal.DefaultFileLocatorHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference ;
import org.osgi.service.url.URLConstants;
import org.ops4j.pax.url.mvn.Mvn2FileResolverService;
import org.ops4j.pax.url.mvn.ServiceConstants;

public class FileLocatorHelperImpl extends DefaultFileLocatorHelper
{

    public File getBundleInstallLocation(Bundle bundle) throws Exception
    {
    	String loc = bundle.getLocation() ;
    	if ( loc.startsWith( "mvn:") )
    	{
			BundleContext bundleContext = bundle.getBundleContext() ;
			String filter = "(" + URLConstants.URL_HANDLER_PROTOCOL + "=" + ServiceConstants.PROTOCOL + ")" ;
			ServiceReference[] references = bundleContext.getServiceReferences(Mvn2FileResolverService.class.getName(), filter);
			if ( references != null && references.length != 0 )
			{
				Mvn2FileResolverService resolverService = (Mvn2FileResolverService)bundleContext.getService(references[0]) ;
 			    File f = resolverService.resolve( new URL( loc ) ) ;
			    bundleContext.ungetService( references[0] ) ;
                return f ;
			} else
			    return null ;
    	} else
    	   return super.getBundleInstallLocation(bundle) ;
    }
}