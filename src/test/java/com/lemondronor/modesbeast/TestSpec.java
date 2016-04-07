package com.lemondronor.modesbeast;

import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class TestSpec {
  public byte[] packet1;
  public byte[] packet2;
  public byte[] extracted1;
  public Boolean hasParity1;
  public Boolean badChecksum1;
  public byte[] extracted2;
  public Boolean hasParity2;
  public Boolean badChecksum2;
  public String comment;

  /**
   * Reads a test suite from a YAML file.
   */
  public static Collection<TestSpec> readFromFile(String path) {
    LinkedList<TestSpec> specs = new LinkedList<>();
    Yaml yaml = new Yaml();
    Collection<Map<String,Object>> descriptions = (Collection) yaml.load(
        TestSpec.class.getClassLoader().getResourceAsStream(path));
    System.err.println(descriptions.getClass());
    for (Map<String,Object> desc : descriptions) {
      TestSpec spec = new TestSpec();
      spec.packet1 = parsePacket((String)desc.get("Packet1"));
      spec.packet2 = parsePacket((String)desc.get("Packet2"));
      spec.extracted1 = parsePacket((String)desc.get("Extracted1"));
      spec.hasParity1 = (Boolean)desc.get("HasParity1");
      spec.badChecksum1 = (Boolean)desc.get("BadChecksum1");
      spec.extracted2 = parsePacket((String)desc.get("Extracted2"));
      spec.hasParity2 = (Boolean)desc.get("HasParity2");
      spec.badChecksum2 = (Boolean)desc.get("BadChecksum2");
      spec.comment = (String) desc.get("Comment");
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
}
