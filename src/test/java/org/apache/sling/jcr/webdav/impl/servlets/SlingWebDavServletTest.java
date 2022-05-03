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
package org.apache.sling.jcr.webdav.impl.servlets;

import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.mock.osgi.MockOsgi;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.apache.sling.testing.mock.sling.MockJcrSlingRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class SlingWebDavServletTest {

    @Rule
    public final OsgiContext context = new OsgiContext();

    private SlingWebDavServlet slingWebDavServlet;
    private BundleContext bundleContext;
    private SlingRepository repository;

    @Before
    public void setUp() {
        bundleContext = MockOsgi.newBundleContext();
        Dictionary<String, Object> properties = new Hashtable<>();

        properties.put("dav.root", SlingWebDavServlet.DEFAULT_CONTEXT);
        properties.put("dav.realm", SlingWebDavServlet.DEFAULT_REALM);
        properties.put("dav.create-absolute-uri", SlingWebDavServlet.DEFAULT_CREATE_ABSOLUTE_URI);
        properties.put(SlingWebDavServlet.TYPE_COLLECTIONS, SlingWebDavServlet.TYPE_COLLECTIONS_DEFAULT);
        properties.put(SlingWebDavServlet.TYPE_NONCOLLECTIONS, SlingWebDavServlet.TYPE_NONCOLLECTIONS_DEFAULT);
        properties.put(SlingWebDavServlet.TYPE_CONTENT, SlingWebDavServlet.TYPE_CONTENT_DEFAULT);

        //Mock HttpService
        HttpService httpService = Mockito.mock(HttpService.class);
        context.registerService(HttpService.class, httpService);

        //Mock MimeTypeService
        MimeTypeService mimeTypeService = Mockito.mock(MimeTypeService.class);
        context.registerService(MimeTypeService.class, mimeTypeService);

        repository = context.registerInjectActivateService(new MockJcrSlingRepository());
        slingWebDavServlet = context.registerInjectActivateService(new SlingWebDavServlet(), properties);
    }

    @Test
    public void testIfServiceActive() {
        assertNotNull(slingWebDavServlet);
    }
}