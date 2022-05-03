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

import org.apache.jackrabbit.server.io.DefaultHandler;
import org.apache.jackrabbit.server.io.DeleteContext;
import org.apache.jackrabbit.server.io.DeleteHandler;
import org.apache.jackrabbit.server.io.ExportContext;
import org.apache.jackrabbit.server.io.IOHandler;
import org.apache.jackrabbit.server.io.IOManager;
import org.apache.jackrabbit.server.io.ImportContext;
import org.apache.jackrabbit.server.io.PropertyExportContext;
import org.apache.jackrabbit.server.io.PropertyHandler;
import org.apache.jackrabbit.server.io.PropertyImportContext;
import org.apache.jackrabbit.server.io.CopyMoveHandler;
import org.apache.jackrabbit.server.io.CopyMoveContext;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavResource;
import org.apache.jackrabbit.webdav.property.PropEntry;
import org.apache.sling.jcr.webdav.impl.servlets.SlingWebDavServlet;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.io.IOException;
import java.util.Map;

import javax.jcr.RepositoryException;

/**
 * Wraps {@link org.apache.jackrabbit.server.io.DefaultHandler} in order to run
 * it as a service.
 */
@Component(
        immediate = true,
        service = {DefaultHandlerService.class,
                IOHandler.class,
                PropertyHandler.class,
                CopyMoveHandler.class,
                DeleteHandler.class
        })
@ServiceRanking(1000)
@Designate(ocd = DefaultHandlerService.Config.class)
public class DefaultHandlerService implements IOHandler, PropertyHandler, CopyMoveHandler, DeleteHandler {

    private DefaultHandler delegatee;

    @SuppressWarnings("java:S100")
    @ObjectClassDefinition(name = "%defaulthandler.name", description = "%defaulthandler.description")
    public @interface Config {

        @AttributeDefinition(name = "%type.collections.name", description = "%type.collections.description")
        String type_collections() default SlingWebDavServlet.TYPE_COLLECTIONS_DEFAULT;

        @AttributeDefinition(name = "%type.noncollections.name", description = "%type.noncollections.description")
        String type_noncollections() default SlingWebDavServlet.TYPE_NONCOLLECTIONS_DEFAULT;

        @AttributeDefinition(name = "%type.content.name", description = "%type.content.description")
        String type_content() default SlingWebDavServlet.TYPE_CONTENT_DEFAULT;
    }

    @Activate
    @Modified
    @SuppressWarnings("unused")
    private void activate(final Config config) {
        final String collectionType = config.type_collections();
        final String nonCollectionType = config.type_noncollections();
        final String contentType = config.type_content();

        this.delegatee = new DefaultHandler(null, collectionType, nonCollectionType, contentType);
    }

    @Deactivate
    @SuppressWarnings("unused")
    private void deactivate() {
        this.delegatee = null;
    }

    public IOManager getIOManager() {
        return delegatee.getIOManager();
    }

    public void setIOManager(IOManager ioManager) {
        delegatee.setIOManager(ioManager);
    }

    public String getName() {
        return delegatee.getName();
    }

    public boolean canImport(ImportContext context, boolean isCollection) {
        return delegatee.canImport(context, isCollection);
    }

    public boolean canImport(ImportContext context, DavResource resource) {
        return delegatee.canImport(context, resource);
    }

    public boolean importContent(ImportContext context, boolean isCollection)
            throws IOException {
        return delegatee.importContent(context, isCollection);
    }

    public boolean importContent(ImportContext context, DavResource resource)
            throws IOException {
        return delegatee.importContent(context, resource);
    }

    public boolean canExport(ExportContext context, boolean isCollection) {
        return delegatee.canExport(context, isCollection);
    }

    public boolean canExport(ExportContext context, DavResource resource) {
        return delegatee.canExport(context, resource);
    }

    public boolean exportContent(ExportContext context, boolean isCollection)
            throws IOException {
        return delegatee.exportContent(context, isCollection);
    }

    public boolean exportContent(ExportContext context, DavResource resource)
            throws IOException {
        return delegatee.exportContent(context, resource);
    }

    public boolean canExport(PropertyExportContext context, boolean isCollection) {
        return delegatee.canExport(context, isCollection);
    }

    public boolean exportProperties(PropertyExportContext exportContext,
            boolean isCollection) throws RepositoryException {
        return delegatee.exportProperties(exportContext, isCollection);
    }

    public boolean canImport(PropertyImportContext context, boolean isCollection) {
        return delegatee.canImport(context, isCollection);
    }

    public Map<? extends PropEntry, ?> importProperties(
            PropertyImportContext importContext, boolean isCollection)
            throws RepositoryException {
        return delegatee.importProperties(importContext, isCollection);
    }

    public boolean canCopy(CopyMoveContext context, DavResource source, DavResource destination) {
        return delegatee.canCopy(context, source, destination);
    }

    public boolean copy(CopyMoveContext context, DavResource source, DavResource destination) throws DavException {
        return delegatee.copy(context, source, destination);
    }

    public boolean canMove(CopyMoveContext context, DavResource source, DavResource destination) {
        return delegatee.canMove(context, source, destination);
    }

    public boolean move(CopyMoveContext context, DavResource source, DavResource destination) throws DavException {
      return delegatee.move(context, source, destination);
    }

    public boolean delete(DeleteContext deleteContext, DavResource davResource)
            throws DavException {
        return delegatee.delete(deleteContext, davResource);
    }

    public boolean canDelete(DeleteContext deleteContext,
            DavResource davResource) {
        return delegatee.canDelete(deleteContext, davResource);
    }
}
