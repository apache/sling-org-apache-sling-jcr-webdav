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

import java.io.IOException;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.jackrabbit.server.SessionProvider;
import org.apache.jackrabbit.server.io.CopyMoveHandler;
import org.apache.jackrabbit.server.io.DeleteHandler;
import org.apache.jackrabbit.server.io.IOHandler;
import org.apache.jackrabbit.server.io.PropertyHandler;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavLocatorFactory;
import org.apache.jackrabbit.webdav.WebdavRequest;
import org.apache.jackrabbit.webdav.WebdavResponse;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.webdav.impl.handler.SlingCopyMoveManager;
import org.apache.sling.jcr.webdav.impl.handler.SlingDeleteManager;
import org.apache.sling.jcr.webdav.impl.handler.SlingIOManager;
import org.apache.sling.jcr.webdav.impl.handler.SlingPropertyManager;
import org.apache.sling.jcr.webdav.impl.helper.SlingLocatorFactory;
import org.apache.sling.jcr.webdav.impl.helper.SlingResourceConfig;
import org.apache.sling.jcr.webdav.impl.helper.SlingSessionProvider;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.component.propertytypes.ServiceVendor;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * The <code>SlingWebDavServlet</code> implements the WebDAV protocol as a
 * default servlet for Sling handling all WebDAV methods.
 *
 */
@Component(
        name = "org.apache.sling.jcr.webdav.impl.servlets.SimpleWebDavServlet",
        configurationPolicy = ConfigurationPolicy.REQUIRE,
        service = { Servlet.class },
        property = {
                "sling.servlet.resourceTypes=sling/servlet/default",
                "sling.servlet.methods=*"
        },
        reference = {
                @Reference( name = SlingWebDavServlet.IOHANDLER_REF_NAME, service = IOHandler.class,
                        cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
                        bind = "bindIOHandler", unbind = "unbindIOHandler"),
                @Reference( name = SlingWebDavServlet.PROPERTYHANDLER_REF_NAME, service = PropertyHandler.class,
                        cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
                        bind = "bindPropertyHandler", unbind = "unbindPropertyHandler"),
                @Reference( name = SlingWebDavServlet.COPYMOVEHANDLER_REF_NAME, service = CopyMoveHandler.class,
                        cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
                        bind = "bindCopyMoveHandler", unbind = "unbindCopyMoveHandler"),
                @Reference( name = SlingWebDavServlet.DELETEHANDLER_REF_NAME, service = DeleteHandler.class,
                        cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC,
                        bind = "bindDeleteHandler", unbind = "unbindDeleteHandler")

        })
@ServiceDescription("Sling WebDAV Servlet")
@ServiceVendor("The Apache Software Foundation")
@Designate(ocd = SlingWebDavServlet.Config.class)
public class SlingWebDavServlet extends SimpleWebdavServlet {

    public static final String DEFAULT_CONTEXT = "/dav";
    public static final boolean DEFAULT_CREATE_ABSOLUTE_URI = true;
    public static final String DEFAULT_REALM = "Sling WebDAV";
    public static final String TYPE_NONCOLLECTIONS_DEFAULT = "nt:file";
    public static final String TYPE_CONTENT_DEFAULT = "nt:resource";
    public static final String TYPE_COLLECTIONS_DEFAULT = "sling:Folder";
    public static final String TYPE_COLLECTIONS = "type.collections";
    public static final String TYPE_NONCOLLECTIONS = "type.noncollections";
    public static final String TYPE_CONTENT = "type.content";

    static final String IOHANDLER_REF_NAME = "IOHandler";
    static final String PROPERTYHANDLER_REF_NAME = "PropertyHandler";
    static final String COPYMOVEHANDLER_REF_NAME = "CopyMoveHandler";
    static final String DELETEHANDLER_REF_NAME = "DeleteHandler";

    @SuppressWarnings("java:S100")
    @ObjectClassDefinition(name = "%dav.name", description = "%dav.description")
    public @interface Config {

        @AttributeDefinition(name = "%dav.root.name", description = "%dav.root.description")
        String dav_root() default DEFAULT_CONTEXT;

        @AttributeDefinition(name = "%dav.create-absolute-uri.name", description = "%dav.create-absolute-uri.description")
        boolean dav_create$_$absolute$_$uri() default DEFAULT_CREATE_ABSOLUTE_URI;

        @AttributeDefinition(name = "%dav.realm.name", description = "%dav.realm.description")
        String dav_realm() default DEFAULT_REALM;

        @AttributeDefinition(name = "%collection.types.name", description = "%collection.types.description")
        String[] collection_types() default {TYPE_NONCOLLECTIONS_DEFAULT, TYPE_CONTENT_DEFAULT};

        @AttributeDefinition(name = "%filter.prefixes.name", description = "%filter.prefixes.description")
        String[] filter_prefixes() default {"rep", "jcr"};

        @AttributeDefinition(name = "%filter.types.name", description = "%filter.types.description")
        String[] filter_types() default {};

        @AttributeDefinition(name = "%filter.uris.name", description = "%filter.uris.description")
        String[] filter_uris() default {};

        @AttributeDefinition(name = "%type.collections.name", description = "%type.collections.description")
        String type_collections() default TYPE_COLLECTIONS_DEFAULT;

