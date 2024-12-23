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

import java.io.InputStream;

import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

public class SlingTikaDetector implements Detector {

    private final MimeTypeService mimeTypeService;

    public SlingTikaDetector(MimeTypeService mimeTypeService) {
        this.mimeTypeService = mimeTypeService;
    }

    public MediaType detect(InputStream rawData, Metadata metadata) {

        // NOTE: This implementation is built after the Tika NameDetector
        //    implementation which only takes the resource name into
        //    consideration when trying to detect the MIME type.

        // Look for a resource name in the input metadata
        String name = metadata.get(Metadata.RESOURCE_NAME_KEY);
        if (name != null) {
            // If the name is a path, skip all but the last component
            int slash = name.lastIndexOf('/');
            if (slash != -1) {
                name = name.substring(slash + 1);
            }
            if (name.length() > 0) {
                // Match the name against the registered patterns
                String type = mimeTypeService.getMimeType(name);
                if (type != null) {
                    return MediaType.parse(type);
                }
            }
        }

        return MediaType.OCTET_STREAM;
    }
}
