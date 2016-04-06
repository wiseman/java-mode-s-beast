package com.lemondronor.modesbeast;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class BeastMessageParserTest {
  @Test
  public void loadTests() {
    Collection<TestSpec> specs = TestSpec.readFromFile("beast-binary-test-cases.yaml");
    for (TestSpec spec : specs) {
      checkTestSpec(spec);
    }
  }

  private void checkTestSpec(TestSpec spec) {
    System.err.println("Checking " + spec.comment);
    System.err.println("  packet " + spec.packet1);

    BeastMessageParser parser = new BeastMessageParser();
    ExtractedBytes e1 = null;
    List<ExtractedBytes> extracteds = getExtractedBytesForPackets(
        parser,
        spec.packet1,
        spec.packet2);
    e1 = extracteds.get(0);
    assertArrayEquals(spec.extracted1, e1.getByteArray());
    assertEquals(spec.hasParity1, e1.hasParity);
    ExtractedBytes e2 = null;
    if (extracteds.size() > 1) {
      e2 = extracteds.get(1);
    }
    if (spec.extracted2 != null) {
      assertNotNull(e2);
      assertArrayEquals(spec.extracted2, e2.getByteArray());
      assertEquals(spec.hasParity2, e2.hasParity);
    } else {
      assertNull(e2);
    }
    System.err.println("**************************************** Test passed: " + spec.comment);
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
}
