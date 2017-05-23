/* Copyright (c) 2015 lib4j
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

package org.libx4j.jjb.runtime.decoder;

import java.io.IOException;

import org.lib4j.util.RewindableReader;
import org.libx4j.jjb.runtime.Binding;
import org.libx4j.jjb.runtime.DecodeException;
import org.libx4j.jjb.runtime.JSObjectBase;

public abstract class Decoder<T> extends JSObjectBase {
  protected abstract T[] newInstance(final int depth);

  public abstract T decode(final RewindableReader reader, char ch, final Binding<?> binding) throws DecodeException, IOException;

  public final T[] recurse(final RewindableReader reader, final int depth, final Binding<?> binding) throws DecodeException, IOException {
    char ch = JSObjectBase.next(reader);
    if (ch == ']')
      return newInstance(depth);

    if (ch == ',')
      return recurse(reader, depth, binding);

    final T value = decode(reader, ch, binding);
    final T[] array = recurse(reader, depth + 1, binding);
    array[depth] = value;
    return array;
  }
}