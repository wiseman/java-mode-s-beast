package com.lemondronor.modesbeast;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;

public class BeastMessageParser {
  private byte[] readBuffer;
  private int readBufferLength;
  private byte[] payload;
  private boolean sawFirstPacket;
  private boolean isBinaryFormat;
  private boolean hasParity;
  private boolean hasMlatPrefix;
  private byte avrMessageStartIndicator;
  private boolean streamFormatIsEstablished;
  private ByteBuffer extractedBytes;

  public BeastMessageParser() {
  }

  public Collection<ExtractedBytes> parse(byte[] bytes, int offset, int bytesRead) {
    LinkedList<ExtractedBytes> result = new LinkedList<>();
    int length = readBufferLength + bytesRead;
    if (readBuffer == null || length > readBuffer.length) {
      byte[] newReadBuffer = new byte[length];
      if (readBuffer != null) {
        System.arraycopy(readBuffer, 0, newReadBuffer, 0, readBuffer.length);
        readBuffer = newReadBuffer;
      }
    }
    System.arraycopy(bytes, 0, readBuffer, readBufferLength, bytesRead);
    readBufferLength = length;

    if (establishStreamFormat()) {
      int startOfPacket = findStartIndex(0);
      if (!sawFirstPacket && startOfPacket == 1) {
        startOfPacket = findStartIndex(startOfPacket);
      }
      int firstByteAfterLastValidPacket = -1;
      while (startOfPacket != -1 && startOfPacket < readBufferLength) {
        int endOfPacket;
        int dataLength = 0;
        if (!isBinaryFormat) {
          endOfPacket = ArrayUtils.indexOf(readBuffer, (byte) ';', startOfPacket);
          if (endOfPacket >= (readBufferLength - startOfPacket)) {
            endOfPacket = ArrayUtils.INDEX_NOT_FOUND;
          }
        } else {
          endOfPacket = extractBinaryPayload(startOfPacket, dataLength);
        }
        if (endOfPacket == -1) {
          break;
        }
        sawFirstPacket = true;
        firstByteAfterLastValidPacket = isBinaryFormat ? endOfPacket : endOfPacket + 1;

        if (!isBinaryFormat) {
          dataLength = extractAvrPayload(startOfPacket, endOfPacket, dataLength);
        }

        if (dataLength == 7 || dataLength == 14) {
          ExtractedBytes extractedBytes = new ExtractedBytes(
              ByteBuffer.wrap(payload, 0, dataLength),
              hasParity || isBinaryFormat);
          result.add(extractedBytes);
        }

      }
      return result;
    }
    return null;
  }

  private int extractAvrPayload(int startOfPacket, int endOfPacket, int dataLength) {
    return 0;
  }

  private boolean establishStreamFormat() {
    return false;
  }

  private int findStartIndex(int start) {
    return 0;
  }

  private int extractBinaryPayload(int startOfPacket, int dataLength) {
    return 0;
  }

  public static byte[] hexStringToByteArray(String string) {
    int len = string.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                            + Character.digit(string.charAt(i + 1), 16));
    }
    return data;
  }
}
