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

package org.openjax.jjb.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

public abstract class JSObject extends JSObjectBase implements Cloneable {
  public static <T extends JSObject>T parse(final Class<?> type, final InputStream in) throws DecodeException, IOException {
    return parse(type, new InputStreamReader(in));
  }

  @SuppressWarnings("unchecked")
  public static <T extends JSObject>T parse(final Class<?> type, final Reader reader) throws DecodeException, IOException {
    try {
      final JsonReader replayReader = reader instanceof JsonReader ? (JsonReader)reader : new JsonReader(reader);
      final char ch = next(replayReader);

      if (ch == '[')
        return (T)decodeValue(ch, replayReader, null, Binding.ANY);

      if (!JSObject.class.isAssignableFrom(type))
        throw new DecodeException("Expected a JSObject type " + type.getName(), replayReader, null);

      return (T)decode(replayReader, ch, ((Class<T>)type).getDeclaredConstructor().newInstance());
    }
    catch (final IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  public JSObject(final JSObject object) {
  }

  public JSObject() {
  }

  protected abstract String _getPath();

  protected String _encode(final int depth) {
    return "";
  }

  protected abstract String _name();
  protected abstract Binding<?> _getBinding(final String name);
  protected abstract Collection<Binding<?>> _bindings();
  protected abstract boolean _skipUnknown();
  protected abstract JSBundle _bundle();

  @Override
  public abstract JSObject clone();

  public abstract java.lang.String toExternalForm();
}