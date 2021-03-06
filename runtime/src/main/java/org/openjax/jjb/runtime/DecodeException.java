/* Copyright (c) 2016 OpenJAX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.openjax.jjb.runtime;

import java.io.IOException;

public class DecodeException extends Exception {
  private static final long serialVersionUID = -1234230677110958751L;

  private final String json;

  public DecodeException(final JsonReader json) throws IOException {
    this(null, json, null);
  }

  public DecodeException(final String message, final JsonReader json) throws IOException {
    this(message, json, null);
  }

  public DecodeException(final JsonReader json, final Throwable cause) throws IOException {
    this(null, json, cause);
  }

  public DecodeException(final String message, final JsonReader json, final Throwable cause) throws IOException {
    super(message != null ? message + " [" + json.getPosition() + "] " + json.readFully() : "[" + json.getPosition() + "] " + json.readFully(), cause);
    this.json = json.readFully();
  }

  public String getJSON() {
    return json;
  }
}