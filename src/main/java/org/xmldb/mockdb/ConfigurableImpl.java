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

import java.util.HashMap;
import java.util.Map;

import org.xmldb.api.base.Configurable;

/**
 * The ConfigurableImpl class provides a concrete implementation of the Configurable interface. It
 * allows for managing configuration properties using key-value pairs.
 */
public class ConfigurableImpl implements Configurable {
  private final Map<String, String> properties;

  /**
   * Constructs a new instance of ConfigurableImpl.
   * <p>
   * Initializes the internal storage for configuration properties using a HashMap. This object
   * allows managing key-value pairs for configuration purposes.
   */
  public ConfigurableImpl() {
    properties = new HashMap<>();
  }

  @Override
  public final String getProperty(String name) {
    return properties.get(name);
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    return properties.getOrDefault(name, defaultValue);
  }

  @Override
  public final void setProperty(String name, String value) {
    properties.put(name, value);
  }
}
