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

import java.io.InputStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Test;
import org.fastjax.util.FastCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jjb.api;

public class JSObjectTest {
  private static final Logger logger = LoggerFactory.getLogger(JSObjectTest.class);

  private static String external(final String encoded) {
    return encoded.replaceAll(": ", ":").replaceAll("([,{])\\s+", "$1").replaceAll("\\s+}", "}");
  }

  @Test
  public void testJSObject() throws Exception {
    //Generator.generate(Thread.currentThread().getContextClassLoader().getResource("json.xml"), new File("target/generated-test-sources/json"));

    final api.Message.Attachment att1 = new api.Message.Attachment();
    att1.serial.set(BigDecimal.valueOf(2));
    final api.Message.Attachment.Data data1 = new api.Message.Attachment.Data();
    att1.data.set(data1);
    data1.a.set("\"1A");
    data1.b.set("\\1B");
    data1.c.set("1C");

    try {
      att1.toString();
    }
    catch (final EncodeException e) {
      if (!e.getMessage().startsWith("message.attachment.filename is required"))
        throw e;
    }

    att1.filename.set(null);

    try {
      att1.toString();
    }
    catch (final EncodeException e) {
      if (!e.getMessage().startsWith("message.attachment.filename cannot be null"))
        throw e;
    }

    att1.filename.set("data1.txt");

    final api.Message.Attachment att2 = new api.Message.Attachment();
    final api.Message.Attachment.Data data2 = new api.Message.Attachment.Data();
    att2.data.set(data2);
    data2.a.set("2X");
    data2.b.set("2B");
    data2.c.set("2C");

    att2.filename.set("data2.txt");
    att2.serial.set(BigDecimal.valueOf(-2.424242424));

    try {
      att2.toString();
    }
    catch (final EncodeException e) {
      if (!e.getMessage().startsWith("message.attachment.data.a does not match pattern"))
        throw e;
    }

    data2.a.set("2A");

    final api.Message.Attachment att3 = new api.Message.Attachment();
    att3.filename.set("data3.txt");
    final api.Message.Attachment.Data data3 = new api.Message.Attachment.Data();
    att3.data.set(data3);
    data3.a.set("\"3A");
    data3.b.set("\\3B");
    data3.c.set("3C");
    att3.serial.set(BigDecimal.valueOf(99999));

    final api.Signature signature = new api.Signature();
    signature.pubRsa.set("pub_rsa");
    signature.xmldsig.set("xmldsig");

    final api.Message message = new api.Message();
    final String subject = "Test subject";
    message.subject.set(subject);
    final String url = "http://www.thesaurus.com/browse/cool?s=t";
    message.url.set(url);
    message.important.set(true);
    final List<String> recipients = FastCollections.asCollection(new ArrayList<String>(), "alex", "seva");
    message.recipients.set(recipients);
    message.emptyarray.set(new ArrayList<String>());
    final api.Message.Attachment[] attachment = {att1, att2, att3, null};
    message.attachment.set(Arrays.asList(attachment));
    message.signature.set(signature);

    String encoded;
    try {
      encoded = message.toString();
    }
    catch (final EncodeException e) {
      if (!e.getMessage().startsWith("message.requiredArray is required"))
        throw e;
    }

    message.requiredArray.set(null);
    try {
      encoded = message.toString();
    }
    catch (final EncodeException e) {
      if (!e.getMessage().startsWith("message.requiredArray cannot be null"))
        throw e;
    }

    message.requiredArray.set(new ArrayList<Boolean>());

    message.notRequired.set(true);
    message.notRequired.clear();

    message.notRequiredArray.set((List<Boolean>)null);
    message.notRequiredArray.clear();

    encoded = message.toString();
    if (encoded.indexOf("notRequired") != -1)
      fail("message.notRequired or message.notRequiredArray should not be present in the encoded string");

    logger.info(encoded);
    assertEquals(external(encoded), message.toExternalForm());
    try {
      JSObject.parse(api.Message.class, new StringReader(encoded.replace("3A", "3a")));
    }
    catch (final DecodeException e) {
      if (!e.getMessage().startsWith("message.attachment.data.a does not match pattern"))
        throw e;
    }

    try {
      JSObject.parse(api.Message.class, new StringReader(encoded.replace("\"filename\": \"data3.txt\", ", "")));
    }
    catch (final DecodeException e) {
      if (!e.getMessage().startsWith("\"filename\" is missing"))
        throw e;
    }

    final api.Message decoded = JSObject.parse(api.Message.class, new StringReader(encoded));
    final String reEncoded = decoded.toString();
    logger.info(reEncoded);

    assertEquals(encoded, reEncoded);
    assertEquals(subject, decoded.subject.get());
    assertEquals(url, decoded.url.get());
    assertEquals(true, decoded.important.get());
    assertEquals(recipients, decoded.recipients.get());
    assertEquals(0, decoded.emptyarray.get().size());
    assertArrayEquals(attachment, decoded.attachment.get().toArray());
    assertEquals(signature, decoded.signature.get());
  }

