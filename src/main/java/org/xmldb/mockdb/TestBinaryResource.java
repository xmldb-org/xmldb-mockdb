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

import static org.xmldb.api.base.ErrorCodes.*;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Arrays;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;

/**
 * A concrete implementation of {@code TestBaseResource<byte[]>} that represents a binary resource.
 * This class provides foundational behavior for managing binary data within a parent collection,
 * while implementing the {@code BinaryResource} interface.
 * <p>
 * This resource type supports binary data and provides methods specific to handling such data.
 * However, for this implementation, the functionality of these methods is not provided and throws
 * exceptions to indicate unimplemented functionality.
 */
public class TestBinaryResource extends TestBaseResource<byte[]> implements BinaryResource {
  private byte[] content;

  /**
   * Constructs a new {@code TestBinaryResource} with the specified identifier and parent
   * collection.
   * <p>
   * This constructor initializes the resource and sets its creation timestamp to the current time.
   *
   * @param id the unique identifier for this resource
   * @param parentCollection the parent collection to which this resource belongs
   */
  public TestBinaryResource(String id, Collection parentCollection) {
    this(id, Instant.now(), parentCollection);
  }

  /**
   * Constructs a new {@code TestBinaryResource} with the specified identifier, creation timestamp,
   * and parent collection.
   * <p>
   * This constructor initializes the resource with the provided parameters, where the creation
   * timestamp is explicitly supplied.
   *
   * @param id the unique identifier for this resource
   * @param creation the timestamp representing the creation time of this resource
   * @param parentCollection the parent collection to which this resource belongs
   */
  public TestBinaryResource(String id, Instant creation, Collection parentCollection) {
    super(id, creation, creation, parentCollection);
  }

  @Override
  public void getContentAsStream(OutputStream stream) throws XMLDBException {
    try {
      stream.write(content);
    } catch (IOException e) {
      throw new XMLDBException(VENDOR_ERROR, e);
    }
  }

  @Override
  public byte[] getContent() {
    return Arrays.copyOf(content, content.length);
  }

  @Override
  public void setContent(byte[] value) {
    content = Arrays.copyOf(value, value.length);
    updateLastChange();
  }
}
