/* Copyright (c) 2016 Seva Safris
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

package org.safris.xjb.runtime.validator;

public class NumberValidator extends Validator<String> {
  private final boolean whole;
  private final Integer min;
  private final Integer max;

  public NumberValidator(final boolean whole, final Integer min, final Integer max) {
    this.whole = whole;
    this.min = min;
    this.max = max;
  }

  @Override
  public String validate(final String value) {
    if (value == null)
      return null;

    final String formError = !whole || !value.contains(".") ? null : "is not a \"whole\" number";

    final Double doubleValue = Double.parseDouble(value);
    final String minError = min == null || min <= doubleValue ? null : "is less than \"" + min + "\" min value";
    final String maxError = max == null || max >= doubleValue ? null : "is more than \"" + max + "\" max value";

    if (formError == null && minError == null && maxError == null)
      return null;

    final StringBuilder builder = new StringBuilder();
    if (formError != null)
      builder.append(", and ").append(formError);

    if (minError != null)
      builder.append(", and ").append(minError);

    if (maxError != null)
      builder.append(", and ").append(maxError);

    return builder.substring(6) + ": \"" + value + "\"";
  }
}