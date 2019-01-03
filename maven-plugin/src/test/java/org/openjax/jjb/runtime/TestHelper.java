/* Copyright (c) 2017 OpenJAX
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

import java.lang.reflect.Field;

import org.openjax.standard.util.Classes;

public final class TestHelper {
  @SuppressWarnings("rawtypes")
  public static Property property(final JSObject jsObject, final String propertyName) {
    try {
      final Field field = Classes.getField(jsObject.getClass(), propertyName);
      if (field == null)
        throw new RuntimeException("Field not found: " + propertyName);

      return (Property)field.get(jsObject);
    }
    catch (final IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public static String replaceProperty(final String json, final String propertyName, final Object value) {
    final int start = json.indexOf("\"" + propertyName + "\": ");
    if (start < 0)
      throw new IllegalArgumentException("\"" + propertyName + "\" not found in:\n" + json);

    final char[] chars = json.toCharArray();
    Boolean isString = null;
    Integer arrayCount = null;
    Integer objCount = null;
    int end = -1;
    for (int i = start + propertyName.length() + 4; i < json.length(); i++) {
      final char ch = chars[i];
      if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t')
        continue;

      if (isString == null) {
        isString = ch == '"';
        if (!isString) {
          arrayCount = ch == '[' ? 1 : 0;
          objCount = ch == '{' ? 1 : 0;
        }

        continue;
      }

      if (isString) {
        if (ch == '"') {
          end = i + 1;
          break;
        }

        continue;
      }

      if (arrayCount > 0) {
        arrayCount += ch == ']' ? -1 : ch == '[' ? 1 : 0;
        continue;
      }

      if (objCount > 0) {
        objCount += ch == '}' ? -1 : ch == '{' ? 1 : 0;
        continue;
      }

      if (ch == ',') {
        end = value.toString().length() == 0 ? i + 1 : i;
        break;
      }

      if (ch == '}') {
        end = i;
        break;
      }
    }

    if (end == -1)
      throw new RuntimeException();

    return json.substring(0, start) + value + json.substring(end);
  }

  private TestHelper() {
  }
}