package org.eclipse.jetty.osgi.boot.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.jetty.osgi.boot.utils.internal.DefaultFileLocatorHelper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference ;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;
import org.ops4j.pax.url.mvn.Parser;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLocatorHelperImpl extends DefaultFileLocatorHelper
{
	private Logger log = LoggerFactory.getLogger( FileLocatorHelperImpl.class ) ;

    public File getBundleInstallLocation(Bundle bundle) throws Exception
    {
    	String loc = bundle.getLocation() ;
	
    	if ( loc.startsWith( "mvn:") )
    	{
			BundleContext bundleContext = bundle.getBundleContext() ;
			String filter = "(" + URLConstants.URL_HANDLER_PROTOCOL + "=" + ServiceConstants.PROTOCOL + ")" ;
			ServiceReference[] references = bundleContext.getServiceReferences(URLStreamHandlerService.class.getName(), filter);
			if ( references != null && references.length != 0 )
			{
				URLStreamHandlerService streamService = (URLStreamHandlerService)bundleContext.getService(references[0]) ;
				URL url = new URL( loc );
				URLConnection connection = streamService.openConnection( url ) ;
				InputStream in = connection.getInputStream() ;
				String outFileName = createFileName( bundle, url ) ;
				File outFile = bundle.getDataFile( outFileName ) ;
				if ( !outFile.exists() || outFile.lastModified() < bundle.getLastModified() ) {
				   // The cache does not exist or older than the bundle
				   saveIt( in, outFile  ) ;
				   outFile.setLastModified( bundle.getLastModified() ) ;
				   log.info( "catch {} to {}", loc, outFile.toString() ) ;
				}
				in.close() ;
 			    //File f = resolverService.resolve( new URL( loc ) ) ;
			    bundleContext.ungetService( references[0] ) ;
                return outFile ;
			} else
			    return null ;
    	} else
    	   return super.getBundleInstallLocation(bundle) ;
    }
    
    private void saveIt( InputStream in, File outFile ) throws IOException {
    	byte[] buffer = new byte[ 1024 * 100 ];
    	int len;
    	OutputStream out = new FileOutputStream( outFile ) ;
    	while ((len = in.read(buffer)) != -1) {
    	    out.write(buffer, 0, len);
    	}
    	out.close() ;
    }
    
    private String createFileName( Bundle bundle, URL url ) throws MalformedURLException
    {
		Parser parser = new Parser( url.getPath() );
		StringBuilder builder = new StringBuilder() ;
		//builder.append( parser.getGroup() ) ;
		//builder.append( "/" ) ;
		builder.append( parser.getArtifact() ) ;
		if ( parser.getClassifier() != null && !parser.getClassifier().isEmpty() )
			builder.append( "-" + parser.getClassifier() ) ;
		builder.append( "-" + parser.getVersion() ) ;
		if ( parser.getType() != null && !parser.getType().isEmpty() )
			builder.append( "." + parser.getType() ) ;

		return builder.toString() ;

    }
}