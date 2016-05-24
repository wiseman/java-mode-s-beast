package com.lemondronor.modesbeast;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RunWith(Parameterized.class)
public class BeastMessageParserTest {

  private TestSpec testSpec;

  /**
   * Load test cases from the YAML file.
   */
  @Parameterized.Parameters
  public static Collection<Object[]> loadTests() {
    Collection<TestSpec> specs = TestSpec.readFromFile("beast-binary-test-cases.yaml");
    return specs.stream()
        .map(s -> new Object[] { s, null })
        .collect(Collectors.toList());
  }

  public BeastMessageParserTest(TestSpec spec, Object ignore) {
    testSpec = spec;
  }

  @Test
  public void checkTestSpec() {
    TestSpec spec = testSpec;
    System.err.println("Test: " + spec.comment);
    BeastMessageParser parser = new BeastMessageParser();
    ExtractedBytes e1 = null;
    List<ExtractedBytes> extracteds = getExtractedBytesForPackets(
        parser,
        spec.packet1,
        spec.packet2);
    if (extracteds.size() > 0) {
      e1 = extracteds.get(0);
    }
    if (spec.extracted1 != null) {
      assertNotNull(spec.comment, e1);
      assertArrayEquals(
          desc(spec.comment, spec.extracted1, e1.getByteArray()),
          spec.extracted1, e1.getByteArray());
      assertEquals(spec.comment, spec.hasParity1, e1.hasParity);
      assertEquals(spec.comment, spec.badChecksum1, e1.badChecksum);
      if (spec.signalLevel1 != null) {
          assertEquals(spec.comment, spec.signalLevel1.intValue(), e1.signalLevel);
      } else {
          assertEquals(spec.comment, -1, e1.signalLevel);
      }
      assertEquals(spec.comment, spec.isMlat1, e1.isMlat);
    }
    ExtractedBytes e2 = null;
    if (extracteds.size() > 1) {
      e2 = extracteds.get(1);
    }
    if (spec.extracted2 != null) {
      assertNotNull(e2);
      assertArrayEquals(
          desc(spec.comment, spec.extracted2, e2.getByteArray()),
          spec.extracted2, e2.getByteArray());
      assertEquals(spec.hasParity2, e2.hasParity);
      assertEquals(spec.badChecksum2, e2.badChecksum);
      if (spec.signalLevel2 != null) {
          assertEquals(spec.signalLevel2.intValue(), e2.signalLevel);
      } else {
          assertEquals(-1, e2.signalLevel);
      }
      assertEquals(spec.isMlat2, e2.isMlat);
    } else {
      assertNull(e2);
    }
  }

  private List<ExtractedBytes> getExtractedBytesForPackets(
      BeastMessageParser parser, byte[] packet1, byte[] packet2) {
    Collection<ExtractedBytes> extracteds1 = parser.parse(packet1, 0, packet1.length);
    Collection<ExtractedBytes> extracteds2 = null;
    if (packet2 != null) {
      extracteds2 = parser.parse(packet2, 0, packet2.length);
    } else {
      extracteds2 = new LinkedList<ExtractedBytes>();
    }
    Stream<ExtractedBytes> extracteds = Stream.concat(
        extracteds1.stream(),
        extracteds2.stream());
    return extracteds.collect(Collectors.toList());
  }

  private String desc(String comment, byte[] bytes1, byte[] bytes2) {
    return ("Test: '" + comment + "', "
            + "expected: [" + arrayToString(bytes1) + "] "
            + "actual: [" + arrayToString(bytes2) + "]");
  }

  private String arrayToString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (int b : bytes) {
      if (!first) {
        sb.append(" ");
      } else {
        first = false;
      }
      sb.append(String.format("%02X", b & 0xff));
    }
    return sb.toString();
  }
}
