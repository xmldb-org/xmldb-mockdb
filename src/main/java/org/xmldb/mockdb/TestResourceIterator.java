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

import org.xmldb.api.base.Resource;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.XMLDBException;

/**
 * The TestResourceIterator class implements the ResourceIterator interface and provides a simple,
 * non-functional implementation of its methods. This class is designed as a placeholder or test
 * implementation and does not actually iterate over any resources.
 * <p>
 * Methods in this implementation are overridden to return default or null values, indicating the
 * absence of functionality.
 */
public class TestResourceIterator implements ResourceIterator {

  /**
   * Constructs a new instance of the TestResourceIterator class. This constructor initializes a
   * basic implementation of a resource iterator with no specific functionality or state.
   */
  public TestResourceIterator() {
    super();
  }

  @Override
  public boolean hasMoreResources() {
    return false;
  }

  @Override
  public Resource nextResource() {
    return null;
  }

}
