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

import java.time.Instant;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;

/**
 * An abstract base class that implements the {@code Resource} interface and provides a foundational
 * implementation for managing resources within a collection.
 * <p>
 * This class encapsulates basic metadata about a resource, such as its identifier, creation time,
 * last modification time, and parent collection.
 *
 * @param <T> the type of content that this resource manages.
 */
public abstract class TestBaseResource<T> implements Resource<T> {
  private final String id;
  private final Collection parentCollection;
  private final Instant creation;

  private boolean closed;
  private Instant lastChange;

  /**
   * Constructs a protected instance of the {@code TestBaseResource} class with the provided
   * attributes, initializing the resource identifier, creation time, last modification time, and
   * associated parent collection.
   *
   * @param id the unique identifier for the resource.
   * @param creation the timestamp representing the creation time of the resource.
   * @param lastChange the timestamp of the last modification to the resource.
   * @param parentCollection the collection that this resource is a part of.
   */
  protected TestBaseResource(String id, Instant creation, Instant lastChange,
      Collection parentCollection) {
    this.id = id;
    this.creation = creation;
    this.lastChange = lastChange;
    this.parentCollection = parentCollection;
  }

  /**
   * Updates the last modification time of the resource to the current instant.
   * <p>
   * This method sets the {@code lastChange} property to the current system time, effectively
   * marking the resource as having been modified.
   */
  protected void updateLastChange() {
    lastChange = Instant.now();
  }

  @Override
  public Collection getParentCollection() {
    return parentCollection;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void close() {
    closed = true;
  }

  @Override
  public Instant getCreationTime() {
    return creation;
  }

  @Override
  public Instant getLastModificationTime() {
    return lastChange;
  }
}
