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

/**
 * The AuthenticationCallback interface defines a mechanism for handling user authentication.
 * Implementations of this interface are expected to authenticate a user by verifying the provided
 * username and password.
 */
@FunctionalInterface
public interface AuthenticationCallback {
  /**
   * Authenticates a user by verifying the provided username and password.
   *
   * @param username The username of the user attempting to authenticate. Must not be null or empty.
   * @param password The password associated with the given username. Must not be null or empty.
   */
  void authenticate(String username, String password);
}
