package com.lemondronor.modesbeast;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

/**
 * This is a Java implementation of the parser from Virtual Radar Server.
 */
public class BeastMessageParser {

  static final int BEAST_ESCAPE = 0x1A;
  static final byte[] mlatGeneratedMessageTimestamp = new byte[] { (byte) 0xFF, 0x00, 0x4D, 0x4C, 0x41, 0x54 };
  static final int maximumBufferSize = 10000;

  private class ExtractResult {
    int endOfPacket;
    int startOfPacket;
    int dataLength;
    int signalLevel;

    ExtractResult(int endOfPacket, int startOfPacket, int dataLength,
                  int signalLevel) {
      this.endOfPacket = endOfPacket;
      this.startOfPacket = startOfPacket;
      this.dataLength = dataLength;
      this.signalLevel = signalLevel;
    }
  }

  private byte[] readBuffer;
  private int readBufferLength;
  private byte[] payload;
  private byte[] timestampBytes;
  private boolean sawFirstPacket;
  private boolean isBinaryFormat;
  private boolean hasParity;
  private boolean hasMlatPrefix;
  private boolean isMlat;
  private byte avrMessageStartIndicator;
  private boolean streamFormatIsEstablished;
  private ByteBuffer extractedBytes;

  public BeastMessageParser() {
    this.timestampBytes = new byte[6];
  }

