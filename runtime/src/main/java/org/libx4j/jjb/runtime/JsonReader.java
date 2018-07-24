package org.libx4j.jjb.runtime;

import java.io.IOException;
import java.io.Reader;

import org.lib4j.io.input.ReplayReader;

public class JsonReader extends ReplayReader {
  private String full;

  public JsonReader(final Reader in) {
    super(in);
  }

  public JsonReader(final Reader in, final int size) {
    super(in, size);
  }

  public int getPosition() {
    return buffer.size();
  }

  public String readFully() throws IOException {
    if (full != null)
      return full;

    for (int i = 0; i != -1; i = read());

    return full = buffer.toString();
  }
}