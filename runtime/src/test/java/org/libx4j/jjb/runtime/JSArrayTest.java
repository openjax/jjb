/* Copyright (c) 2017 lib4j
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
import java.io.StringReader;
import java.math.BigInteger;

import org.junit.Assert;
import org.junit.Test;
import org.libx4j.jjb.runtime.DecodeException;
import org.libx4j.jjb.runtime.JSArray;
import org.libx4j.jjb.runtime.JSObject;

public class JSArrayTest {
  @Test
  public void testEmpty() throws DecodeException, IOException {
    final String data = "[]";
    final JSArray<?> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(0, jsObj.size());
  }

  @Test
  public void test1() throws DecodeException, IOException {
    final String data = "[0, 1, 2, 3, 4]";
    final JSArray<BigInteger> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(5, jsObj.size());
    for (int i = 0; i < 5; i++)
      Assert.assertEquals(i, jsObj.get(i).intValue());
  }

  @Test
  public void test2() throws DecodeException, IOException {
    final String data = "[[0, 1, 2, 3, 4]]";
    final JSArray<JSArray<BigInteger>> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(1, jsObj.size());
    final JSArray<BigInteger> inner = jsObj.get(0);
    for (int i = 0; i < 5; i++)
      Assert.assertEquals(i, inner.get(i).intValue());
  }

  @Test
  public void test3() throws DecodeException, IOException {
    final String data = "[[[0, 1, 2, 3, 4]]]";
    final JSArray<JSArray<JSArray<BigInteger>>> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(1, jsObj.size());
    final JSArray<JSArray<BigInteger>> inner = jsObj.get(0);
    Assert.assertEquals(1, inner.size());
    final JSArray<BigInteger> inner2 = inner.get(0);
    for (int i = 0; i < 5; i++)
      Assert.assertEquals(i, inner2.get(i).intValue());
  }

  @Test
  public void test21() throws DecodeException, IOException {
    final String data = "[[[0, 1, 2, 3, 4]], [[0, 1, 2, 3, 4]], [[0, 1, 2, 3, 4]]]";
    final JSArray<JSArray<JSArray<BigInteger>>> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(3, jsObj.size());
    for (int i = 0; i < 3; i++) {
      final JSArray<JSArray<BigInteger>> inner = jsObj.get(i);
      Assert.assertEquals(1, inner.size());
      final JSArray<BigInteger> inner2 = inner.get(0);
      for (int j = 0; j < 5; j++)
        Assert.assertEquals(j, inner2.get(j).intValue());
    }
  }

  @Test
  public void test31() throws DecodeException, IOException {
    final String data = "[[[0, 1, 2, 3, 4], [0, 1, 2, 3, 4], [0, 1, 2, 3, 4]]]";
    final JSArray<JSArray<JSArray<BigInteger>>> jsObj = JSObject.parse(JSArray.class, new StringReader(data));
    Assert.assertEquals(1, jsObj.size());
    final JSArray<JSArray<BigInteger>> inner = jsObj.get(0);
    Assert.assertEquals(3, inner.size());
    for (int i = 0; i < 3; i++) {
      final JSArray<BigInteger> inner2 = inner.get(i);
      for (int j = 0; j < 5; j++)
        Assert.assertEquals(j, inner2.get(j).intValue());
    }
  }
}