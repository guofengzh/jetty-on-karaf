/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.jetty.httpservice;

import org.apache.felix.http.base.internal.DispatcherServlet;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class JettyContextHandlerServiceTracker implements ServiceListener
{
    private DispatcherServlet dispatcher;
    private final BundleContext context;

    private static String SERVICE_PROP_CONTEXT_PATH = "contextPath" ;

    public JettyContextHandlerServiceTracker(BundleContext context, DispatcherServlet dispatcher)
    {
        this.context = context;
        this.dispatcher = dispatcher;
    }

    public void stop() throws Exception
    {
    }

    /**
     * Receives notification that a service has had a lifecycle change.
     *
     * @param ev
     *            The <code>ServiceEvent</code> object.
     */
    public void serviceChanged(ServiceEvent ev)
    {
        ServiceReference sr = ev.getServiceReference();
        switch (ev.getType())
        {
            case ServiceEvent.MODIFIED:
            case ServiceEvent.UNREGISTERING:
            {
            	String contextPath = (String)sr.getProperty( SERVICE_PROP_CONTEXT_PATH ) ;
            	if ( "/".equals( contextPath ) )
                	dispatcher.destroy() ;
            }
            if (ev.getType() == ServiceEvent.UNREGISTERING)
            {
                break;
            }
            else
            {
                // modified, meaning: we reload it. now that we stopped it;
                // we can register it.
            }
            case ServiceEvent.REGISTERED:
            {
            	String contextPath = (String)sr.getProperty( SERVICE_PROP_CONTEXT_PATH ) ;
            	if ( "/".equals( contextPath ) )
            	{
                    ContextHandler contextHandler = (ContextHandler)context.getService(sr);
                    ((WebAppContext)contextHandler).addServlet(new ServletHolder(dispatcher),"/*");
            	}

                break ;
            }
        }
    }
}
