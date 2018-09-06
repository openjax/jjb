/* Copyright (c) 2018 lib4j
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

package org.libx4j.jjb.runtime;

import java.io.IOException;
import java.io.Reader;

import org.lib4j.io.ReplayReader;

public class JsonReader extends ReplayReader {
  private String full;

  public JsonReader(final Reader in) {
    super(in);
  }

  public JsonReader(final Reader in, final int size) {
    super(in, size);
  }

  public int getPosition() {
    return buffer.size();
  }

  public String readFully() throws IOException {
    if (full != null)
      return full;

    for (int i = 0; i != -1; i = read());

    return full = buffer.toString();
  }
}