package org.eclipse.jetty.osgi.boot.mvn;

import org.eclipse.jetty.osgi.boot.utils.BundleFileLocatorHelper;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator
{
	private BundleContext m_bundleContext;
    private ServiceRegistration m_bundleFileLocatorHelperServiceReq ;

    public void start(BundleContext bundleContext) throws Exception
    {
        m_bundleContext = bundleContext;
        m_bundleFileLocatorHelperServiceReq = registerBundleFileLocatorHelperService() ;
    }

    public void stop(BundleContext context) throws Exception
    {
        if ( m_bundleFileLocatorHelperServiceReq != null )
        {
        	m_bundleFileLocatorHelperServiceReq.unregister() ;
        }
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
}
