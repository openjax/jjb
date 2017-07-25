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

package org.libx4j.jjb.runtime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.lib4j.lang.Numbers;
import org.lib4j.net.URIComponent;
import org.lib4j.util.Collections;
import org.lib4j.util.RewindableReader;
import org.libx4j.jjb.runtime.decoder.StringDecoder;

public class Property<T> {
  @SuppressWarnings("unchecked")
  private static <T>T encode(final T value, final JSObject jsObject, final Binding<T> binding) {
    if (value != null && !binding.type.isAssignableFrom(value.getClass()))
      throw new EncodeException("\"" + binding.name + "\": " + value.getClass().getName() + " cannot be encoded as " + binding.type.getName(), jsObject);

    if (value instanceof String) {
      final String escaped = StringDecoder.escapeString((String)value);
      return (T)(binding.urlEncode ? URIComponent.encode(escaped) : escaped);
    }

    return value;
  }

  @SuppressWarnings("unchecked")
  private static <T>T decode(final T value, final JSObject jsObject) {
    return value instanceof String ? (T)URIComponent.decode(((String)value)) : value;
  }

  private final JSObject jsObject;
  protected final Binding<T> binding;
  private boolean required;
  private boolean present = false;
  private T value;

  public Property(final JSObject jsObject, final Binding<T> binding) {
    this.jsObject = jsObject;
    this.binding = binding;
    this.required = binding.required;
  }

  protected void clone(final Property<T> clone) {
    this.present = clone.present;
    this.value = clone.value;
  }

  protected boolean isTypeAssignable(final T value) {
    final Class<?> type;
    return value == null || (binding.array ? value instanceof List && ((type = Collections.getComponentType((List<?>)value)) == null || binding.type.isAssignableFrom(type)) : binding.type.isAssignableFrom(type = value.getClass()));
  }

  public void set(final T value) {
    // FIXME: This check is not necessary in the real world, as the only way it
    // FIXME: is possible call set() with an incorrectly typed object is with
    // FIXME: raw or missing (thus unchecked) generics, or thru reflection.
//    if (!isTypeAssignable(value))
//      throw new ClassCastException(binding.type.getName() + " incompatible with " + (binding.array ? List.class.getName() + "<" + value.getClass().getName() + ">" : value.getClass().getName()));

    this.present = true;
    this.value = value;
  }

  public T get() {
    return value;
  }

  public void clear() {
    this.present = false;
    this.value = null;
  }

  protected boolean required() {
    return required;
  }

  public boolean present() {
    return present;
  }

  @SuppressWarnings("unchecked")
  protected T encode() throws EncodeException {
    final String error = binding.validate(value);
    if (error != null)
      throw new EncodeException(error, jsObject);

    if (!binding.isAssignable(value))
      throw new EncodeException("\"" + binding.name + "\": " + value.getClass().getName() + " cannot be encoded as " + (binding.array ? List.class.getName() + "<" + value.getClass().getName() + ">" : value.getClass().getName()), jsObject);

    if (value instanceof Collection<?>) {
      final Collection<T> collection = (Collection<T>)value;
      final Collection<T> encoded = new JSArray<T>(collection.size());
      for (final T member : collection)
        encoded.add(encode(member, jsObject, binding));

      return (T)encoded;
    }

    return encode(value, jsObject, binding);
  }

  @SuppressWarnings("unchecked")
  protected void decode(final RewindableReader reader) throws DecodeException, IOException {
    final String error = binding.validate(value);
    if (error != null)
      throw new DecodeException(error, reader);

    if (value instanceof Collection<?>) {
      final Collection<T> collection = (Collection<T>)value;
      final Collection<T> decoded = new ArrayList<T>(collection.size());
      for (final T member : collection)
        decoded.add(decode(member, jsObject));

      this.value = (T)decoded;
    }
    else {
      this.value = decode(value, jsObject);
    }
  }

  @Override
  public int hashCode() {
    final int hashCode = 1917841483;
    return value == null ? hashCode : hashCode ^ 31 * value.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this)
      return true;

    if (!(obj instanceof Property))
      return false;

    final Property<?> that = (Property<?>)obj;
    if (that.value instanceof Number)
      return Numbers.equivalent((Number)value, (Number)that.value);

    return that.value != null ? that.value.equals(value) : value == null;
  }
}