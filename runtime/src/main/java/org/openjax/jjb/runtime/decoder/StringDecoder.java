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

import org.openjax.jjb.runtime.Binding;
import org.openjax.jjb.runtime.DecodeException;
import org.openjax.jjb.runtime.JSObjectBase;
import org.openjax.jjb.runtime.JsonReader;

public class StringDecoder extends Decoder<String> {
  public static String escapeString(final String string) {
    final StringBuilder out = new StringBuilder();
    for (int i = 0, length = string.length(); i < length; i++) {
      final char ch = string.charAt(i);
      /*
       * From RFC 4627, "All Unicode characters may be placed within the
       * quotation marks except for the characters that must be escaped:
       * quotation mark, reverse solidus, and the control characters (U+0000
       * through U+001F)."
       */
      switch (ch) {
        case '"':
        case '\\':
        case '/':
          out.append('\\').append(ch);
          break;
        case '\t':
          out.append("\\t");
          break;
        case '\b':
          out.append("\\b");
          break;
        case '\n':
          out.append("\\n");
          break;
        case '\r':
          out.append("\\r");
          break;
        case '\f':
          out.append("\\f");
          break;
        default:
          out.append(ch <= 0x1F ? String.format("\\u%04x", (int)ch) : ch);
          break;
      }
    }

    return out.toString();
  }

  /**
   * Unescapes the specified character, which is identified by the character or
   * characters that immediately follow a backslash. The backslash '\' should
   * have already been read. This supports both unicode escapes "u000A" and
   * two-character escapes "\n".
   *
   * @return The escaped character.
   * @throws IOException If an I/O error has occurred.
   * @throws DecodeException If the escape sequence is unterminated.
   * @throws NumberFormatException if any unicode escape sequences are
   *           malformed.
   */
  private static char readEscaped(final JsonReader reader, final char escaped) throws DecodeException, IOException {
    switch (escaped) {
      case 'u':
        final char[] unicode = new char[4];
        for (int i = 0; i < unicode.length; ++i) {
          final int ch = reader.read();
          if (ch == -1)
            throw new DecodeException("Unterminated escape sequence", reader);

          unicode[i] = (char)ch;
        }

        return (char)Integer.parseInt(new String(unicode), 16);
      case 't':
        return '\t';
      case 'b':
        return '\b';
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 'f':
        return '\f';
      case '\'':
      case '"':
      case '\\':
      default:
        return escaped;
    }
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
        builder.append(readEscaped(reader, ch));
        escape = false;
      }
      else if (!(escape = ch == '\\')) {
        builder.append(ch);
      }
    }

    return binding != null && binding.urlDecode ? URLDecoder.decode(builder.toString(), "UTF-8") : builder.toString();
  }
}