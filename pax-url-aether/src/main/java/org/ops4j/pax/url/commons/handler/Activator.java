/*
 * Copyright 2007 Alin Dreghiciu.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.url.commons.handler ;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.ops4j.pax.url.commons.handler.ConnectionFactory;
import org.ops4j.pax.url.commons.handler.HandlerActivator;
import org.ops4j.pax.url.maven.commons.MavenConfiguration;
import org.ops4j.pax.url.maven.commons.MavenConfigurationImpl;
import org.ops4j.pax.url.maven.commons.MavenSettingsImpl;
import org.ops4j.pax.url.mvn.MavenArtifactResolverService;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.pax.url.mvn.internal.MavenArtifactResolverServiceImpl;
import org.ops4j.pax.url.mvn.internal.Connection;
import org.ops4j.util.property.PropertyResolver;

/**
 * Bundle activator for mvn: protocol handler
 */
public final class Activator
    extends HandlerActivator<MavenConfiguration>
{
	
    /**
     * Bundle context in use.
     */
    private BundleContext m_bundleContext;
    
    private ServiceRegistration m_resolverReg;

    static private ConnectionFactory<MavenConfiguration> m_connectionFactory;
    private MavenArtifactResolverServiceImpl resolver ;
    
    public Activator()
    {
        super(
            new String[]{ ServiceConstants.PROTOCOL },
            ServiceConstants.PID,
            newConnectionFactory()
        );
        resolver = new MavenArtifactResolverServiceImpl() ;
    }
    
    public void start( final BundleContext bundleContext )
    {
    	super.start( bundleContext ) ;
        m_bundleContext = bundleContext;
        registerResolver();
    }
    
    public void stop( final BundleContext bundleContext )
    {
    	super.stop( bundleContext ) ;
        if ( m_resolverReg != null )
        {
        	m_resolverReg.unregister();
        }
        m_bundleContext = null;
    }
    
    synchronized void setResolver( final PropertyResolver propertyResolver )
    {
        try {
        	super.setResolver(propertyResolver) ;
			resolver.setupResolver( m_connectionFactory.createConfiguration( getResolver() ) ) ;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
    }
    
    private void registerResolver()
    {
        final Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put( MavenArtifactResolverService.URL_RESOLVER_PROTOCOL, new String[] { ServiceConstants.PROTOCOL } );
        m_resolverReg = m_bundleContext.registerService(
        		MavenArtifactResolverService.class.getName(),
            resolver,
            props
        );
    }

    static ConnectionFactory<MavenConfiguration> newConnectionFactory()
    {
    	m_connectionFactory = new ConnectionFactory<MavenConfiguration>()
    	        {
    	            /**
    	             * @see ConnectionFactory#createConection(BundleContext, URL, Object)
    	             */
    	            public URLConnection createConection( final BundleContext bundleContext,
    	                                                  final URL url,
    	                                                  final MavenConfiguration config )
    	                throws MalformedURLException
    	            {
    	                return new Connection( url, config );
    	            }

    	            /**
    	             * @see ConnectionFactory#createConfiguration(org.ops4j.util.property.PropertyResolver)
    	             */
    	            public MavenConfiguration createConfiguration( final PropertyResolver propertyResolver )
    	            {
    	                final MavenConfigurationImpl config =
    	                    new MavenConfigurationImpl( propertyResolver, ServiceConstants.PID );
    	                config.setSettings(
    	                    new MavenSettingsImpl( config.getSettingsFileUrl(), config.useFallbackRepositories() )
    	                );
    	                return config;
    	            }


    	        } ; 
    	 return m_connectionFactory ;
    }

}