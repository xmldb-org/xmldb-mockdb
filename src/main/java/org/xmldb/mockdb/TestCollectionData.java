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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.xmldb.api.base.Collection;

/**
 * Represents a data record containing information about a test collection.
 * <p>
 * Instances of this record hold a name and a creation timestamp associated with a test collection,
 * providing both required attributes for subsequent use in other operations or records.
 * <p>
 * Constraints: - The name cannot be null. - The creation timestamp cannot be null.
 * <p>
 * This record offers two constructors: 1. A primary constructor accepting both name and creation
 * timestamp. 2. A secondary constructor accepting only the name and defaulting the creation
 * timestamp to the current system time.
 *
 * @param db the database to which the collection belongs, must not be null
 * @param name the name of the test collection, must not be null
 * @param creation the creation timestamp of the test collection, must not be null
 */
public record TestCollectionData(TestDatabase db, String name, Instant creation) {
  /**
   * Constructs a new {@code TestCollectionData} instance with the specified name and assigns the
   * current system timestamp as the creation time.
   *
   * @param db the database to which the collection belongs, must not be null
   * @param name the name of the test collection, must not be null
   */
  public TestCollectionData(TestDatabase db, String name) {
    this(db, name, Instant.now());
  }

  /**
   * Constructs a new {@code TestCollectionData} instance with the specified name and creation time.
   */
  public TestCollectionData {
    Objects.requireNonNull(db);
    Objects.requireNonNull(name);
    Objects.requireNonNull(creation);
    if (name.isBlank() || name.contains("/")) {
      throw new IllegalArgumentException("Collection is blank or contains a slash");
    }
  }

  private String calculateCollectionName(TestCollection parentCollection, String child) {
    final StringJoiner collectionName = new StringJoiner("/");
    if (parentCollection != null) {
      parentCollection.traverseHierarchy(collectionName::add);
    }
    collectionName.add(child);
    return collectionName.toString();
  }

  private Predicate<Map.Entry<String, TestCollection>> calculateCollectionPattern(
      String collectionName) {
    final Pattern compile = Pattern.compile("^%s/([^/]+)$".formatted(collectionName));
    return entry -> compile.matcher(entry.getKey()).matches();
  }

  void addCollection(TestCollection parentCollection, String child) {
    db.addCollection(calculateCollectionName(parentCollection, child));
  }

  Collection getCollection(TestCollection parentCollection, String collectionName) {
    return db.getCollection(calculateCollectionName(parentCollection, collectionName));
  }

  List<String> listCollection(TestCollection parentCollection) {
    return db.collections()
        .filter(calculateCollectionPattern(calculateCollectionName(parentCollection, name)))
        .map(Map.Entry::getValue).map(TestCollection::name).toList();
  }

  int getCollectionCount(TestCollection parentCollection) {
    return Math.toIntExact(db.collections()
        .filter(calculateCollectionPattern(calculateCollectionName(parentCollection, name)))
        .count());
  }
}
