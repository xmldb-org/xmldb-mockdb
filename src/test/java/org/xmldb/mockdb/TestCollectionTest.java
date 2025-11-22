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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.time.Instant;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;

@MockitoSettings
class TestCollectionTest {
  @Spy
  TestDatabase db;
  @Mock
  Consumer<TestCollection> initializer;

  TestCollection collection;

  @BeforeEach
  void setUp() {
    collection = db.addCollection("db").getCollection("db");
  }

  @Test
  void getName() {
    assertThat(collection.getName()).isEqualTo("/db");
  }

  @Test
  void getChildCollectionCount() {
    assertThat(collection.getChildCollectionCount()).isZero();
    // add some collections
    collection.addCollection("sub1");
    collection.addCollection("sub2");
    assertThat(collection.getChildCollectionCount()).isEqualTo(2);
    // some sub-subcollections that should not be counted
    db.addCollection("db/sub1/sub1_1");
    db.addCollection("db/sub2/sub2_1");
    assertThat(collection.getChildCollectionCount()).isEqualTo(2);
  }

  @Test
  void listChildCollections() {
    assertThat(collection.listChildCollections()).isEmpty();
    // add some collections
    collection.addCollection("sub1");
    collection.addCollection("sub2");
    assertThat(collection.listChildCollections()).containsExactlyInAnyOrder("sub1", "sub2");
    // some sub-subcollections that should not be counted
    db.addCollection("db/sub1/sub1_1");
    db.addCollection("db/sub2/sub2_1");
    assertThat(collection.listChildCollections()).containsExactlyInAnyOrder("sub1", "sub2");
  }

  @Test
  void getChildCollection() {
    assertThat(collection.getChildCollection("sub")).isNull();
    // add some collections
    collection.addCollection("sub");
    collection.addCollection("sub/subsub");
    assertThat(collection.getChildCollection("sub")).isNotNull().satisfies(subCollection -> {
      assertThat(subCollection.getName()).isEqualTo("/db/sub");
      assertThat(subCollection.getChildCollection("subsub")).isNotNull().satisfies(subsub -> {
        assertThat(subsub.getName()).isEqualTo("/db/sub/subsub");
      });
    });
  }

  @Test
  void getParentCollection() {
    assertThat(collection.getParentCollection()).isNull();
  }

  @Test
  void close() {
    assertThat(collection.isOpen()).isTrue();
    assertThatNoException().isThrownBy(collection::close);
    assertThat(collection.isOpen()).isFalse();
  }

  @Test
  void getCreationTime() {
    assertThat(collection.getCreationTime()).isNotNull().isBeforeOrEqualTo(Instant.now());
  }

  @Test
  void nameAndToString() {
    assertThat(collection.name()).isEqualTo("db");
    assertThat(collection).hasToString("db");
  }
}