  @Test
  public void testPayPalObject() throws Exception {
    JSObject.parse(api.PayPalEvent.class, new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("paypal.json")));
  }

  @Test
  public void testGiphyObject() throws Exception {
    final api.Giphy giphy = JSObject.parse(api.Giphy.class, new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("giphy.json")));
    final String expected = "{\n  \"data\": [{\n    \"id\": \"iuHaJ0D7macZq\",\n    \"url\": \"http://giphy.com/gifs/cat-day-tomorrow-iuHaJ0D7macZq\"\n  }, {\n    \"id\": \"Z1kpfgtHmpWHS\",\n    \"url\": \"http://giphy.com/gifs/cat-way-make-Z1kpfgtHmpWHS\"\n  }, {\n    \"id\": \"ZpWJhFSusGSac\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-ZpWJhFSusGSac\"\n  }, {\n    \"id\": \"dLd4DTFSeGwc8\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-dLd4DTFSeGwc8\"\n  }, {\n    \"id\": \"gWFhtJxDgDUXK\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-gWFhtJxDgDUXK\"\n  }, {\n    \"id\": \"cTaC7ryiBZIxG\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-cTaC7ryiBZIxG\"\n  }, {\n    \"id\": \"ZjrZpeIkBclpK\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-ZjrZpeIkBclpK\"\n  }, {\n    \"id\": \"dir7Th3EEsA6s\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-dir7Th3EEsA6s\"\n  }, {\n    \"id\": \"bKrynkeJjTKow\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-bKrynkeJjTKow\"\n  }, {\n    \"id\": \"csBIJ9q2AXx1m\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-csBIJ9q2AXx1m\"\n  }, {\n    \"id\": \"Xnlz8u878NR8A\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-Xnlz8u878NR8A\"\n  }, {\n    \"id\": \"SBIDrovnm0wOA\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-SBIDrovnm0wOA\"\n  }, {\n    \"id\": \"QJVrZI6VHmt4Q\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-QJVrZI6VHmt4Q\"\n  }, {\n    \"id\": \"VIcgtjYlDLfkk\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-VIcgtjYlDLfkk\"\n  }, {\n    \"id\": \"VDYtzvOm5fQcM\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-VDYtzvOm5fQcM\"\n  }, {\n    \"id\": \"OvJuHbDfem9Co\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-OvJuHbDfem9Co\"\n  }, {\n    \"id\": \"PFifISGfIz2vK\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-PFifISGfIz2vK\"\n  }, {\n    \"id\": \"Mp592a0PuVzbi\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-Mp592a0PuVzbi\"\n  }, {\n    \"id\": \"MYXU72VlYV3i\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-MYXU72VlYV3i\"\n  }, {\n    \"id\": \"GOWYG0kaBDkwo\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-GOWYG0kaBDkwo\"\n  }, {\n    \"id\": \"FMppCCrk97sB2\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-FMppCCrk97sB2\"\n  }, {\n    \"id\": \"HHp5GPMG5PwHu\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-HHp5GPMG5PwHu\"\n  }, {\n    \"id\": \"DDqfQ5YNY6yLC\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-DDqfQ5YNY6yLC\"\n  }, {\n    \"id\": \"Cw8m4xnyJJQ7m\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-Cw8m4xnyJJQ7m\"\n  }, {\n    \"id\": \"BVwi1Pwm6kaT6\",\n    \"url\": \"http://giphy.com/gifs/funny-cat-BVwi1Pwm6kaT6\"\n  }]\n}";
    assertEquals(expected, giphy.toString());
  }

  private static void assertJSObject(final JSObject object, final Class<?> type) throws Exception {
    final String string = object.toString();
    final JSObject parsed = JSObject.parse(type, new StringReader(string));
    assertEquals(object, parsed);
    assertEquals(string, parsed.toString());
  }

  @Test
  public void testArray() throws Exception {
    final JSArray<String> array1 = new JSArray<>();
    array1.add("a");
    array1.add("b");
    array1.add("c");
    assertJSObject(array1, String.class);

    final JSArray<Number> array2 = new JSArray<>();
    array2.add(1);
    array2.add(2);
    array2.add(3);
    logger.info(array2.toString());

    final JSArray<api.Dsig> array3 = new JSArray<>();
    api.Dsig dsig = new api.Dsig();
    dsig.xmldsig.set("one");
    array3.add(dsig);

    dsig = new api.Dsig();
    dsig.xmldsig.set("two");
    array3.add(dsig);

    dsig = new api.Dsig();
    dsig.xmldsig.set("three");
    array3.add(dsig);
    logger.info(array3.toString());
  }
}