  /**
   * Scans a byte array for Mode S beast messages. Returns a
   * collection of extracted Mode S payloads.
   */
  public List<ExtractedBytes> parse(byte[] bytes, int offset, int bytesRead) {
    LinkedList<ExtractedBytes> result = new LinkedList<>();
    int length = readBufferLength + bytesRead;
    if (length > maximumBufferSize) {
      readBufferLength = 0;
      length = bytesRead;
    }
    if (readBuffer == null || length > readBuffer.length) {
      byte[] newReadBuffer = new byte[length];
      if (readBuffer != null) {
        System.arraycopy(readBuffer, 0, newReadBuffer, 0, readBuffer.length);
      }
      readBuffer = newReadBuffer;
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
        int signalLevel = -1;
        if (!isBinaryFormat) {
          endOfPacket = ArrayUtils.indexOf(readBuffer, (byte) ';', startOfPacket, readBufferLength);
        } else {
          ExtractResult extractResult = extractBinaryPayload(startOfPacket, dataLength);
          endOfPacket = extractResult.endOfPacket;
          startOfPacket = extractResult.startOfPacket;
          dataLength = extractResult.dataLength;
          signalLevel = extractResult.signalLevel;
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
          ExtractedBytes extractedBytes = new ExtractedBytes()
                                          .bytes(Arrays.copyOf(payload, dataLength))
                                          .hasParity(hasParity || isBinaryFormat)
                                          .signalLevel(signalLevel)
                                          .isMlat(isMlat);
          result.add(extractedBytes);
        }
        startOfPacket = findStartIndex(firstByteAfterLastValidPacket);
      }

      if (firstByteAfterLastValidPacket != ArrayUtils.INDEX_NOT_FOUND) {
        int unusedBytesCount = readBufferLength - firstByteAfterLastValidPacket;
        if (unusedBytesCount > 0) {
          if (unusedBytesCount > 1024) {
            unusedBytesCount = 0;
          } else {
            for (int si = firstByteAfterLastValidPacket, di = 0;
                 di < unusedBytesCount;
                 ++si, ++di) {
              readBuffer[di] = readBuffer[si];
            }
          }
        }
        readBufferLength = unusedBytesCount;
      }
    }
    return result;
  }

  private int extractAvrPayload(int startOfPacket, int endOfPacket, int dataLength) {
    if (hasMlatPrefix) {
      int actualStartOfPacket = startOfPacket + 12;
      if (actualStartOfPacket >= readBufferLength) {
        actualStartOfPacket = endOfPacket;
      } else {
        if (!convertAsciiHexDigits(timestampBytes, startOfPacket, actualStartOfPacket)) {
          actualStartOfPacket = endOfPacket;
        } else {
          isMlat = true;
          for (int i = 0; i < timestampBytes.length; ++i) {
            if (timestampBytes[i] != mlatGeneratedMessageTimestamp[i]) {
              isMlat = false;
              break;
            }
          }
        }
      }
      startOfPacket = actualStartOfPacket;
    }
    dataLength = endOfPacket - startOfPacket;
    dataLength = (dataLength & 1) == 1 ? -1 : dataLength / 2;
    if (dataLength > 0 && dataLength < 15) {
      if (payload == null || payload.length < dataLength) {
        payload = new byte[dataLength];
      }
      if (!convertAsciiHexDigits(payload, startOfPacket, endOfPacket)) {
        dataLength = -1;
      }
    }
    return dataLength;
  }

  private boolean convertAsciiHexDigits(byte[] buffer, int start, int end) {
    boolean result = true;
    for (int di = 0, si = start; si < end; ++di, ++si) {
      int highNibble = extractHexDigitValue(readBuffer[si]);
      int lowNibble = extractHexDigitValue(readBuffer[++si]);
      if (highNibble == 0xff || lowNibble == 0xff) {
        result = false;
        break;
      }
      if (buffer != null) {
        buffer[di] = (byte)((highNibble << 4) | lowNibble);
      }
    }
    return result;
  }

  private int extractHexDigitValue(byte by) {
    if (by >= 0x30 && by <= 0x39) {
      return by - 0x30;
    }
    if (by >= 0x41 && by <= 0x46) {
      return by - 0x37;
    }
    if (by >= 0x61 && by <= 0x66) {
      return by - 0x57;
    }
    return 0xff;
  }

  private boolean establishStreamFormat() {
    if (!streamFormatIsEstablished) {
      isBinaryFormat = ArrayUtils.indexOf(
          readBuffer, (byte) BEAST_ESCAPE, 0, readBufferLength) != ArrayUtils.INDEX_NOT_FOUND;
      if (isBinaryFormat) {
        streamFormatIsEstablished = true;
      } else if (readBufferLength > 22) {
        for (byte ch : readBuffer) {
          switch (ch) {
            case 0x2a: // '*'
              streamFormatIsEstablished = true;
              hasParity = true;
              avrMessageStartIndicator = ch;
              break;
            case 0x3a: // ':'
              streamFormatIsEstablished = true;
              avrMessageStartIndicator = ch;
              break;
            case 0x40: // '@'
              streamFormatIsEstablished = true;
              hasParity = true;
              hasMlatPrefix = true;
              avrMessageStartIndicator = ch;
              break;
            default:
          }
          if (streamFormatIsEstablished) {
            break;
          }
        }
        if (!streamFormatIsEstablished && readBufferLength > 100) {
          readBufferLength = 0;
        }
      }
    }
    return streamFormatIsEstablished;
  }


  private int findStartIndex(int start) {
    int result = ArrayUtils.INDEX_NOT_FOUND;
    if (!isBinaryFormat) {
      result = ArrayUtils.indexOf(
          readBuffer, avrMessageStartIndicator, start, readBufferLength);
      if (result != ArrayUtils.INDEX_NOT_FOUND) {
        result += 1;
      }
    } else {
      for (int i = start; i < readBufferLength; ++i) {
        byte ch = readBuffer[i];
        if (ch == BEAST_ESCAPE) {
          if (++i < readBufferLength) {
            if (readBuffer[i] != BEAST_ESCAPE) {
              result = i;
              break;
            }
          }
        }
      }
    }
    return result;
  }

  private ExtractResult extractBinaryPayload(int startOfPacket, int dataLength) {
    dataLength = 0;
    switch (readBuffer[startOfPacket++]) {
      case 0x31:
        dataLength = 4;
        break;
      case 0x32:
        dataLength = 7;
        break;
      case 0x33:
        dataLength = 14;
        break;
      default:
    }
    if (payload == null || payload.length < dataLength) {
      payload = new byte[dataLength];
    }
    int si = startOfPacket;
    int di = 0;
    int signalLevel = -1;
    isMlat = true;
    for (; si < readBufferLength && di < 7; ++si, ++di) {
      byte ch = readBuffer[si];
      if (ch == BEAST_ESCAPE && ++si > readBufferLength) {
        break;
      }
      if (di == 6) {
        // Make sure, e.g., 0xFF gets converted into 255.
        signalLevel = ch & 0xFF;
      } else {
        timestampBytes[di] = ch;
        if (isMlat && ch != mlatGeneratedMessageTimestamp[di]) {
          isMlat = false;
        }
      }
    }
    for (di = 0; si < readBufferLength && di < dataLength; ++si) {
      byte ch = readBuffer[si];
      if (ch == BEAST_ESCAPE) {
        if (++si >= readBufferLength) {
          break;
        }
        ch = readBuffer[si];
      }
      payload[di++] = ch;
    }
    int endOfPacket = di != dataLength ? ArrayUtils.INDEX_NOT_FOUND : si;
    return new ExtractResult(endOfPacket, startOfPacket, dataLength, signalLevel);
  }

  /**
   * Converts a hex string like "00FF0D" into an array of bytes.
   */
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
