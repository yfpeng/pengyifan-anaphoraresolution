package edu.nus.comp.nlp.tool.anaphoraresolution;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class UnitlsTest {

  private static final int SEN_INDEX = -1;
  private static final String LINE = "(S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX))";
  private static final TagWord W1 = new TagWord("PRP", "We", SEN_INDEX, 19);
  private static final TagWord W2 = new TagWord("VBD", "demonstrated", SEN_INDEX, 33);
  private static final TagWord W3 = new TagWord("IN", "that", SEN_INDEX, 57);
  private static final TagWord W4 = new TagWord("NN", "katX", SEN_INDEX, 74);

  @Test
  public void test_analyseTagWordPairs() {
    List<TagWord> list = Utils.parseTagWordPairs(LINE, SEN_INDEX);
    assertEquals(4, list.size());
    assertEquals(W1, list.get(0));
    assertEquals(W2, list.get(1));
    assertEquals(W3, list.get(2));
    assertEquals(W4, list.get(3));
  }

}
