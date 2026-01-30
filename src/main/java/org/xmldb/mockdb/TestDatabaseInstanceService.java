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

import org.xmldb.api.base.ErrorCodes;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.DatabaseInstanceService;

class TestDatabaseInstanceService extends BaseService implements DatabaseInstanceService {
  TestDatabaseInstanceService(TestCollection collection) {
    super(new ServiceInfo("DatabaseInstanceService", "1.0"), collection);
  }

  @Override
  public void shutdown() throws XMLDBException {
    throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED, "Namespace management is not supported.");
  }

  @Override
  public void shutdown(long delay) throws XMLDBException {
    throw new XMLDBException(ErrorCodes.NOT_IMPLEMENTED, "Namespace management is not supported.");
  }

  @Override
  public boolean isLocalInstance() {
    return false;
  }
}
