package org.eclipse.jetty.osgi.boot.mvn;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.osgi.boot.utils.BundleFileLocatorHelper;

import org.ops4j.pax.swissbox.property.BundleContextPropertyResolver;
import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.util.property.DictionaryPropertyResolver;
import org.ops4j.util.property.PropertyResolver;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public class Activator implements BundleActivator
{
    private static final Log LOG = LogFactory.getLog( Activator.class );
	
    private static Activator INSTANCE = null;
    private PropertyResolver m_propertyResolver;
    private MavenConfiguration m_configuration;
	private BundleContext m_bundleContext;
	private ServiceRegistration m_managedServiceReg;
    private ServiceRegistration m_bundleFileLocatorHelperServiceReq ;
    
    public static Activator getInstance()
    {
        return INSTANCE;
    }

    public void start(BundleContext bundleContext) throws Exception
    {
        INSTANCE = this;
        
        m_bundleContext = bundleContext;
        m_managedServiceReg = registerManagedService();
        m_bundleFileLocatorHelperServiceReq = registerBundleFileLocatorHelperService() ;
    }

    public void stop(BundleContext context) throws Exception
    {
        if ( m_managedServiceReg != null )
        {
            m_managedServiceReg.unregister();
        }
        
        if ( m_bundleFileLocatorHelperServiceReq != null )
        {
        	m_bundleFileLocatorHelperServiceReq.unregister() ;
        }

       INSTANCE = null;
    }
    
    public BundleContext getBundleContext() {
		return m_bundleContext;
	}
    
    public MavenConfiguration getMavenConfiguration() {
		return m_configuration;
	}

    synchronized PropertyResolver getResolver()
    {
        return m_propertyResolver;
    }

    /**
     * Setter.
     *
     * @param propertyResolver property resolver
     */
    synchronized void setResolver( final PropertyResolver propertyResolver )
    {
        m_propertyResolver = propertyResolver;
        m_configuration = createConfiguration( propertyResolver );
    }
    
    private MavenConfiguration createConfiguration( final PropertyResolver propertyResolver )
    {
        final MavenConfigurationImpl config = new MavenConfigurationImpl( propertyResolver, ServiceConstants.PID );
        
        // TODO: why ConfigAdmin notification not received
		URL u = config.getSettingsFileUrl() ;
		System.err.println( u ) ;
		URL uu = null ;
		try {
			uu = new URL ( "file:/c:/.m2/settings.xml" ) ;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

        config.setSettings(
            new MavenSettingsImpl( uu, config.useFallbackRepositories() )
        );
        return config;
    }
    
    private ServiceRegistration registerBundleFileLocatorHelperService() throws Exception
    {
        ServiceRegistration registration = m_bundleContext.registerService(
        		BundleFileLocatorHelper.class.getName(),
                new MavenFileLocatorHelper(m_bundleContext),
                null
          );
        
        return registration ;
    }

    private ServiceRegistration registerManagedService()
    {
        final ManagedService managedService = new ManagedService() {
            public void updated( final Dictionary config ) throws ConfigurationException
            {
                if ( config == null )
                {
                  setResolver( new BundleContextPropertyResolver( m_bundleContext ) );
                }
                else
                {
                  setResolver(
                     new DictionaryPropertyResolver(
                         config,
                          new BundleContextPropertyResolver( m_bundleContext )
                     )
                  );
                }
            }

       };
       
      final Dictionary<String, String> props = new Hashtable<String, String>();
      props.put( Constants.SERVICE_PID, ServiceConstants.PID );
      ServiceRegistration registration = m_bundleContext.registerService(
            ManagedService.class.getName(),
            managedService,
            props
      );
      
      synchronized ( this )
      {
          if ( getResolver() == null )
          {
             try
             {
                managedService.updated( null );
             }
             catch ( ConfigurationException ignore )
             {
                // this should never happen
                LOG.error( "Internal error. Cannot set initial configuration resolver.", ignore );
             }
         }
      }
      
      return registration;
   }

    
}
