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

package org.libx4j.jjb.runtime.validator;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.lib4j.lang.Numbers;

public class NumberValidator extends Validator<Number> {
  private final boolean integer;
  private final BigDecimal min;
  private final boolean minInclusive;
  private final BigDecimal max;
  private final boolean maxInclusive;

  public NumberValidator(final boolean integer, final BigDecimal min, final boolean minInclusive, final BigDecimal max, final boolean maxInclusive) {
    super(Number.class);
    this.integer = integer;
    this.min = min;
    this.minInclusive = minInclusive;
    this.max = max;
    this.maxInclusive = maxInclusive;
  }

  @Override
  public String validate(final Number value) {
    if (value == null)
      return null;

    final String formError = !integer || value instanceof BigInteger ? null : "is not an \"integer\" number";

    final String minError = min == null || Integer.compare(Numbers.compare(min, value), 0) < (minInclusive ? 1 : 0) ? null : "min " + (minInclusive ? "inclusive" : "exclusive") + " bound \"" + min.stripTrailingZeros().toPlainString() + "\" violated";
    final String maxError = max == null || Integer.compare(Numbers.compare(value, max), 0) < (maxInclusive ? 1 : 0) ? null : "max " + (maxInclusive ? "inclusive" : "exclusive") + " bound \"" + max.stripTrailingZeros().toPlainString() + "\" violated";

    if (formError == null && minError == null && maxError == null)
      return null;

    final StringBuilder builder = new StringBuilder();
    if (formError != null)
      builder.append(", and ").append(formError);

    if (minError != null)
      builder.append(", and ").append(minError);

    if (maxError != null)
      builder.append(", and ").append(maxError);

    return builder.append("(\"").append(value).append("\")").substring(6);
  }
}