package com.lemondronor.modesbeast;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExtractedBytes {
  public byte[] bytes;
  public boolean hasParity;

  public ExtractedBytes(byte[] bytes, boolean hasParity) {
    this.bytes = bytes;
    this.hasParity = hasParity;
  }

  public byte[] getByteArray() {
    return bytes;
  }
}
