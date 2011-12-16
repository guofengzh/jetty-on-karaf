package org.planner.sample.httpservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

        private HttpServiceTracker serviceTracker;

        public void start(BundleContext context) throws Exception {
                serviceTracker = new HttpServiceTracker(context);
                serviceTracker.open();
        }

        public void stop(BundleContext context) throws Exception {
                serviceTracker.close();
                serviceTracker = null;
        }

}
