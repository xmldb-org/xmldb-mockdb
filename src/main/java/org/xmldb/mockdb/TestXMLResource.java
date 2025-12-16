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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.xmldb.api.base.ErrorCodes.*;
import static org.xmldb.api.base.ErrorCodes.NOT_IMPLEMENTED;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XMLResource;

/**
 * The {@code TestXMLResource} class provides an implementation of an XML resource within a
 * collection. It extends the {@code TestBaseResource} for managing resource metadata and implements
 * the {@code XMLResource} interface for XML-specific operations.
 * <p>
 * Instances of this class represent individual XML resources identified by a unique ID and
 * associated with a parent collection. The class includes functionality for managing XML content
 * and interaction but does not currently implement its content-handling methods.
 */
public class TestXMLResource extends TestBaseResource implements XMLResource {
  private String content;

  /**
   * Constructs a new instance of TestXMLResource with the specified identifier and parent
   * collection.
   *
   * @param id the unique identifier for the resource
   * @param parentCollection the parent collection to which this resource belongs
   */
  public TestXMLResource(String id, Collection parentCollection) {
    this(id, Instant.now(), parentCollection);
  }

  /**
   * Constructs a new instance of TestXMLResource.
   *
   * @param id the unique identifier for the resource
   * @param creation the timestamp marking the creation of the resource
   * @param parentCollection the parent collection to which this resource belongs
   */
  public TestXMLResource(String id, Instant creation, Collection parentCollection) {
    super(id, creation, creation, parentCollection);
  }

  @Override
  public void getContentAsStream(OutputStream stream) throws XMLDBException {
    try {
      stream.write(content.getBytes(UTF_8));
    } catch (IOException e) {
      throw new XMLDBException(VENDOR_ERROR, e);
    }
  }

  @Override
  public void setContentAsStream(InputStream inputStream) throws XMLDBException {
    try (inputStream; ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      inputStream.transferTo(outputStream);
      this.content = outputStream.toString(UTF_8);
    } catch (IOException e) {
      throw new XMLDBException(VENDOR_ERROR, e);
    }
  }

  @Override
  public Object getContent() {
    return content;
  }

  @Override
  public void setContent(Object value) throws XMLDBException {
    if (value instanceof String stringValue) {
      content = stringValue;
      updateLastChange();
    } else {
      throw new XMLDBException(VENDOR_ERROR, "Content must be of type String");
    }
  }

  @Override
  public String getDocumentId() {
    return getId();
  }

  @Override
  public Node getContentAsDOM() throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public void setContentAsDOM(Node content) throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public void getContentAsSAX(ContentHandler handler) throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public ContentHandler setContentAsSAX() throws XMLDBException {
    throw new XMLDBException(NOT_IMPLEMENTED);
  }

  @Override
  public void setSAXFeature(String feature, boolean value)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    throw new SAXNotSupportedException();
  }

  @Override
  public boolean getSAXFeature(String feature)
      throws SAXNotRecognizedException, SAXNotSupportedException {
    return false;
  }

  @Override
  public void setXMLReader(XMLReader xmlReader) {
    // no action
  }
}
