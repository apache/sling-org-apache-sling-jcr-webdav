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

import org.apache.jackrabbit.server.io.DirListingExportHandler;
import org.apache.jackrabbit.server.io.IOHandler;
import org.apache.jackrabbit.server.io.PropertyHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.propertytypes.ServiceRanking;

/**
 * Wraps {@link org.apache.jackrabbit.server.io.DirListingExportHandler} in order to run it as a service.
 */
@Component(service = { DirListingExportHandlerService.class, IOHandler.class, PropertyHandler.class })
@ServiceRanking(100)
public class DirListingExportHandlerService extends DirListingExportHandler {

}
