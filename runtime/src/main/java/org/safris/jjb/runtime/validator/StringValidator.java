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

package org.safris.jjb.runtime.validator;

import org.safris.jjb.runtime.decoder.StringDecoder;

public class StringValidator extends Validator<String> {
  private final String pattern;
  private final Integer length;

  public StringValidator(final String pattern, final Integer length) {
    this.pattern = pattern;
    this.length = length;
  }

  @Override
  public String validate(final String value) {
    final String patternError = pattern == null || value.matches(pattern) ? null : "does not match pattern \"" + StringDecoder.escapeString(pattern) + "\"";
    final String lengthError = length == null || value.length() <= length ? null : "is longer than length \"" + length + "\"";
    if (patternError == null) {
      if (lengthError == null)
        return null;

      return lengthError + ": \"" + StringDecoder.escapeString(value) + "\"";
    }

    if (lengthError == null)
      return patternError + ": \"" + StringDecoder.escapeString(value) + "\"";

    return patternError + ", and " + lengthError + ": \"" + StringDecoder.escapeString(value) + "\"";
  }
}