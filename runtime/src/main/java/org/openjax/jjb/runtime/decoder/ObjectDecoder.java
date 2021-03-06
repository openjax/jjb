/* Copyright (c) 2016 OpenJAX
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

import org.openjax.jjb.runtime.Binding;
import org.openjax.jjb.runtime.DecodeException;
import org.openjax.jjb.runtime.JSObjectBase;
import org.openjax.jjb.runtime.JsonReader;

public class ObjectDecoder extends Decoder<Object> {
  private final JSObjectDecoder objectDecoder;
  private final StringDecoder stringDecoder;
  private final NumberDecoder numberDecoder;
  private final BooleanDecoder booleanDecoder;

  public ObjectDecoder(final JSObjectDecoder objectDecoder, final StringDecoder stringDecoder, final NumberDecoder numberDecoder, final BooleanDecoder booleanDecoder) {
    this.objectDecoder = objectDecoder;
    this.stringDecoder = stringDecoder;
    this.numberDecoder = numberDecoder;
    this.booleanDecoder = booleanDecoder;
  }

  @Override
  protected Object[] newInstance(final int depth) {
    return new Object[depth];
  }

  @Override
  public Object decode(final JsonReader reader, char ch, final Binding<?> binding) throws DecodeException, IOException {
    if (ch == '"')
      return stringDecoder.decode(reader, ch, binding);

    if ('0' <= ch && ch <= '9' || ch == '.')
      return numberDecoder.decode(reader, ch, binding);

    if (ch == 't' || ch == 'f')
      return booleanDecoder.decode(reader, ch, binding);

    if (ch == '{')
      return objectDecoder.decode(reader, ch, binding);

    if (ch == '[')
      return decodeValue(ch, reader, null, Binding.ANY);

    if (JSObjectBase.isNull(ch, reader))
      return null;

    throw new DecodeException("Illegal char for " + getClass().getSimpleName() + ": " + ch, reader);
  }
}