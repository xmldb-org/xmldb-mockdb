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
package org.xmldb.mockdb.services;

import org.xmldb.api.base.Collection;
import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.Service;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.mockdb.ConfigurableImpl;
import org.xmldb.mockdb.TestCollection;

public abstract class BaseService extends ConfigurableImpl implements Service {
  private final ServiceInfo info;

  private TestCollection collection;

  BaseService(ServiceInfo info, TestCollection collection) {
    this.info = info;
    this.collection = collection;
  }

  TestCollection collection() {
    return collection;
  }

  @Override
  public final String getName() throws XMLDBException {
    return info.name();
  }

  @Override
  public final String getVersion() throws XMLDBException {
    return info.version();
  }

  @Override
  public final void setCollection(Collection col) throws XMLDBException {
    if (col instanceof TestCollection remoteCollection) {
      this.collection = remoteCollection;
    } else {
      throw new XMLDBException(ErrorCodes.INVALID_COLLECTION);
    }
  }
}
