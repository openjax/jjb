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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import jjb.type;

public class TypeTest extends TestHarness {
  private static <T extends type.Booleans>T setBooleans(final T jsObject) {
    setProperty(jsObject, "booleanDefault", Boolean.TRUE);
    setProperty(jsObject, "booleanNotRequired", Boolean.TRUE);
    setProperty(jsObject, "booleanNotNull", Boolean.TRUE);
    return jsObject;
  }

  @Test
  public void testBooleans() {
    final type.Booleans jsObject = setBooleans(new type.Booleans());

    testUnits(jsObject, "booleanDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "booleanNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "booleanNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  @Test
  public void testBooleanArrays() {
    final type.BooleanArrays jsObject = new type.BooleanArrays();
    setProperty(jsObject, "booleanArrayDefault", Boolean.TRUE, null, Boolean.FALSE);
    setProperty(jsObject, "booleanArrayNotRequired", Boolean.TRUE, null, Boolean.FALSE);
    setProperty(jsObject, "booleanArrayNotNull", Boolean.TRUE, null, Boolean.FALSE);

    testUnits(jsObject, "booleanArrayDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "booleanArrayNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "booleanArrayNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  private static type.Strings setStrings(final type.Strings jsObject) {
    setProperty(jsObject, "stringDefault", "valid");
    setProperty(jsObject, "stringNotRequired", "valid");
    setProperty(jsObject, "stringNotNull", "valid");
    setProperty(jsObject, "stringLength", "valid");
    setProperty(jsObject, "stringPattern", "valid");
    setProperty(jsObject, "stringUrlDecode", "url%20encoded");
    setProperty(jsObject, "stringUrlEncode", "url decoded");

    return jsObject;
  }

  @Test
  public void testStrings() {
    final type.Strings jsObject = setStrings(new type.Strings());
    setProperty(jsObject, "stringDefault", "valid");
    setProperty(jsObject, "stringNotRequired", "valid");
    setProperty(jsObject, "stringNotNull", "valid");
    setProperty(jsObject, "stringLength", "valid");
    setProperty(jsObject, "stringPattern", "valid");
    setProperty(jsObject, "stringUrlDecode", "url%20encoded");
    setProperty(jsObject, "stringUrlEncode", "url decoded");

    testUnits(jsObject, "stringDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringLength", lengthEncode, lengthDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringPattern", patternEncode, patternDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringUrlDecode", urlDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringUrlEncode", urlEncode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  private static <T extends type.StringArrays>T setStringArrays(final T jsObject) {
    setProperty(jsObject, "stringArrayDefault", "valid", null, "valid");
    setProperty(jsObject, "stringArrayNotRequired", "valid", null, "valid");
    setProperty(jsObject, "stringArrayNotNull", "valid", null, "valid");
    setProperty(jsObject, "stringArrayLength", "valid", null, "valid");
    setProperty(jsObject, "stringArrayPattern", "valid", null, "valid");
    setProperty(jsObject, "stringArrayUrlDecode", "url%20encoded", "valid", null, "valid");
    setProperty(jsObject, "stringArrayUrlEncode", "url decoded", "valid", null, "valid");
    return jsObject;
  }

  @Test
  public void testStringArrays() {
    final type.StringArrays jsObject = setStringArrays(new type.StringArrays());

    testUnits(jsObject, "stringArrayDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayLength", lengthEncode, lengthDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayPattern", patternEncode, patternDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayUrlDecode", urlDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "stringArrayUrlEncode", urlEncode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  @Test
  public void testNumbers() {
    final type.Numbers jsObject = new type.Numbers();
    setProperty(jsObject, "numberDefault", new BigDecimal("3.1415"));
    setProperty(jsObject, "numberNotRequired", new BigDecimal("2.7182"));
    setProperty(jsObject, "numberNotNull", new BigDecimal("9.8106"));
    setProperty(jsObject, "numberMin", new BigDecimal("3.1415"));
    setProperty(jsObject, "numberMax", new BigDecimal("-2.7182"));
    setProperty(jsObject, "numberInteger", BigInteger.TEN);

    testUnits(jsObject, "numberDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberMin", minEncode, minDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberMax", maxEncode, maxDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberInteger", integerEncode, integerDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  @Test
  public void testNumberArrays() {
    final type.NumberArrays jsObject = new type.NumberArrays();
    setProperty(jsObject, "numberArrayDefault", new BigDecimal("3.1415"), null, new BigDecimal("2.7182"));
    setProperty(jsObject, "numberArrayNotRequired", new BigDecimal("2.7182"), null, new BigDecimal("9.8106"));
    setProperty(jsObject, "numberArrayNotNull", new BigDecimal("9.8106"), null, new BigDecimal("3.1415"));
    setProperty(jsObject, "numberArrayMin", new BigDecimal("3.1415"), null, new BigDecimal("9.8106"));
    setProperty(jsObject, "numberArrayMax", new BigDecimal("-2.7182"), null, new BigDecimal("-3.1415"));
    setProperty(jsObject, "numberArrayInteger", BigInteger.TEN, null, BigInteger.ZERO);

    testUnits(jsObject, "numberArrayDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberArrayNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberArrayNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberArrayMin", minEncode, minDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberArrayMax", maxEncode, maxDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "numberArrayInteger", integerEncode, integerDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  private static type.Objects.ObjectExtendsAbstract setObjectExtendsAbstract(final type.Objects.ObjectExtendsAbstract jsObject) {
    setProperty(jsObject, "objectBoolean", Boolean.TRUE);
    setProperty(jsObject, "objectString", "string");
    setProperty(jsObject, "objectNumber", BigDecimal.TEN);
    setProperty(jsObject, "objectExtendsBooleans", setBooleans(new type.Objects.ObjectExtendsAbstract.ObjectExtendsBooleans()));
    return jsObject;
  }

  @Test
  public void testObjects() {
    final type.Objects jsObject = new type.Objects();
    setProperty(jsObject, "objectDefault", new type.Objects.ObjectDefault());
    setProperty(jsObject, "objectNotRequired", new type.Objects.ObjectNotRequired());
    setProperty(jsObject, "objectNotNull", new type.Objects.ObjectNotNull());
    setProperty(jsObject, "objectExtendsAbstract", setObjectExtendsAbstract(new type.Objects.ObjectExtendsAbstract()));
    final type.Objects.ObjectExtendsStrings objectExtendsStrings = new type.Objects.ObjectExtendsStrings();
    setProperty(jsObject, "objectExtendsStrings", setStrings(objectExtendsStrings));
    setProperty(objectExtendsStrings, "additionalString", "additional string");

    testUnits(jsObject, null, unknownFailDecode);

    testUnits(jsObject, "objectDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectExtendsAbstract", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectExtendsStrings", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }

  private static type.ObjectArrays.ObjectArrayPropertiesSkipUnknown setObjectArrayPropertiesSkipUnknown(final type.ObjectArrays.ObjectArrayPropertiesSkipUnknown jsObject) {
    setProperty(jsObject, "objectArrayBoolean", Boolean.TRUE, null, Boolean.FALSE);
    setProperty(jsObject, "objectArrayString", "valid", null, "valid");
    setProperty(jsObject, "objectArrayNumber", new BigDecimal("3.1415"), null, new BigDecimal("2.7182"));
    setProperty(jsObject, "objectArrayExtendsBooleans", setBooleans(new type.ObjectArrays.ObjectArrayPropertiesSkipUnknown.ObjectArrayExtendsBooleans()), null, setBooleans(new type.ObjectArrays.ObjectArrayPropertiesSkipUnknown.ObjectArrayExtendsBooleans()));
    return jsObject;
  }

  @Test
  public void testObjectArrays() {
    final type.ObjectArrays jsObject = new type.ObjectArrays();
    setProperty(jsObject, "objectBoolean", Boolean.TRUE);
    setProperty(jsObject, "objectArrayDefault", new type.ObjectArrays.ObjectArrayDefault(), null, new type.ObjectArrays.ObjectArrayDefault());
    setProperty(jsObject, "objectArrayNotRequired", new type.ObjectArrays.ObjectArrayNotRequired(), null, new type.ObjectArrays.ObjectArrayNotRequired());
    setProperty(jsObject, "objectArrayNotNull", new type.ObjectArrays.ObjectArrayNotNull(), null, new type.ObjectArrays.ObjectArrayNotNull());
    final type.ObjectArrays.ObjectArrayPropertiesSkipUnknown objectArrayPropertiesSkipUnknown = setObjectArrayPropertiesSkipUnknown(new type.ObjectArrays.ObjectArrayPropertiesSkipUnknown());

    setProperty(jsObject, "objectArrayPropertiesSkipUnknown", objectArrayPropertiesSkipUnknown, null, setObjectArrayPropertiesSkipUnknown(new type.ObjectArrays.ObjectArrayPropertiesSkipUnknown()));

    final type.ObjectArrays.ObjectArrayExtendsStringArrays objectArrayExtendsStringArrays1 = setStringArrays(new type.ObjectArrays.ObjectArrayExtendsStringArrays());
    setProperty(objectArrayExtendsStringArrays1, "additionalStringArray", "one", "two", null, "three");
    final type.ObjectArrays.ObjectArrayExtendsStringArrays objectArrayExtendsStringArrays2 = setStringArrays(new type.ObjectArrays.ObjectArrayExtendsStringArrays());
    setProperty(objectArrayExtendsStringArrays2, "additionalStringArray", "four", "five", null, "six");

    setProperty(jsObject, "objectArrayExtendsStringArrays", objectArrayExtendsStringArrays1, null, objectArrayExtendsStringArrays2);

    testUnits(jsObject, "objectArrayDefault", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectArrayNotRequired", encodeDecodeSuccess, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectArrayNotNull", nullEncode, nullDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(jsObject, "objectArrayPropertiesSkipUnknown", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
    testUnits(objectArrayPropertiesSkipUnknown, null, unknownPassDecode);

    testUnits(jsObject, "objectArrayExtendsStringArrays", requiredDecode, incorrectTypeEncode, incorrectTypeDecode, incorrectArrayEncode, incorrectArrayDecode);
  }
}