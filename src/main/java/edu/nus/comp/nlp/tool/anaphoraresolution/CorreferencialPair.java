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

  public String toString() {
    String refereeStr = null;
    String anaphorStr = null;
    if (referee == null) {
      refereeStr = "NULL";
    } else {
      if (System.getProperty("referenceChain").equals("false")) {
        // true/undefined by default
        refereeStr = referee.toStringBrief();
      } else {
        // bind to the earliest NP
        refereeStr = referee.getAntecedent().toStringBrief();
      }
      // update salience factors for the detected coreferential pair
      referee.mergeSalience(referer);
      referer.mergeSalience(referee);
    }
    anaphorStr = ((TagWord) referer).toStringBrief();
    return refereeStr + " <-- " + anaphorStr;
  }
}
