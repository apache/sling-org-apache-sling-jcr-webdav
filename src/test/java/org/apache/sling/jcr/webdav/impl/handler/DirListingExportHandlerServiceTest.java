/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.jcr.webdav.impl.handler;

import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import static org.junit.Assert.assertNotNull;

public class DirListingExportHandlerServiceTest {

    @Rule
    public final OsgiContext context = new OsgiContext();

    private DirListingExportHandlerService dirListingExportHandlerService;

    private BundleContext bundleContext;

    @Before
    public void setUp() {
        bundleContext = MockOsgi.newBundleContext();
        dirListingExportHandlerService = new DirListingExportHandlerService();
        context.registerService(DirListingExportHandlerService.class, dirListingExportHandlerService);
    }

    @Test
    public void testIfServiceActive() {
        MockOsgi.activate(dirListingExportHandlerService, bundleContext);
        DirListingExportHandlerService registeredService = context.getService(DirListingExportHandlerService.class);
        assertNotNull(registeredService);

        MockOsgi.deactivate(dirListingExportHandlerService, bundleContext);
    }
}
