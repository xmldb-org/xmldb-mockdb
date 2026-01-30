/*
 * Licensed under the Apache License, Version 2.0 (the "License") you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in
 * writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package org.xmldb.mockdb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.XMLDBException;

final class ByteData {
  private final Class<? extends Resource> type;
  private final Instant creation;
  private final Instant lastChange;
  private final byte[] content;

  ByteData(Resource resource) throws XMLDBException {
    type = resource.getClass();
    creation = resource.getCreationTime();
    lastChange = resource.getLastModificationTime();
    ByteArrayOutputStream dataConsumer = new ByteArrayOutputStream();
    resource.getContentAsStream(dataConsumer);
    content = dataConsumer.toByteArray();
  }

  private Resource setContent(TestBaseResource resource) throws XMLDBException {
    resource.setContentAsStream(new ByteArrayInputStream(content));
    resource.setLastChange(lastChange);
    return resource;
  }

  Resource createResource(String id, TestCollection testCollection) throws XMLDBException {
    return setContent(testCollection.createResource(id, type, creation));
  }

  @Override
  public String toString() {
    return "ByteData(creation=%s, lastChange=%s, content.length=%d)".formatted(creation, lastChange,
        content.length);
  }
}
