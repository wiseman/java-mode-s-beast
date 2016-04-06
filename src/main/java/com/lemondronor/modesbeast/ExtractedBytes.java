package com.lemondronor.modesbeast;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExtractedBytes {
  public ByteBuffer bytes;
  public boolean hasParity;

  public ExtractedBytes(ByteBuffer bytes, boolean hasParity) {
    this.bytes = bytes;
    this.hasParity = hasParity;
  }

  public byte[] getByteArray() {
    return Arrays.copyOf(bytes.array(), bytes.position());
  }
}
