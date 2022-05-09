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

import org.apache.jackrabbit.server.io.CopyMoveHandler;
import org.apache.jackrabbit.server.io.DeleteHandler;
import org.apache.jackrabbit.server.io.IOHandler;
import org.apache.jackrabbit.server.io.PropertyHandler;
import org.apache.sling.jcr.webdav.impl.servlets.SlingWebDavServlet;
import org.apache.sling.testing.mock.osgi.junit.OsgiContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertNotNull;

public class DefaultHandlerServiceTest {

    @Rule
    public final OsgiContext context = new OsgiContext();

    private DefaultHandlerService defaultHandlerService;

    @Before
    public void setUp() {
        defaultHandlerService = new DefaultHandlerService();
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(SlingWebDavServlet.TYPE_COLLECTIONS, SlingWebDavServlet.TYPE_COLLECTIONS_DEFAULT);
        properties.put(SlingWebDavServlet.TYPE_NONCOLLECTIONS, SlingWebDavServlet.TYPE_NONCOLLECTIONS_DEFAULT);
        properties.put(SlingWebDavServlet.TYPE_CONTENT, SlingWebDavServlet.TYPE_CONTENT_DEFAULT);

        context.registerInjectActivateService(DefaultHandlerService.class, defaultHandlerService, properties);
    }

    @Test
    public void testIfServiceActive() {
        assertNotNull(context.getService(CopyMoveHandler.class));
        assertNotNull(context.getService(PropertyHandler.class));
        assertNotNull(context.getService(IOHandler.class));
        assertNotNull(context.getService(DeleteHandler.class));
    }
}