        @AttributeDefinition(name = "%type.noncollections.name", description = "%type.noncollections.description")
        String type_noncollections() default TYPE_NONCOLLECTIONS_DEFAULT;

        @AttributeDefinition(name = "%type.content.name", description = "%type.content.description")
        String type_content() default TYPE_CONTENT_DEFAULT;
    }

    @Reference
    private SlingRepository repository;

    @Reference
    private HttpService httpService;

    @Reference
    private MimeTypeService mimeTypeService;

    private final SlingIOManager ioManager = new SlingIOManager(IOHANDLER_REF_NAME);

    private final SlingPropertyManager propertyManager = new SlingPropertyManager(PROPERTYHANDLER_REF_NAME);

    private final SlingCopyMoveManager copyMoveManager = new SlingCopyMoveManager(COPYMOVEHANDLER_REF_NAME);

    private final SlingDeleteManager deleteManager = new SlingDeleteManager(DELETEHANDLER_REF_NAME);

    private SlingResourceConfig resourceConfig;

    private DavLocatorFactory locatorFactory;

    private SessionProvider sessionProvider;

    private boolean simpleWebDavServletRegistered;

    // ---------- SimpleWebdavServlet overwrites -------------------------------

    @Override
    public void init() throws ServletException {
        super.init();

        setResourceConfig(resourceConfig);
    }

    @Override
    public Repository getRepository() {
        return repository;
    }

    @Override
    public DavLocatorFactory getLocatorFactory() {
        if (locatorFactory == null) {

            // configured default workspace name
            SlingRepository slingRepo = (SlingRepository) getRepository();
            String workspace = slingRepo.getDefaultWorkspace();

            // no configuration, try to login and acquire the default name
            if (workspace == null || workspace.length() == 0) {
                Session tmp = null;
                try {
                    tmp = slingRepo.login();
                    workspace = tmp.getWorkspace().getName();
                } catch (Throwable t) {
                    // TODO: log !!
                    workspace = "default"; // fall back name
                } finally {
                    if (tmp != null) {
                        tmp.logout();
                    }
                }
            }

            locatorFactory = new SlingLocatorFactory(workspace);
        }
        return locatorFactory;
    }

    @Override
    public synchronized SessionProvider getSessionProvider() {
        if (sessionProvider == null) {
            sessionProvider = new SlingSessionProvider();
        }
        return sessionProvider;
    }

    @Activate
    protected void activate(ComponentContext context, Config config)
            throws NamespaceException, ServletException {

        this.ioManager.setComponentContext(context);
        this.propertyManager.setComponentContext(context);
        this.copyMoveManager.setComponentContext(context);
        this.deleteManager.setComponentContext(context);

        resourceConfig = new SlingResourceConfig(mimeTypeService,
                config,
                ioManager,
                propertyManager,
                copyMoveManager,
                deleteManager);

        // Register servlet, and set the contextPath field to signal successful registration
        Servlet simpleServlet = new SlingSimpleWebDavServlet(resourceConfig, getRepository());
        httpService.registerServlet(resourceConfig.getServletContextPath(),
            simpleServlet, resourceConfig.getServletInitParams(), null);
        simpleWebDavServletRegistered = true;
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {

        if (simpleWebDavServletRegistered) {
            httpService.unregister(resourceConfig.getServletContextPath());
            simpleWebDavServletRegistered = false;
        }

        this.resourceConfig = null;
        this.ioManager.setComponentContext(null);
        this.propertyManager.setComponentContext(null);
        this.copyMoveManager.setComponentContext(null);
        this.deleteManager.setComponentContext(null);
    }

    public void bindIOHandler(final ServiceReference ioHandlerReference) {
        this.ioManager.bindIOHandler(ioHandlerReference);
    }

    public void unbindIOHandler(final ServiceReference ioHandlerReference) {
        this.ioManager.unbindIOHandler(ioHandlerReference);
    }

    public void bindPropertyHandler(final ServiceReference propertyHandlerReference) {
        this.propertyManager.bindPropertyHandler(propertyHandlerReference);
    }

    public void unbindPropertyHandler(final ServiceReference propertyHandlerReference) {
        this.propertyManager.unbindPropertyHandler(propertyHandlerReference);
    }

    public void bindCopyMoveHandler(final ServiceReference copyMoveHandlerReference) {
        this.copyMoveManager.bindCopyMoveHandler(copyMoveHandlerReference);
    }

    public void unbindCopyMoveHandler(final ServiceReference copyMoveHandlerReference) {
        this.copyMoveManager.unbindCopyMoveHandler(copyMoveHandlerReference);
    }

    public void bindDeleteHandler(final ServiceReference deleteHandlerReference) {
        this.deleteManager.bindDeleteHandler(deleteHandlerReference);
    }

    public void unbindDeleteHandler(final ServiceReference deleteHandlerReference) {
        this.deleteManager.unbindDeleteHandler(deleteHandlerReference);
    }

    /** Overridden as the base class uses sendError that we don't want (SLING-2443) */
    @Override
    protected void sendUnauthorized(WebdavRequest request, WebdavResponse response, DavException error) throws IOException {
        response.setHeader("WWW-Authenticate", getAuthenticateHeaderValue());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (error != null) {
            response.getWriter().write(error.getStatusPhrase());
            response.getWriter().write("\n");
        }
        response.getWriter().flush();
    }
}
