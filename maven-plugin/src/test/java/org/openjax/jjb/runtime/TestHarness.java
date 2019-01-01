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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.openjax.classic.math.BigDecimals;
import org.openjax.classic.math.BigIntegers;

public abstract class TestHarness {
  @SuppressWarnings({"rawtypes", "unchecked"})
  protected static void setProperty(final JSObject jsObject, final String propertyName, final Object ... value) {
    if (value.length == 0)
      throw new IllegalArgumentException("value.length == 0");

    final Property propetry = TestHelper.property(jsObject, propertyName);

    final Object val = value.length == 1 ? value[0] : Arrays.asList(value);
    // Set the value
    propetry.set(val);

    // Assert that the get method returns the value that's been set
    assertEquals(val, TestHelper.property(jsObject, propertyName).get());

    // Clear the property
    propetry.clear();

    // Assert that after property.clear(), the get method returns null
    assertNull(propetry.get());

    // Set the value again
    propetry.set(val);
  }

  @SafeVarargs
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected static void testUnits(final JSObject jsObject, final String propertyName, final Unit ... units) {
    final JSObject clone = jsObject.clone();
    if (units.length == 0)
      throw new IllegalArgumentException("Must provide at least one unit");

    for (final Unit unit : units) {
      final Object validValue = propertyName != null ? TestHelper.property(clone, propertyName).get() : null;
      final Object condition = unit.instigate(clone, propertyName);
      if (!unit.validate(clone, propertyName, condition))
        fail("Failed \"" + unit.getName() + "\" unit:\n" + condition);

      if (propertyName != null)
        TestHelper.property(clone, propertyName).set(validValue);
    }
  }

  protected static abstract class Unit<T> {
    private final String name;

    public Unit(final String name) {
      this.name = name;
    }

    public String getName() {
      return this.name;
    }

    public abstract T instigate(final JSObject jsObject, final String propertyName);
    public abstract boolean validate(final JSObject jsObject, final String propertyName, final T result);
  }

  protected static abstract class EncodeUnit extends Unit<Exception> {
    public EncodeUnit(final String name) {
      super(name);
    }

    public abstract void condition(final JSObject jsObject, final String propertyName);

    @Override
    public final Exception instigate(final JSObject jsObject, final String propertyName) {
      try {
        condition(jsObject, propertyName);
        final String json = jsObject.toString();
        throw new AssertionError("Expected " + getClass().getSimpleName() + " to instigate condition \"" + getName() + "\"\n" + json);
      }
      catch (final EncodeException e) {
        return e;
      }
    }
  }

  protected static abstract class DecodeUnit extends Unit<Exception> {
    public DecodeUnit(final String name) {
      super(name);
    }

    public abstract String condition(final JSObject jsObject, final String propertyName);

    @Override
    public final Exception instigate(final JSObject jsObject, final String propertyName) {
      try {
        final String json = condition(jsObject, propertyName);
        JSObject.parse(jsObject.getClass(), new StringReader(json));
        throw new AssertionError("Expected " + getClass().getSimpleName() + " to instigate condition with \"" + getName() + "\": " + json);
      }
      catch (final DecodeException e) {
        return e;
      }
      catch (final IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  protected static final EncodeUnit nullEncode = new EncodeUnit("null encode") {
    @Override
    public void condition(final JSObject jsObject, final String propertyName) {
      TestHelper.property(jsObject, propertyName).set(null);
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("cannot be null");
    }
  };

  protected static final DecodeUnit nullDecode = new DecodeUnit("null decode") {
    @Override
    public String condition(final JSObject jsObject, final String propertyName) {
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "\"" + propertyName + "\": null");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("cannot be null");
    }
  };

  protected static final DecodeUnit requiredDecode = new DecodeUnit("required decode") {
    @Override
    public String condition(final JSObject jsObject, final String propertyName) {
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("is required");
    }
  };

  protected static final EncodeUnit lengthEncode = new EncodeUnit("length encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList("invalid") : "invalid");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("is longer than");
    }
  };

