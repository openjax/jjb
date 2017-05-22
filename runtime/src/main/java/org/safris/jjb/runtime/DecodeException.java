/* Copyright (c) 2016 lib4j
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

package org.safris.jjb.runtime;

import java.io.IOException;

import org.safris.commons.util.RewindableReader;

public class DecodeException extends Exception {
  private static final long serialVersionUID = -1234230677110958751L;

  private final String json;

  public DecodeException(final RewindableReader json) throws IOException {
    this(null, json, null);
  }

  public DecodeException(final String message, final RewindableReader json) throws IOException {
    this(message, json, null);
  }

  public DecodeException(final RewindableReader json, final Throwable cause) throws IOException {
    this(null, json, cause);
  }

  public DecodeException(final String message, final RewindableReader json, final Throwable cause) throws IOException {
    super(message != null ? message + " [" + json.getLength() + "] " + json.readFully() : "[" + json.getLength() + "] " + json.readFully(), cause);
    this.json = json.readFully();
  }

  public String getJSON() {
    return json;
  }
}