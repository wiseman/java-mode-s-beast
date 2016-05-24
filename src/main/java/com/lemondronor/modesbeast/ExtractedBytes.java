package com.lemondronor.modesbeast;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ExtractedBytes {
  public byte[] bytes;
  public boolean hasParity;
  public boolean badChecksum;
  public int signalLevel;
  public boolean isMlat;

  public ExtractedBytes() {
    signalLevel = -1;
  }

  public ExtractedBytes bytes(byte[] bytes) {
    this.bytes = bytes;
    return this;
  }

  public ExtractedBytes badChecksum(boolean badChecksum) {
    this.badChecksum = badChecksum;
    return this;
  }

  public ExtractedBytes hasParity(boolean hasParity) {
    this.hasParity = hasParity;
    return this;
  }

  public ExtractedBytes signalLevel(int signalLevel) {
    this.signalLevel = signalLevel;
    return this;
  }

  public ExtractedBytes isMlat(boolean isMlat) {
    this.isMlat = isMlat;
    return this;
  }

  public byte[] getByteArray() {
    return bytes;
  }
}
