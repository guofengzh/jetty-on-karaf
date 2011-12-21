package org.eclipse.jetty.osgi.boot.mvn;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.jetty.osgi.boot.utils.BundleFileLocatorHelper;
import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.pax.url.mvn.internal.Parser;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class MavenFileLocatorHelper implements BundleFileLocatorHelper
{
	private MavenConfiguration m_configuration ;
	private BundleContext m_bundleContext ;

//    private Parser m_parser;
    private AetherBasedResolver m_aetherBasedResolver;

	public MavenFileLocatorHelper(BundleContext bundleContext) throws MalformedURLException
	{
		m_bundleContext = bundleContext ;
/*		PropertyResolver propertyResolver =new BundleContextPropertyResolver( m_bundleContext ) ;
		MavenConfigurationImpl config = new MavenConfigurationImpl( propertyResolver, ServiceConstants.PID );
		URL u = config.getSettingsFileUrl() ;
		System.err.println( u ) ;
		URL uu = new URL ( "file:/c:/.m2/settings.xml" ) ;
		config.setSettings(new MavenSettingsImpl( uu, config.useFallbackRepositories() ) );
        m_configuration = config ;

        m_aetherBasedResolver = new MyResolver( m_configuration );
*/
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
    		MavenArtifactResolver a = new MavenArtifactResolver( new URL( loc ), m_bundleContext ) ; ///****************
    		return a.resolve() ;
    	} else
    		return null ;
    }

    /**
     * Locate a file inside a bundle.
     *
     * @param bundle
     * @param path
     * @return file object
     * @throws Exception
     */
    public File getFileInBundle(Bundle bundle, String path) throws Exception
    {
        if (path != null && path.length() > 0 && path.charAt(0) == '/')
        {
            path = path.substring(1);
        }
        File bundleInstall = getBundleInstallLocation(bundle);
        File webapp = path != null && path.length() != 0?new File(bundleInstall,path):bundleInstall;
        if (!webapp.exists())
        {
            throw new IllegalArgumentException("Unable to locate " + path + " inside " + bundle.getSymbolicName() + " ("
                    + (bundleInstall != null?bundleInstall.getAbsolutePath():" no_bundle_location ") + ")");
        }
        return webapp;
    }

    /**
	 * Helper method equivalent to Bundle#getEntry(String entryPath) except that
	 * it searches for entries in the fragments by using the Bundle#findEntries method.
	 *
	 * @param bundle
	 * @param entryPath
	 * @return null or all the entries found for that path.
	 */
	public Enumeration<URL> findEntries(Bundle bundle, String entryPath)
	{
    	int last = entryPath.lastIndexOf('/');
    	String path = last != -1 && last < entryPath.length() -2
    		? entryPath.substring(0, last) : "/";
    	if (!path.startsWith("/"))
    	{
    		path = "/" + path;
    	}
    	String pattern = last != -1 && last < entryPath.length() -2
			? entryPath.substring(last+1) : entryPath;
		Enumeration<URL> enUrls = bundle.findEntries(path, pattern, false);
		return enUrls;
	}

    /**
     * If the bundle is a jar, returns the jar. If the bundle is a folder, look
     * inside it and search for jars that it returns.
     * <p>
     * Good enough for our purpose (TldLocationsCache when it scans for tld
     * files inside jars alone. In fact we only support the second situation for
     * development purpose where the bundle was imported in pde and the classes
     * kept in a jar.
     * </p>
     *
     * @param bundle
     * @return The jar(s) file that is either the bundle itself, either the jars
     *         embedded inside it.
     */
    public File[] locateJarsInsideBundle(Bundle bundle) throws Exception
    {
        File jasperLocation = getBundleInstallLocation(bundle);
        if (jasperLocation.isDirectory())
        {
            // try to find the jar files inside this folder
            ArrayList<File> urls = new ArrayList<File>();
            for (File f : jasperLocation.listFiles())
            {
                if (f.getName().endsWith(".jar") && f.isFile())
                {
                    urls.add(f);
                }
                else if (f.isDirectory() && f.getName().equals("lib"))
                {
                    for (File f2 : jasperLocation.listFiles())
                    {
                        if (f2.getName().endsWith(".jar") && f2.isFile())
                        {
                            urls.add(f2);
                        }
                    }
                }
            }
            return urls.toArray(new File[urls.size()]);
        }
        else
        {
            return new File[] { jasperLocation };
        }
    }


	//introspection on equinox to invoke the getLocalURL method on BundleURLConnection
    //equivalent to using the FileLocator without depending on an equinox class.
	private static Method BUNDLE_URL_CONNECTION_getLocalURL = null;
	private static Method BUNDLE_URL_CONNECTION_getFileURL = null;
	/**
	 * Only useful for equinox: on felix we get the file:// or jar:// url already.
	 * Other OSGi implementations have not been tested
	 * <p>
	 * Get a URL to the bundle entry that uses a common protocol (i.e. file:
	 * jar: or http: etc.).
	 * </p>
	 * @return a URL to the bundle entry that uses a common protocol
	 */
	public static URL getLocalURL(URL url) {
		if ("bundleresource".equals(url.getProtocol()) || "bundleentry".equals(url.getProtocol())) {
			try {
				URLConnection conn = url.openConnection();
				if (BUNDLE_URL_CONNECTION_getLocalURL == null &&
						conn.getClass().getName().equals(
								"org.eclipse.osgi.framework.internal.core.BundleURLConnection")) {
					BUNDLE_URL_CONNECTION_getLocalURL = conn.getClass().getMethod("getLocalURL", null);
					BUNDLE_URL_CONNECTION_getLocalURL.setAccessible(true);
				}
				if (BUNDLE_URL_CONNECTION_getLocalURL != null) {
					return (URL)BUNDLE_URL_CONNECTION_getLocalURL.invoke(conn, null);
				}
			} catch (Throwable t) {
				System.err.println("Unable to locate the OSGi url: '" + url + "'.");
				t.printStackTrace();
			}
		}
		return url;
	}
	/**
	 * Only useful for equinox: on felix we get the file:// url already.
	 * Other OSGi implementations have not been tested
	 * <p>
	 * Get a URL to the content of the bundle entry that uses the file: protocol.
	 * The content of the bundle entry may be downloaded or extracted to the local
	 * file system in order to create a file: URL.
	 * @return a URL to the content of the bundle entry that uses the file: protocol
	 * </p>
	 */
	public static URL getFileURL(URL url)
	{
		if ("bundleresource".equals(url.getProtocol()) || "bundleentry".equals(url.getProtocol()))
		{
			try
			{
				URLConnection conn = url.openConnection();
				if (BUNDLE_URL_CONNECTION_getFileURL == null &&
						conn.getClass().getName().equals(
								"org.eclipse.osgi.framework.internal.core.BundleURLConnection"))
				{
					BUNDLE_URL_CONNECTION_getFileURL = conn.getClass().getMethod("getFileURL", null);
					BUNDLE_URL_CONNECTION_getFileURL.setAccessible(true);
				}
				if (BUNDLE_URL_CONNECTION_getFileURL != null)
				{
					return (URL)BUNDLE_URL_CONNECTION_getFileURL.invoke(conn, null);
				}
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		return url;
	}

}
