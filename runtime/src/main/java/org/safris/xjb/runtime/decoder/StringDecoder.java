/* Copyright (c) 2015 Seva Safris
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

package org.safris.xjb.runtime.decoder;

import java.io.IOException;
import java.io.Reader;
import java.net.URLDecoder;

import org.safris.xjb.runtime.Binding;
import org.safris.xjb.runtime.JSObjectUtil;

public class StringDecoder extends Decoder<String> {
  public static String escapeString(final String string) {
    return string.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  @Override
  protected String[] newInstance(final int depth) {
    return new String[depth];
  }

  @Override
  public String decode(final Reader reader, char ch, final Binding<?> binding) throws IOException {
    if (ch != '"') {
      if (JSObjectUtil.isNull(ch, reader))
        return null;

      throw new IllegalArgumentException("Malformed JSON");
    }

    boolean escape = false;
    final StringBuilder builder = new StringBuilder();
    while ((ch = JSObjectUtil.nextAny(reader)) != '"' || escape)
      if (!(escape = ch == '\\' && !escape))
        builder.append(ch);

    return binding != null && binding.urlDecode ? URLDecoder.decode(builder.toString(), "UTF-8") : builder.toString();
  }
}