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

import static java.util.regex.Pattern.compile;
import static org.xmldb.api.DatabaseManager.URI_PREFIX;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;

/**
 * TestDatabase is an in-memory implementation of the Database interface. It provides support for
 * managing collections and simulates database operations without requiring a physical or external
 * database system.
 * <p>
 * This class extends ConfigurableImpl, inheriting mechanisms to manage configuration properties
 * using key-value pairs.
 * <p>
 * TestDatabase assigns a default name if no name is provided during construction. It supports
 * operations to add and retrieve collections and implements the necessary Database interface
 * methods.
 */
public class TestDatabase extends ConfigurableImpl implements Database {
  private static final String DEFAULT_NAME = "test";
  private static final String COLLECTION_DELIMITER = "/";
  private static final Pattern COLLECTION_PATTERN = compile(COLLECTION_DELIMITER);

  private final String name;
  private final Map<String, TestCollection> collections;
  private final BiPredicate<String, String> authenticationCallback;

  /**
   * Default constructor for the TestDatabase class. Constructs a new TestDatabase instance using
   * the default name.
   */
  public TestDatabase() {
    this(DEFAULT_NAME);
  }

  /**
   * Constructs a new TestDatabase instance with the specified name. If the provided name is null or
   * empty, a default name is assigned.
   *
   * @param name The name of the database. If null or empty, a default name is used.
   */
  public TestDatabase(String name) {
    this(name, null);
  }

  /**
   * Constructs a new TestDatabase instance with the specified name and authentication callback. If
   * the provided name is null or empty, a default name is assigned. If the authenticationCallback
   * is null, a default callback that always returns true will be used.
   *
   * @param name The name of the database. If null or empty, a default name will be assigned.
   * @param authenticationCallback A BiPredicate used for authenticating users. The first parameter
   *        represents the username, and the second represents the password. If null, a default
   *        callback that always returns true is used.
   */
  public TestDatabase(String name, BiPredicate<String, String> authenticationCallback) {
    this.authenticationCallback =
        Objects.requireNonNullElseGet(authenticationCallback, () -> (u, p) -> true);
    if (name == null || name.isEmpty()) {
      this.name = DEFAULT_NAME;
    } else {
      this.name = name;
    }
    collections = new HashMap<>();
  }

  private String sanitizePath(final String path) {
    if (path.startsWith(COLLECTION_DELIMITER)) {
      return path.substring(1).replaceFirst("/+$", "");
    }
    return path.replaceFirst("/+$", "");
  }

  /**
   * Adds a new collection to the database. If a collection with the specified name does not already
   * exist, it is created and initialized using the provided initializer.
   *
   * @param collectionName The name of the collection to be added. Must not be null or empty.
   * @return The current TestDatabase instance, allowing for method chaining.
   */
  public TestDatabase addCollection(String collectionName) {
    return addCollection(collectionName, null);
  }

  /**
   * Adds a new collection to the database. If a collection with the specified name does not already
   * exist, it is created and initialized using the provided initializer.
   *
   * @param collectionName The name of the collection to be added. Must not be null or empty.
   * @param initializer A Consumer instance that initializes the collection after it is created.
   * @return The current TestDatabase instance, allowing for method chaining.
   */
  public TestDatabase addCollection(String collectionName, Consumer<TestCollection> initializer) {
    final StringJoiner path = new StringJoiner(COLLECTION_DELIMITER);
    final AtomicReference<TestCollection> parentCollection = new AtomicReference<>();
    try (Stream<String> segmentStream = COLLECTION_PATTERN.splitAsStream(collectionName)) {
      segmentStream.filter(s -> !s.isBlank()).forEach(segment -> {
        path.add(segment);
        parentCollection.set(collections.computeIfAbsent(path.toString(),
            key -> createCollection(segment, parentCollection.get(), initializer)));
      });
    }
    return this;
  }

  TestCollection createCollection(String name, TestCollection parent,
      Consumer<TestCollection> initializer) {
    final TestCollection collection =
        new TestCollection(new TestCollectionData(this, name), parent);
    if (initializer != null) {
      initializer.accept(collection);
    }
    return collection;
  }

  TestCollection getCollection(String collectionName) {
    return collections.get(sanitizePath(collectionName));
  }

  Stream<Map.Entry<String, TestCollection>> collections() {
    return collections.entrySet().stream();
  }

  @Override
  public final String getName() {
    return name;
  }

  @Override
  public Collection getCollection(String uri, Properties info) throws XMLDBException {
    if (acceptsURI(uri)) {
      final String user;
      final String password;
      if (info == null) {
        user = null;
        password = null;
      } else {
        user = info.getProperty("user");
        password = info.getProperty("password");
      }
      if (uri.startsWith(URI_PREFIX) && authenticationCallback.test(user, password)) {
        final URI dbUri = URI.create(uri.substring(URI_PREFIX.length()));
        if (name.equals(dbUri.getScheme())) {
          return collections.get(sanitizePath(dbUri.getPath()));
        }
        return null;
      }
      throw new XMLDBException(ErrorCodes.PERMISSION_DENIED);
    }
    return null;
  }

  @Override
  public boolean acceptsURI(String uri) {
    if (uri.startsWith(URI_PREFIX)) {
      final URI dbUri = URI.create(uri.substring(URI_PREFIX.length()));
      return name.equals(dbUri.getScheme());
    }
    return false;
  }

  @Override
  public String getConformanceLevel() {
    return "0";
  }
}
