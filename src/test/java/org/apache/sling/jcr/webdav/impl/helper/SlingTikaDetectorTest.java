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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.sling.commons.mime.MimeTypeService;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SlingTikaDetectorTest {

    private static final ByteArrayInputStream EMPTY_INPUT_STREAM = new ByteArrayInputStream(new byte[0]);

    private MimeTypeService mimeTypeService = new MimeTypeService() {

        @Override
        public void registerMimeType(String arg0, String... arg1) {}

        @Override
        public void registerMimeType(InputStream arg0) throws IOException {}

        @Override
        public String getMimeType(String name) {
            if (name.toLowerCase(Locale.ENGLISH).endsWith(".html")) {
                return "text/html";
            } else {
                return null;
            }
        }

        @Override
        public String getExtension(String arg0) {
            return null;
        }
    };

    private SlingTikaDetector detector = new SlingTikaDetector(mimeTypeService);

    @Test
    public void noName() {
        Metadata metadata = new Metadata();
        assertEquals(MediaType.OCTET_STREAM, detector.detect(EMPTY_INPUT_STREAM, metadata));
    }

    @Test
    public void withPercentInName() {
        // see SLING-7528: checks that there is no percent-unescaping
        Metadata metadata = new Metadata();
        metadata.add(Metadata.RESOURCE_NAME_KEY, "a/b%c.html");
        assertEquals(MediaType.TEXT_HTML, detector.detect(EMPTY_INPUT_STREAM, metadata));
    }
}
