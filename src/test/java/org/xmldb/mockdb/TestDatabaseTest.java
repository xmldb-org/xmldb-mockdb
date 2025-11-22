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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Properties;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.xmldb.api.base.XMLDBException;

@MockitoSettings
class TestDatabaseTest {
  TestDatabase db;

  @BeforeEach
  void setUp() {
    db = new TestDatabase();
    assertThat(db.addCollection("db")).isSameAs(db);
    assertThat(db.addCollection("db/sub1")).isSameAs(db);
    assertThat(db.addCollection("db/sub1/sub1_1")).isSameAs(db);
    assertThat(db.addCollection("db/sub1/sub1_2")).isSameAs(db);
    assertThat(db.addCollection("db/sub2")).isSameAs(db);
    assertThat(db.addCollection("db/sub2/sub2_1")).isSameAs(db);
    assertThat(db.addCollection("db/sub2/sub2_2")).isSameAs(db);
  }

  @Test
  void getName() {
    assertThat(db.getName()).isEqualTo("test");
  }

  @Test
  void collections() {
    assertThat(db.collections()).hasSize(7);
    assertThat(db.collections().map(Map.Entry::getKey)).containsExactlyInAnyOrder("db", "db/sub1",
        "db/sub1/sub1_1", "db/sub1/sub1_2", "db/sub2", "db/sub2/sub2_1", "db/sub2/sub2_2");
  }

  @Test
  void getCollection() {
    db.collections().forEach(entry -> {
      assertThat(db.getCollection("/" + entry.getKey() + "/")).isSameAs(entry.getValue())
          .satisfies(collection -> {
            assertThat(collection.getName()).isEqualTo("/" + entry.getKey());
          });
    });
  }


  @ParameterizedTest
  @MethodSource("dbUris")
  void getCollection(String uri) throws XMLDBException {
    Properties info = new Properties();
    assertThat(db.getCollection(uri, info)).isNotNull()
        .satisfies(collection -> assertThat(collection.getName()).isEqualTo("/db"));
  }

  @ParameterizedTest
  @CsvSource(textBlock = """
       ,
      john, doe
      """)
  void getCollectionAuthenticator(String username, String password,
      @Mock BiPredicate<String, String> authenticationCallback) throws XMLDBException {
    final String uri = "xmldb:mytest://hostname.domain:1234/mydb";
    final Properties info = new Properties();
    if (username != null) {
      info.setProperty("user", username);
    }
    if (password != null) {
      info.setProperty("password", password);
    }
    TestDatabase mytest = new TestDatabase("mytest", authenticationCallback);
    mytest.addCollection("/mydb/", name -> assertThat(name.getName()).isEqualTo("/mydb"));

    when(authenticationCallback.test(username, password)).thenReturn(false, true);

    assertThatExceptionOfType(XMLDBException.class)
        .isThrownBy(() -> mytest.getCollection(uri, info));
    assertThat(mytest.getCollection(uri, info)).isNotNull();
  }

  @ParameterizedTest
  @MethodSource("dbUris")
  void getCollectionNullInfo(String uri) throws XMLDBException {
    assertThat(db.getCollection(uri, null)).isNotNull()
        .satisfies(collection -> assertThat(collection.getName()).isEqualTo("/db"));
  }

  @ParameterizedTest
  @MethodSource("dbUris")
  void acceptsURI(String uri) {
    assertThat(db.acceptsURI(uri)).isTrue();
  }

  @Test
  void getConformanceLevel() {
    assertThat(db.getConformanceLevel()).isEqualTo("0");
  }

  static Stream<String> dbUris() {
    return Stream.of( //
        "xmldb:test:/db/", //
        "xmldb:test://127.0.0.1/db", //
        "xmldb:test://127.0.0.1:1234/db", //
        "xmldb:test://[::1]/db", //
        "xmldb:test://[::1]:1234/db", //
        "xmldb:test://hostname/db", //
        "xmldb:test://hostname:1234/db", //
        "xmldb:test://hostname.domain/db", //
        "xmldb:test://hostname.domain:1234/db" //
    );
  }
}
