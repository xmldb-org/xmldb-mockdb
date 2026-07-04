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

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Resource;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.ServiceProviderCache;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.BinaryResource;
import org.xmldb.api.modules.CollectionManagementService;
import org.xmldb.api.modules.DatabaseInstanceService;
import org.xmldb.api.modules.TransactionService;
import org.xmldb.api.modules.XMLResource;
import org.xmldb.api.modules.XPathQueryService;
import org.xmldb.api.modules.XQueryService;
import org.xmldb.api.modules.XUpdateQueryService;
import org.xmldb.api.security.PermissionManagementService;
import org.xmldb.api.security.UserPrincipalLookupService;

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
  private final ServiceProviderCache serviceProviderCache =
      ServiceProviderCache.withRegistered(this::registerProviders);
  private final AtomicBoolean open;

  /**
   * Constructs a new TestCollection instance with the specified data and parent collection.
   *
   * @param data The data associated with this collection. Must not be null.
   */
  TestCollection(final TestCollectionData data) {
    this.data = Objects.requireNonNull(data, "data must not be null");
    open = new AtomicBoolean(true);
  }

  final void registerProviders(ServiceProviderCache.ProviderRegistry reg) {
    // modules
    reg.add(CollectionManagementService.class, () -> new TestCollectionManagementService(this));
    reg.add(DatabaseInstanceService.class, () -> new TestDatabaseInstanceService(this));
    reg.add(TransactionService.class, () -> new TestTransactionService(this));
    reg.add(XPathQueryService.class, () -> new TestXPathQueryService(this));
    reg.add(XQueryService.class, () -> new TestXQueryService(this));
    reg.add(XUpdateQueryService.class, () -> new TestXUpdateQueryService(this));
    // security
    reg.add(PermissionManagementService.class, () -> new TestPermissionManagementService(this));
    reg.add(UserPrincipalLookupService.class, () -> new TestUserPrincipalLookupService(this));
  }

  /**
   * Adds a new resource to the collection using the specified creation action.
   *
   * @param <R> The type of resource, which must extend TestBaseResource.
   * @param id The unique identifier for the resource to be added.
   * @param createAction A function that creates a resource using its identifier and the current
   *        collection.
   * @return The newly created and added resource.
   */
  public <R extends TestBaseResource> R addResource(String id,
      BiFunction<String, TestCollection, R> createAction) {
    if (id == null || id.isBlank()) {
      id = createId();
    }
    R resource = createAction.apply(id, this);
    try {
      data.resources().put(resource.getId(), new ByteData(resource));
    } catch (XMLDBException e) {
      throw new IllegalStateException("Unexpected failure while extracting data", e);
    }
    return resource;
  }

  TestBaseResource createResource(String id, Class<?> type, Instant creation)
      throws XMLDBException {
    if (id == null || id.isBlank()) {
      id = createId();
    }
    if (creation == null) {
      creation = Instant.now();
    }
    if (BinaryResource.class.isAssignableFrom(type)) {
      return new TestBinaryResource(id, creation, this);
    } else if (XMLResource.class.isAssignableFrom(type)) {
      return new TestXMLResource(id, creation, this);
    }
    throw new XMLDBException(INVALID_RESOURCE, "Invalid resource type: " + type);
  }

  /**
   * Adds a child collection to the current collection.
   *
   * @param child The name of the child collection to be added.
   */
  public TestCollection addCollection(String child) {
    AtomicReference<TestCollection> collectionDataAtomicReference = new AtomicReference<>();
    data.addCollection(data, child, collectionDataAtomicReference::set);
    return collectionDataAtomicReference.get();
  }

  void removeCollection(String name) {
    data.removeCollection(data, name);
  }

  /**
   * Retrieves the parent collection of this collection.
   *
   * @return The parent collection, or {@code null} if this collection has no parent.
   */
  TestCollection parentCollection() {
    final TestCollectionData parentData = data.parent();
    if (parentData == null) {
      return null;
    }
    return new TestCollection(parentData);
  }

  /**
   * Retrieves the name of the collection without any parent collection names.
   *
   * @return The name of the collection as a string.
   */
  String name() {
    return data.name();
  }

  @Override
  public final String getName() {
    final StringJoiner joiner = new StringJoiner("/", "/", "");
    data.traverseHierarchy(joiner::add);
    return joiner.toString();
  }

  @Override
  public <S extends Service> boolean hasService(Class<S> serviceType) {
    return serviceProviderCache.hasService(serviceType);
  }

  @Override
  public <S extends Service> Optional<S> findService(Class<S> serviceType) {
    return serviceProviderCache.findService(serviceType);
  }

  @Override
  public int getChildCollectionCount() {
    return data.getCollectionCount();
  }

  @Override
  public List<String> listChildCollections() {
    return data.listCollection();
  }

  @Override
  public Collection getChildCollection(String collectionName) {
    final TestCollectionData childCollectionData = data.getCollection(collectionName);
    if (childCollectionData == null) {
      return null;
    }
    return new TestCollection(childCollectionData);
  }

  @Override
  public Collection getParentCollection() {
    return parentCollection();
  }

  @Override
  public int getResourceCount() {
    return data.resources().size();
  }

  @Override
  public List<String> listResources() {
    return data.resources().keySet().stream().toList();
  }

  @Override
  public <R extends Resource> R createResource(String id, Class<R> type) throws XMLDBException {
    return type.cast(createResource(id, type, null));
  }

  @Override
  public void removeResource(Resource res) throws XMLDBException {
    final ByteData byteData = data.resources().remove(res.getId());
    if (byteData == null) {
      throw new XMLDBException(INVALID_RESOURCE, "Resource not found: " + res.getId());
    }
  }

  @Override
  public void storeResource(Resource res) throws XMLDBException {
    data.resources().put(res.getId(), new ByteData(res));
  }

  @Override
  public Resource getResource(String id) throws XMLDBException {
    final ByteData byteData = data.resources().get(id);
    if (byteData == null) {
      return null;
    }
    return byteData.createResource(id, this);
  }

  @Override
  public String createId() {
    return UUID.randomUUID().toString();
  }

  @Override
  public boolean isOpen() {
    return open.get();
  }

  @Override
  public void close() {
    open.set(false);
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
