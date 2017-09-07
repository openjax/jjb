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

package org.libx4j.jjb.runtime;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lib4j.lang.Arrays;
import org.lib4j.lang.PackageLoader;
import org.lib4j.lang.PackageNotFoundException;
import org.lib4j.util.Collections;
import org.lib4j.util.RewindableReader;
import org.libx4j.jjb.runtime.decoder.BooleanDecoder;
import org.libx4j.jjb.runtime.decoder.JSObjectDecoder;
import org.libx4j.jjb.runtime.decoder.NumberDecoder;
import org.libx4j.jjb.runtime.decoder.ObjectDecoder;
import org.libx4j.jjb.runtime.decoder.StringDecoder;

public abstract class JSObjectBase {
  private static final BooleanDecoder booleanDecoder = new BooleanDecoder();
  private static final NumberDecoder numberDecoder = new NumberDecoder();
  private static final StringDecoder stringDecoder = new StringDecoder();
  private static final JSObjectDecoder jsObjectDecoder = new JSObjectDecoder();
  private static final ObjectDecoder objectDecoder = new ObjectDecoder(jsObjectDecoder, stringDecoder, numberDecoder, booleanDecoder);

  private static final Map<String,Class<? extends JSObject>> bindings = new HashMap<String,Class<? extends JSObject>>();

  static {
    try {
      PackageLoader.getSystemContextPackageLoader().loadPackage("json");
    }
    catch (final PackageNotFoundException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  protected static <T>void clone(final Property<T> property, final Property<T> clone) {
    property.clone(clone);
  }

  protected static <T>T encode(final Property<T> property) throws EncodeException {
    return property.encode();
  }

  protected static <T>void decode(final Property<T> property, final RewindableReader reader) throws DecodeException, IOException {
    property.decode(reader);
  }

  protected static void registerBinding(final String name, final Class<? extends JSObject> bindingClass) {
    bindings.put(name, bindingClass);
  }

  protected static char next(final Reader reader) throws IOException {
    int ch;
    while (true) {
      if ((ch = reader.read()) == -1)
        throw new IOException("EOS");

      if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r')
        return (char)ch;
    }
  }

  protected static char nextAny(final Reader reader) throws IOException {
    int ch;
    while (true) {
      if ((ch = reader.read()) == -1)
        throw new IOException("EOS");

      return (char)ch;
    }
  }

  protected static boolean isNull(char ch, final Reader reader) throws IOException {
    if (ch != 'n')
      return false;

    reader.mark(1);
    if (next(reader) != 'u') {
      reader.reset();
      return false;
    }

    reader.mark(1);
    if (next(reader) != 'l') {
      reader.reset();
      return false;
    }

    reader.mark(1);
    if (next(reader) != 'l') {
      reader.reset();
      return false;
    }

    return true;
  }

  protected static boolean required(final Property<?> property) {
    return property.required();
  }

  protected static char[] pad(final int depth) {
    return Arrays.createRepeat(' ', depth * 2);
  }

  protected static String encode(final JSObject object, final int depth) {
    if (object instanceof JSArray)
      return object._encode(depth);

    final StringBuilder string = new StringBuilder("{\n");
    string.append(object._encode(depth)).append("\n").append(pad(depth - 1)).append("}");
    return string.toString();
  }

  protected static String toString(final Object part, final int depth) {
    return part == null ? "null" : part instanceof JSObject ? encode((JSObject)part, depth) : part instanceof String ? "\"" + part + "\"" : String.valueOf(part);
  }

  protected static Object decodeValue(final char ch, final RewindableReader reader, final Class<?> type, final Binding<?> binding) throws DecodeException, IOException {
    final boolean isArray = ch == '[';
    if (type == null)
      return isArray ? Collections.asCollection(new JSArray(), objectDecoder.recurse(reader, 0, binding)) : objectDecoder.decode(reader, ch, binding);

    if (JSObject.class.isAssignableFrom(type))
      return isArray ? Collections.asCollection(new JSArray(), jsObjectDecoder.recurse(reader, 0, binding)) : jsObjectDecoder.decode(reader, ch, binding);

    if (type == String.class)
      return isArray ? Collections.asCollection(new JSArray(), stringDecoder.recurse(reader, 0, binding)) : stringDecoder.decode(reader, ch, binding);

    if (type == Boolean.class)
      return isArray ? Collections.asCollection(new JSArray(), booleanDecoder.recurse(reader, 0, binding)) : booleanDecoder.decode(reader, ch, binding);

    if (Number.class.isAssignableFrom(type))
      return isArray ? Collections.asCollection(new JSArray(), numberDecoder.recurse(reader, 0, binding)) : numberDecoder.decode(reader, ch, binding);

    throw new UnsupportedOperationException("Unsupported type: " + type);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  protected static JSObject decode(final RewindableReader reader, char ch, final JSObject jsObject) throws DecodeException, IOException {
    boolean inObject = false;
    boolean inQuote = false;
    final StringBuilder builder = new StringBuilder();
    while (true) {
      if (ch == '{') {
        if (inObject)
          throw new DecodeException("Malformed JSON", reader);

        inObject = true;
      }
      else {
        if (!inObject && isNull(ch, reader))
          return null;

        try {
          if (ch == '"') {
            if (!inQuote) {
              inQuote = true;
            }
            else {
              inQuote = false;
              ch = next(reader);
              if (ch != ':')
                throw new DecodeException("Expected \":\" character", reader);

              // Special case for parsing the container object
              Binding<?> member = jsObject == null ? Binding.ANY : jsObject._getBinding(builder.toString());
              if (member == null) {
                if (jsObject._skipUnknown())
                  member = Binding.ANY;
                else
                  throw new DecodeException("Unknown property name: " + builder, reader);
              }

              builder.setLength(0);
              ch = next(reader);

              final Object value = decodeValue(ch, reader, member.type, member);

              if (member.required && member.notNull && value == null)
                throw new DecodeException("\"" + member.name + "\" cannot be null", reader);

              if (member != Binding.ANY) {
                final Property property = (Property)member.property.get(jsObject);
                if (!property.binding.isAssignable(value))
                  throw new DecodeException("\"" + property.binding.name + "\": " + property.binding.type.getName() + " incompatible with " + (property.binding.array ? List.class.getName() + "<" + value.getClass().getName() + ">" : value.getClass().getName()), reader);

                property.set(value);
                property.decode(reader);
              }
            }
          }
          else {
            if (ch == '}') {
              if (jsObject == null)
                return null;

              for (final Binding<?> binding : jsObject._bindings()) {
                final Property<?> property = (Property<?>)binding.property.get(jsObject);
                if (binding.required) {
                  if (!property.present())
                    throw new DecodeException("\"" + binding.name + "\" is required", reader);

                  if (binding.notNull && property.get() == null)
                    throw new DecodeException("\"" + binding.name + "\" cannot be null", reader);
                }
                else if (property.present() && binding.notNull && property.get() == null) {
                  throw new DecodeException("\"" + binding.name + "\" cannot be null", reader);
                }
              }

              return jsObject;
            }

            if (ch == '[')
              return (JSArray)decodeValue(ch, reader, null, Binding.ANY);

            if (ch != ',')
              builder.append(ch);
            else if (!inQuote && builder.length() > 0)
              throw new DecodeException("Expected \"{}\" but found \"" + builder + "\"", reader);
          }
        }
        catch (final IllegalAccessException e) {
          throw new UnsupportedOperationException(e);
        }
      }

      ch = next(reader);
    }
  }
}