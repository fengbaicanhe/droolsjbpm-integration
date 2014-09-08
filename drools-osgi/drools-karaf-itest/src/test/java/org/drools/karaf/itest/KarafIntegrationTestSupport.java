/*
 * Copyright 2012 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.drools.karaf.itest;

import org.apache.camel.CamelContext;
import org.apache.camel.osgi.CamelContextFactory;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.MavenArtifactProvisionOption;
import org.ops4j.pax.exam.options.UrlReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

public class KarafIntegrationTestSupport extends CamelTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(KarafIntegrationTestSupport.class);
    private static final String CamelVersion = "2.10.3";
    protected static final String DroolsVersion;
    static { 
        Properties testProps = new Properties();
        try {
            testProps.load(KieSpringIntegrationTestSupport.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize DroolsVersion property: " + e.getMessage(), e);
        }
        DroolsVersion = testProps.getProperty("project.version");
    }
    
    @Inject
    protected BundleContext bundleContext;

    protected Bundle getInstalledBundle(String symbolicName) {
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(symbolicName)) {
                return b;
            }
        }
        for (Bundle b : bundleContext.getBundles()) {
            LOG.warn("Bundle: " + b.getSymbolicName());
        }
        throw new RuntimeException("Bundle " + symbolicName + " does not exist");
    }

    protected CamelContext createCamelContext() throws Exception {
        LOG.info("Get the bundleContext is " + bundleContext);
        LOG.info("Application installed as bundle id: " + bundleContext.getBundle().getBundleId());

        setThreadContextClassLoader();

        CamelContextFactory factory = new CamelContextFactory();
        factory.setBundleContext(bundleContext);
        factory.setRegistry(createRegistry());
        return factory.createContext();
    }

    protected void setThreadContextClassLoader() {
        // set the thread context classloader current bundle classloader
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
    }

    public static MavenArtifactProvisionOption getFeatureUrl(String groupId, String camelId) {
        return mavenBundle().groupId(groupId).artifactId(camelId);
    }

    public static UrlReference getCamelKarafFeatureUrl(String version) {
        String type = "xml";
        String classifier = "features";
        MavenArtifactProvisionOption mavenOption = getFeatureUrl("org.apache.camel.karaf", "apache-camel");
        if (version == null) {
            return mavenOption.versionAsInProject().type(type).classifier(classifier);
        } else {
            return mavenOption.version(version).type(type).classifier(classifier);
        }
    }

    public static Option loadCamelFeatures(String... features) {
        List<String> result = new ArrayList<String>();
        result.add("camel-core");
        result.add("camel-spring");
        result.add("camel-test");
        for (String feature : features) {
            result.add(feature);
        }
        return features(getCamelKarafFeatureUrl(CamelVersion), result.toArray(new String[4 + features.length]));
    }

    public static Option loadDroolsFeatures(String... features) {
        List<String> result = new ArrayList<String>();
        result.add("drools-module");
        for (String feature : features) {
            result.add(feature);
        }
        return features(getFeatureUrl("org.drools", "drools-karaf-features").type("xml").classifier("features").version(DroolsVersion), result.toArray(new String[4 + features.length]));
    }

}
