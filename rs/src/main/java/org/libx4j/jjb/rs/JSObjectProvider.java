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

package org.libx4j.jjb.rs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.libx4j.jjb.runtime.DecodeException;
import org.libx4j.jjb.runtime.JSObject;

@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class JSObjectProvider implements MessageBodyReader<JSObject>, MessageBodyWriter<JSObject> {
  @Override
  public boolean isReadable(final Class<?> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return JSObject.class.isAssignableFrom(rawType);
  }

  @Override
  public JSObject readFrom(final Class<JSObject> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String,String> httpHeaders, final InputStream entityStream) throws IOException {
    try {
      return JSObject.parse(rawType, new InputStreamReader(entityStream));
    }
    catch (final DecodeException e) {
      throw new BadRequestException(e);
    }
  }

  @Override
  public long getSize(final JSObject t, final Class<?> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return t.toString().length();
  }

  @Override
  public boolean isWriteable(final Class<?> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return JSObject.class.isAssignableFrom(rawType);
  }

  @Override
  public void writeTo(final JSObject t, final Class<?> rawType, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String,Object> httpHeaders, final OutputStream entityStream) throws IOException {
    final byte[] bytes = t.toString().getBytes();
    entityStream.write(bytes);
    httpHeaders.putSingle(HttpHeaders.CONTENT_LENGTH, bytes.length);
  }
}