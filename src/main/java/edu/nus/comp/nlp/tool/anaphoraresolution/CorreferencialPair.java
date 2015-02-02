package edu.nus.comp.nlp.tool.anaphoraresolution;
/**
 * 
 * @author "Yifan Peng"
 *
 */
public class CorreferencialPair {

  TagWord referee;
  TagWord referer;

  public CorreferencialPair(TagWord tw0, TagWord tw1) {
    referee = tw0;
    referer = tw1;
  }

  public TagWord getReferer() {
    return referer;
  }

  public TagWord getReferee() {
    return referee;
  }

  public CorreferencialPair(String refereeRecord, String refererRecord) {
    // (sentenceIdx,offset) word
    if (refereeRecord.trim().equals("NULL")) {
      referee = null;
    }
    else {
      referee = new TagWord(refereeRecord.trim());
    }
    referer = new TagWord(refererRecord.trim());
  }

  public String toString() {
    return Util.processResult(referee, referer);
  }
}
