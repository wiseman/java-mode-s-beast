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
    Collection<TestSpec> specs = TestSpec.readFromFile("beast-binary-test-cases.yaml", true);
    specs.addAll(TestSpec.readFromFile("beast-text-test-cases.yaml", false));
    return specs.stream()
        // Junit parameters are <?, ?> pairs.
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
    List<ExtractedBytes> expectedOutputs = spec.expectedOutputs;
    List<ExtractedBytes> outputs = getExtractedBytesForPackets(parser, spec.inputs);
    assertEquals(spec.comment, expectedOutputs.size(), outputs.size());

    for (int i = 0; i < outputs.size(); i++) {
      ExtractedBytes output = outputs.get(i);
      ExtractedBytes expectedOutput = expectedOutputs.get(i);
      assertArrayEquals(
          desc(spec.comment,
               expectedOutput.getByteArray(),
               output.getByteArray()),
          expectedOutput.getByteArray(), output.getByteArray());
      assertEquals(spec.comment, expectedOutput.hasParity, output.hasParity);
      assertEquals(spec.comment, expectedOutput.badChecksum, output.badChecksum);
      assertEquals(spec.comment, expectedOutput.signalLevel, output.signalLevel);
      assertEquals(spec.comment, expectedOutput.isMlat, output.isMlat);
    }
  }

  private List<ExtractedBytes> getExtractedBytesForPackets(
      BeastMessageParser parser, Collection<byte[]> packets) {
    List<ExtractedBytes> outputs = new LinkedList<>();
    for (byte[] packet : packets) {
      List<ExtractedBytes> extracteds = parser.parse(packet, 0, packet.length);
      outputs.addAll(extracteds);
    }
    return outputs;
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
