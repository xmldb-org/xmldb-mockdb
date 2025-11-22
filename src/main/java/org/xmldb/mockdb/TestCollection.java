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

import static org.xmldb.api.base.ErrorCodes.INVALID_RESOURCE;
import static org.xmldb.api.base.ErrorCodes.NOT_IMPLEMENTED;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.XMLResource;

/**
 * The TestCollection class represents a collection of resources and child collections
 * <p>
 * It is an implementation of the Collection interface, providing functionality for managing
 * resources and hierarchical collections.
 * <p>
 * TestCollection instances can also be configured using inherited capabilities from
 * ConfigurableImpl.
 */
public class TestCollection extends ConfigurableImpl implements Collection {
  private final TestCollectionData data;
  private final TestCollection parentCollection;
  private final ConcurrentMap<String, Resource<?>> resources;

  private boolean closed;

  /**
   * Constructs a new TestCollection instance with the specified data and parent collection.
   *
   * @param data The data associated with this collection. Must not be null.
   * @param parent The parent collection, which may be null if this collection does not have a
   *        parent.
   */
  TestCollection(final TestCollectionData data, final TestCollection parent) {
    this.data = data;
    this.parentCollection = parent;
    resources = new ConcurrentHashMap<>();
  }

  /**
   * Creates a new instance of TestCollection with the specified name.
   *
   * @param db the database to which the collection belongs must not be null
   * @param name The name of the collection to be created. Must not be null or empty.
   * @param parent The parent collection, which may be null if this collection does not have a
   *        parent.
   * @param initializer A Consumer instance that initializes the collection after it is created.
   * @return A new TestCollection instance with the given name.
   */
  public static TestCollection create(TestDatabase db, String name, TestCollection parent,
      Consumer<TestCollection> initializer) {
    final TestCollection collection = new TestCollection(new TestCollectionData(db, name), parent);
    if (initializer != null) {
      initializer.accept(collection);
    }
    return collection;
  }

  /**
   * Adds a new resource to the collection using the specified creation action.
   *
   * @param <T> The type of data the resource holds.
   * @param <R> The type of resource, which must extend TestBaseResource.
   * @param id The unique identifier for the resource to be added.
   * @param createAction A function that creates a resource using its identifier and the current
   *        collection.
   * @return The newly created and added resource.
   */
  public <T, R extends TestBaseResource<T>> R addResource(String id,
      BiFunction<String, Collection, R> createAction) {
    R resource = createAction.apply(id, this);
    resources.put(resource.getId(), resource);
    return resource;
  }

  /**
   * Adds a child collection to the current collection.
   *
   * @param child The name of the child collection to be added.
   */
  public void addCollection(String child) {
    data.addCollection(this, child);
  }

  /**
   * Retrieves the parent collection of this collection.
   *
   * @return The parent collection, or {@code null} if this collection has no parent.
   */
  TestCollection parentCollection() {
    return parentCollection;
  }

  /**
   * Retrieves the name of the collection without any parent collection names.
   *
   * @return The name of the collection as a string.
   */
  String name() {
    return data.name();
  }

  /**
   * Traverses the hierarchy of collections, starting from the current collection up to the root
   * collection, and applies the specified action to the name of each collection in the hierarchy.
   *
   * @param action A Consumer that processes the name of each collection in the hierarchy. Must not
   *        be null.
   */
  void traverseHierarchy(Consumer<String> action) {
    if (parentCollection != null) {
      parentCollection.traverseHierarchy(action);
    }
    action.accept(name());
  }

  @Override
  public final String getName() {
    final StringJoiner joiner = new StringJoiner("/", "/", "");
    traverseHierarchy(joiner::add);
    return joiner.toString();
  }

  @Override
  public <S extends Service> boolean hasService(Class<S> serviceType) {
    return false;
  }

  @Override
  public <S extends Service> Optional<S> findService(Class<S> serviceType) {
    return Optional.empty();
  }

  @Override
  public <S extends Service> S getService(Class<S> serviceType) throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public int getChildCollectionCount() {
    return data.getCollectionCount(parentCollection);
  }

  @Override
  public List<String> listChildCollections() {
    return data.listCollection(parentCollection);
  }

  @Override
  public Collection getChildCollection(String collectionName) {
    return data.getCollection(this, collectionName);
  }

  @Override
  public Collection getParentCollection() {
    return parentCollection;
  }

  @Override
  public int getResourceCount() {
    return resources.size();
  }

  @Override
  public List<String> listResources() {
    return resources.keySet().stream().toList();
  }

  @Override
  public <T, R extends Resource<T>> R createResource(String id, Class<R> type)
      throws XMLDBException {
    if (BinaryResource.class.equals(type)) {
      return type.cast(new TestBinaryResource(id, this));
    } else if (XMLResource.class.equals(type)) {
      return type.cast(new TestXMLResource(id, this));
    }
    throw new XMLDBException(INVALID_RESOURCE);
  }

  @Override
  public void removeResource(Resource<?> res) throws XMLDBException {
    resources.remove(res.getId());
  }

  @Override
  public void storeResource(Resource<?> res) throws XMLDBException {
    resources.put(res.getId(), res);
  }

  @Override
  public Resource<?> getResource(String id) {
    return resources.get(id);
  }

  @Override
  public String createId() throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public boolean isOpen() {
    return !closed;
  }

  @Override
  public void close() {
    closed = true;
  }

  @Override
  public Instant getCreationTime() {
    return data.creation();
  }

  @Override
  public String toString() {
    return name();
  }
}