  protected static final DecodeUnit lengthDecode = new DecodeUnit("length decode") {
    @Override
    @SuppressWarnings("rawtypes")
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "\"" + propertyName + "\": " + (property.binding.array ? "[\"this is too long\"]" : "\"this is too long\""));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("is longer than");
    }
  };

  protected static final EncodeUnit patternEncode = new EncodeUnit("pattern encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList("invalid") : "invalid");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("does not match pattern");
    }
  };

  protected static final DecodeUnit patternDecode = new DecodeUnit("pattern decode") {
    @Override
    @SuppressWarnings("rawtypes")
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "\"" + propertyName + "\": " + (property.binding.array ? "[\"invalid\"]" : "\"invalid\""));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("does not match pattern");
    }
  };

  protected static final Unit<JSObject> urlDecode = new Unit<JSObject>("urlDecode") {
    @Override
    public JSObject instigate(final JSObject jsObject, final String propertyName) {
      try {
        final String json = jsObject.toString();
        return JSObject.parse(jsObject.getClass(), new StringReader(json));
      }
      catch (final DecodeException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final JSObject result) {
      return TestHelper.property(result, propertyName).get().toString().contains("url encoded");
    }
  };

  protected static final Unit<String> urlEncode = new Unit<String>("urlEncode") {
    @Override
    public String instigate(final JSObject jsObject, final String propertyName) {
      return jsObject.toString();
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final String result) {
      return result.contains("url%20decoded");
    }
  };

  protected static final EncodeUnit minEncode = new EncodeUnit("min encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigDecimals.of("-1.1")) : BigDecimals.of("-1.1"));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("min") && result.getMessage().contains("violated");
    }
  };

  protected static final DecodeUnit minDecode = new DecodeUnit("min decode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigDecimals.of("0.0")) : BigDecimals.of("0.0"));
      return jsObject.toString().replaceAll("0.0", "-1");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("min") && result.getMessage().contains("violated");
    }
  };

  protected static final EncodeUnit maxEncode = new EncodeUnit("max encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigDecimals.of("1.1")) : BigDecimals.of("1.1"));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("max") && result.getMessage().contains("violated");
    }
  };

  protected static final DecodeUnit maxDecode = new DecodeUnit("max decode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigDecimals.of("0.0")) : BigDecimals.of("0.0"));
      return jsObject.toString().replaceAll("0.0", "1.1");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("max") && result.getMessage().contains("violated");
    }
  };

  protected static final EncodeUnit integerEncode = new EncodeUnit("integer encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigDecimals.of("3.1415")) : BigDecimals.of("3.1415"));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("is not an \"integer\"");
    }
  };

  protected static final DecodeUnit integerDecode = new DecodeUnit("integer decode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      property.set(property.binding.array ? Collections.singletonList(BigIntegers.of("989898")) : BigIntegers.of("989898"));
      return jsObject.toString().replaceAll("989898", "3.1415");
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("is not an \"integer\"");
    }
  };

  protected static final Unit<Exception> encodeDecodeSuccess = new Unit<Exception>("success") {
    @Override
    public Exception instigate(final JSObject jsObject, final String propertyName) {
      try {
        JSObject.parse(jsObject.getClass(), new StringReader(jsObject.toString()));
        return null;
      }
      catch (final DecodeException | IOException e) {
        return e;
      }
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result == null;
    }
  };

  protected static final EncodeUnit incorrectTypeEncode = new EncodeUnit("incorrect type encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      final Object value = Number.class.isAssignableFrom(property.binding.type) ? "oops" : property.binding.type == Boolean.class ? BigInteger.ZERO : property.binding.type == String.class ? Boolean.FALSE : "{}";
      property.set(property.binding.array ? Collections.singletonList(value) : value);
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("cannot be encoded");
    }
  };

  protected static final DecodeUnit incorrectTypeDecode = new DecodeUnit("incorrect type decode") {
    @Override
    @SuppressWarnings("rawtypes")
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      final Object value = Number.class.isAssignableFrom(property.binding.type) ? "\"oops\"" : property.binding.type == Boolean.class ? BigInteger.ZERO : property.binding.type == String.class ? "{}" : "false";
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "\"" + propertyName + "\": " + (property.binding.array ? "[" + value + "]" : value));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("Illegal char") || result.getMessage().contains("but found") || result.getMessage().contains("is required");
    }
  };

  protected static final EncodeUnit incorrectArrayEncode = new EncodeUnit("incorrect array encode") {
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      final Object value = Number.class.isAssignableFrom(property.binding.type) ? BigInteger.ZERO : property.binding.type == String.class ? "valid" : property.binding.type == Boolean.class ? "true" : "{}";
      property.set(property.binding.array ? value : Collections.singletonList(value));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("cannot be encoded");
    }
  };

  protected static final DecodeUnit incorrectArrayDecode = new DecodeUnit("incorrect array decode") {
    @Override
    @SuppressWarnings("rawtypes")
    public String condition(final JSObject jsObject, final String propertyName) {
      final Property property = TestHelper.property(jsObject, propertyName);
      final Object value = Number.class.isAssignableFrom(property.binding.type) ? BigInteger.ZERO : property.binding.type == String.class ? "\"valid\"" : property.binding.type == Boolean.class ? "true" : "{}";
      return TestHelper.replaceProperty(jsObject.toString(), propertyName, "\"" + propertyName + "\": " + (property.binding.array ? value : "[" + value + "]"));
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("incompatible with") || result.getMessage().contains("is required");
    }
  };

  protected static final Unit<JSObject> unknownPassDecode = new Unit<JSObject>("unknown pass decode") {
    @Override
    public JSObject instigate(final JSObject jsObject, final String propertyName) {
      try {
        final String json = "{\"unknown\": true," + jsObject.toString().substring(1);
        return JSObject.parse(jsObject.getClass(), new StringReader(json));
      }
      catch (final DecodeException | IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final JSObject result) {
      return true;
    }
  };

  protected static final DecodeUnit unknownFailDecode = new DecodeUnit("unknown fail decode") {
    @Override
    public String condition(final JSObject jsObject, final String propertyName) {
      return "{\"unknown\": true," + jsObject.toString().substring(1);
    }

    @Override
    public boolean validate(final JSObject jsObject, final String propertyName, final Exception result) {
      return result.getMessage().contains("Unknown property");
    }
  };
}