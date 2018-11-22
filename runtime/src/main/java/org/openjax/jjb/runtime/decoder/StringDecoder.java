/* Copyright (c) 2015 OpenJAX
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

package org.openjax.jjb.runtime.decoder;

import java.io.IOException;
import java.net.URLDecoder;

import org.fastjax.util.Characters;
import org.openjax.jjb.runtime.Binding;
import org.openjax.jjb.runtime.DecodeException;
import org.openjax.jjb.runtime.JSObjectBase;
import org.openjax.jjb.runtime.JsonReader;

public class StringDecoder extends Decoder<String> {
  public static String escapeString(final String string) {
    return string.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  @Override
  protected String[] newInstance(final int depth) {
    return new String[depth];
  }

  @Override
  public String decode(final JsonReader reader, char ch, final Binding<?> binding) throws DecodeException, IOException {
    if (ch != '"') {
      if (JSObjectBase.isNull(ch, reader))
        return null;

      throw new DecodeException("Illegal char for " + getClass().getSimpleName() + ": " + ch, reader);
    }

    boolean escape = false;
    final StringBuilder builder = new StringBuilder();
    while ((ch = JSObjectBase.nextAny(reader)) != '"' || escape) {
      if (escape && ch != '"') {
        builder.append(Characters.escape(ch));
        escape = false;
      }
      else if (!(escape = ch == '\\')) {
        builder.append(ch);
      }
    }

    return binding != null && binding.urlDecode ? URLDecoder.decode(builder.toString(), "UTF-8") : builder.toString();
  }
}