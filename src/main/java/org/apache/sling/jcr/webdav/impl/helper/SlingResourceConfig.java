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
package org.apache.sling.jcr.webdav.impl.helper;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.jackrabbit.server.io.CopyMoveManager;
import org.apache.jackrabbit.server.io.DeleteManager;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.PropertyManager;
import org.apache.jackrabbit.webdav.simple.DefaultItemFilter;
import org.apache.jackrabbit.webdav.simple.ItemFilter;
import org.apache.jackrabbit.webdav.simple.ResourceConfig;
import org.apache.jackrabbit.webdav.simple.SimpleWebdavServlet;
import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.sling.jcr.webdav.impl.servlets.SlingWebDavServlet;

public class SlingResourceConfig extends ResourceConfig {

    private final String[] collectionTypes;

    private final ItemFilter itemFilter;

    private final IOManager ioManager;

    private final PropertyManager propertyManager;

    private final CopyMoveManager copyMoveManager;

    private final DeleteManager deleteManager;

    private final String servletContextPath;

    private final Dictionary<String, String> servletInitParams;

    public SlingResourceConfig(
            MimeTypeService mimeTypeService,
            SlingWebDavServlet.Config config,
            IOManager ioManager,
            PropertyManager propertyManager,
            CopyMoveManager copyMoveManager,
            DeleteManager deleteManager) {
        super(new SlingTikaDetector(mimeTypeService));
        this.ioManager = ioManager;
        this.propertyManager = propertyManager;
        this.copyMoveManager = copyMoveManager;
        this.deleteManager = deleteManager;

        collectionTypes = config.collection_types();
        String[] filterPrefixes = config.filter_prefixes();
        String[] filterNodeTypes = config.filter_types();
        String[] filterURIs = config.filter_uris();

        itemFilter = new DefaultItemFilter();
        itemFilter.setFilteredPrefixes(filterPrefixes);
        itemFilter.setFilteredURIs(filterURIs);
        itemFilter.setFilteredNodetypes(filterNodeTypes);

        servletContextPath = config.dav_root();
        servletInitParams = new Hashtable<>();
        servletInitParams.put(SimpleWebdavServlet.INIT_PARAM_RESOURCE_PATH_PREFIX, servletContextPath);
        String value = config.dav_realm();
        servletInitParams.put(SimpleWebdavServlet.INIT_PARAM_AUTHENTICATE_HEADER, "Basic realm=\"" + value + "\"");

        boolean createAbsoluteUri = config.dav_create$_$absolute$_$uri();
        servletInitParams.put(SimpleWebdavServlet.INIT_PARAM_CREATE_ABSOLUTE_URI, Boolean.toString(createAbsoluteUri));
    }

    // ---------- ResourceConfig overwrites

    @Override
    public IOManager getIOManager() {
        return ioManager;
    }

    @Override
    public ItemFilter getItemFilter() {
        return itemFilter;
    }

    @Override
    public PropertyManager getPropertyManager() {
        return propertyManager;
    }

    @Override
    public CopyMoveManager getCopyMoveManager() {
        return copyMoveManager;
    }

    @Override
    public DeleteManager getDeleteManager() {
        return deleteManager;
    }

    @Override
    public boolean isCollectionResource(Item item) {
        if (item.isNode()) {
            Node node = (Node) item;
            for (String type : collectionTypes) {
                try {
                    if (node.isNodeType(type)) {
                        return false;
                    }
                } catch (RepositoryException re) {
                    // TODO: log and continue
                }
            }
        }

        return true;
    }

    @Override
    public void parse(URL configURL) {
        // we don't parse nothing
    }

    // ---------- SlingResourceConfig additions

    public String getServletContextPath() {
        return servletContextPath;
    }

    public Dictionary<String, String> getServletInitParams() {
        return servletInitParams;
    }
}
