package com.lemondronor.modesbeast;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ArrayUtilsTest {
  @Test
  public void indexOfWithStartIndex() {
    byte[] bytes = {0, 1, 2, 3, 4, 9, 6, 7};
    assertEquals(5, ArrayUtils.indexOf(bytes, (byte) 9, 0));
    assertEquals(5, ArrayUtils.indexOf(bytes, (byte) 9, -5));
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 9, 100));
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 10, 0));
  }

  @Test
  public void indexOfWithStopIndex() {
    byte[] bytes = {0, 1, 2, 3, 4, 9, 6, 7};
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 9, 0, 0));
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 9, -5, 0));
    assertEquals(5, ArrayUtils.indexOf(bytes, (byte) 9, -5, 10));
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 9, 0, 0));
    assertEquals(ArrayUtils.INDEX_NOT_FOUND, ArrayUtils.indexOf(bytes, (byte) 9, 0, 5));
    assertEquals(5, ArrayUtils.indexOf(bytes, (byte) 9, 0, 6));
  }
}
