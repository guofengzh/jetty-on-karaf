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

import org.apache.felix.http.base.internal.AbstractHttpActivator;
import org.eclipse.jetty.server.handler.ContextHandler;

public final class JettyHttpServiceActivator extends AbstractHttpActivator
{
    private JettyContextHandlerServiceTracker _jettyContextHandlerTracker;

    protected void doStart() throws Exception
    {
        super.doStart();

        _jettyContextHandlerTracker = new JettyContextHandlerServiceTracker(getBundleContext(), getDispatcherServlet());

        // the tracker in charge of the actual deployment
        // and that will configure and start the jetty server.
        getBundleContext().addServiceListener(_jettyContextHandlerTracker,"(objectclass=" + ContextHandler.class.getName() + ")");
    }

    protected void doStop() throws Exception
    {
        if (_jettyContextHandlerTracker != null)
        {
            _jettyContextHandlerTracker.stop();
            getBundleContext().removeServiceListener(_jettyContextHandlerTracker);
            _jettyContextHandlerTracker = null;
        }
    }
}
