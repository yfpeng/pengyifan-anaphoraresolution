package edu.nus.comp.nlp.tool.anaphoraresolution;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public abstract class Utils {

  /**
   * Parse the all (TAG word sentenceIndex charOffset)s in a string. For
   * example,
   * 
   * <pre>
   * (S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX)) ...
   * </pre>
   * 
   * Returns (PRP We sIndex 18) (VBD demonstrated, ) (IN that) (NN katX) ...
   * 
   * @param s
   * @param sentenceIndex
   * @return
   */
  public static List<TagWord> analyseTagWordPairs(String s, int sentenceIndex) {
    if (s.isEmpty()) {
      return Collections.emptyList();
    }

    int adjPointer = 0; // adjunct pointer
    String tag = null;
    String word;

    int pointer = 0; // to indicate position in the string
    List<TagWord> tags = Lists.newArrayList();
    while (pointer != -1) {
      pointer = s.indexOf('(', pointer);
      if (pointer == -1) {
        break;
      }

      adjPointer = s.indexOf(" ", pointer);
      if (adjPointer == -1) {
        break;
      }

      // testing if it's (TAG word)
      if (s.startsWith("(", adjPointer + 1)) {
        pointer = adjPointer;
        continue;
      }

      tag = s.substring(pointer + 1, adjPointer);
      pointer = s.indexOf(")", adjPointer);
      word = s.substring(adjPointer + 1, pointer);
      tags.add(new TagWord(tag, word, sentenceIndex, adjPointer + 1));
    }

    return tags;
  }
}
