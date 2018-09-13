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
import java.lang.reflect.InvocationTargetException;

import org.openjax.jjb.runtime.Binding;
import org.openjax.jjb.runtime.DecodeException;
import org.openjax.jjb.runtime.JSObject;
import org.openjax.jjb.runtime.JSObjectBase;
import org.openjax.jjb.runtime.JsonReader;

public class JSObjectDecoder extends Decoder<JSObject> {
  @Override
  protected JSObject[] newInstance(final int depth) {
    return new JSObject[depth];
  }

  @Override
  public JSObject decode(final JsonReader reader, char ch, final Binding<?> clazz) throws DecodeException, IOException {
    try {
      return JSObjectBase.decode(reader, ch, clazz.type == null ? null : (JSObject)clazz.type.getDeclaredConstructor().newInstance());
    }
    catch (final IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      throw new UnsupportedOperationException(e);
    }
  }
}