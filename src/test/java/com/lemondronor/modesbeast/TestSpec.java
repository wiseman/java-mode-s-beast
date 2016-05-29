package com.lemondronor.modesbeast;

import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestSpec {
  public String comment;
  public List<byte[]> inputs;
  public List<ExtractedBytes> expectedOutputs;

  /**
   * Reads a test suite from a YAML file.
   */
  public static Collection<TestSpec> readFromFile(String path, boolean isBinary) {
    LinkedList<TestSpec> specs = new LinkedList<>();
    Yaml yaml = new Yaml();
    Collection<Map<String,Object>> descriptions = (Collection) yaml.load(
        TestSpec.class.getClassLoader().getResourceAsStream(path));
    for (Map<String,Object> desc : descriptions) {
      TestSpec spec = new TestSpec();
      spec.comment = (String) desc.get("Comment");
      LinkedList<byte[]> inputs = new LinkedList<byte[]>();
      boolean isFirstPacket = true;
      for (int i = 1; i <= 3; i++) {
        String inputSpec = (String)desc.get("Packet" + i);
        if (inputSpec != null) {
          if (isBinary) {
            inputs.add(parsePacket(inputSpec));
          } else {
            byte[] bytes = inputSpec.getBytes();
            if (isFirstPacket) {
              bytes = prependPacket(bytes);
              isFirstPacket = false;
            }
            inputs.add(bytes);
          }
        }
      }
      spec.inputs = inputs;
      LinkedList<ExtractedBytes> expectedOutputs = new LinkedList<ExtractedBytes>();
      for (int i = 1; i <= 3; i++) {
        ExtractedBytes expectedOutput = new ExtractedBytes();
        String extracted = (String)desc.get("Extracted" + i);
        if (extracted == null) {
          break;
        }
        expectedOutput.bytes(parsePacket(extracted));
        expectedOutput.hasParity((Boolean)desc.get("HasParity" + i));
        expectedOutput.badChecksum((Boolean)desc.get("BadChecksum" + i));
        Integer signalLevel = (Integer)desc.get("SignalLevel" + i);
        if (signalLevel != null) {
          expectedOutput.signalLevel(signalLevel);
        } else {
          expectedOutput.signalLevel(-1);
        }
        expectedOutput.isMlat((Boolean)desc.get("IsMlat" + i));
        expectedOutputs.add(expectedOutput);
      }
      spec.expectedOutputs = expectedOutputs;
      specs.add(spec);
    }
    return specs;
  }

  private static byte[] parsePacket(String text) {
    if (text == null) {
      return null;
    } else {
      return BeastMessageParser.hexStringToByteArray(text.replace(" ", ""));
    }
  }

  private static byte[] prependPacket(byte[] bytes) {
    byte[] newBytes = new byte[bytes.length + 25];
    for (int i = 0; i < 25; i++) {
      newBytes[i] = 0;
    }
    for (int i = 0; i < bytes.length; i++) {
      newBytes[i + 25] = bytes[i];
    }
    return newBytes;
  }
}
