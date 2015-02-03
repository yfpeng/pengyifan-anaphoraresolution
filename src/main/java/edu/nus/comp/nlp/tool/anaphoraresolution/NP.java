/*
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper: An Algorithm for Pronominal Anaphora
 * Resolution. Computational Linguistics, 20(4), pp. 535-561. Copyright (C)
 * 2005,2006 Long Qiu This program is free software; you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package edu.nus.comp.nlp.tool.anaphoraresolution;

import java.util.*;

import javax.swing.tree.*;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

class NP {

  public final static int PLEO = 2; // pleonastic pronoun;
  public final static int PRON = 3; // other pronoun;
  public final static int INDEF = 4; // indefinite NP;
  /* number */
  public final static int SINGLE = 1;
  public final static int PLURAL = 2;
  /* gender */
  public final static int NULL = 0;
  public final static int MALE = 1;
  public final static int FEMALE = 2;

  // type of the Unit;
  private int type = NP.INDEF;
  // number and gender;
  private int number = NP.SINGLE;
  private int gender = NP.NULL;
  private boolean existential = false;
  private boolean subject = false;
  private boolean directObj = false;
  private boolean indirectObj = false;
  private boolean isHead = false;
  private boolean isInADVP = false;
  // Indicates whether this NP is part of a "NNX (NNX)+" combination
  // The probability of such a NP being a good antecedent of an anaphor plumbs.
  private boolean hasNNXsibling = false;
  // index of the sentence where UNIT is localized. 0 based
  private int sentIdx;
  // distance between the beinning of the sentence and the first word in UNIT;
  private int offset;

  private DefaultMutableTreeNode nodeRepresent = null;

  Vector<TagWord> tagWord = new Vector<TagWord>(); // containing instances of
                                                   // TagWord

  NP(int sIdx, int offset) {
    this.sentIdx = sIdx;
    this.offset = offset;
  }

  NP(int sIdx, int offset, String annotatedNP) {
    this.sentIdx = sIdx;
    this.offset = offset;
    AnaphoraResolver.analyseTagWordPairs(annotatedNP, tagWord, sIdx);
    setSlots();
  }

  public void setSubject(boolean b) {
    this.subject = b;
  }

  public boolean isSubject() {
    return subject;
  }

  public void setExistential(boolean b) {
    this.existential = b;
  }

  public boolean isExistential() {
    return existential;
  }

  /**
   * setting the type
   * */
  public void setType(int ty) {
    this.type = ty;
  }

  public void setDirectObj(boolean b) {
    this.directObj = b;
    this.indirectObj = !b;
  }

  public boolean isDirectObj() {
    return directObj;
  }

  public void setIndirectObj(boolean b) {
    this.directObj = !b;
    this.indirectObj = b;
  }

  public boolean isIndirectObj() {
    return indirectObj;
  }

  public void setHasNNXsibling(boolean b) {
    hasNNXsibling = b;
  }

  public void setHead(boolean b) {
    this.isHead = b;
    /*
     * if(!b){ System.out.println(this.toDisplay()); }else{
     * System.out.println("\t"+this.toDisplay()); }
     */
  }

  public boolean isHead() {
    return this.isHead;
  }

  private void setSlots() {
    setType();
  }

  private void setType() {
    switch (tagWord.size()) {
    case 1:
      TagWord aTagWord = (TagWord) tagWord.elementAt(0);
      String tag = aTagWord.tag;
      if (tag.startsWith("PRP")) {
        this.type = NP.PRON;
      }
      break;
    default:
      break;
    }
  }

  public int getType() {
    return this.type;
  }

  public int getSentenceIdx() {
    return this.sentIdx;
  }

  public int getOffset() {
    return this.offset;
  }

  boolean contains(NP np) {
    return np.getNodeRepresent().isNodeAncestor(this.getNodeRepresent());
    /*
     * String s = this.tagWord.toString(); String ss = np.tagWord.toString();
     * int tt = s.indexOf(ss); if
     * (this.tagWord.toString().indexOf(np.tagWord.toString().substring(0,
     * ss.length() - 1)) > -1) { if (this.tagWord.toString().length() ==
     * np.toString().length()) { return false; } return true; } else { return
     * false; }
     */
  }

  public boolean isPRP() {
    if (this.tagWord.size() != 1) {
      return false;
    }
    else if (((TagWord) tagWord.elementAt(0)).tag.startsWith("PRP")) {
      return true;
    }
    return false;
  }

  public boolean isReflexive() {
    if (this.isPRP()) {
      if (((TagWord) tagWord.elementAt(0)).getContent().indexOf("sel") > 0) {
        return true;
      }
    }
    return false;
  }

  public boolean isHuman() {
    return true;
  }

  public boolean isIt() {
    TagWord tw = (TagWord) tagWord.elementAt(0);
    if (!tw.getTag().startsWith("PRP")) {// PRP($)
      return false;
    }
    return tw.getContent().toLowerCase().startsWith("it");// it its itself
  }

  /**
   * @return true if there is a "CC and" in children
   */
  boolean hasAnd() {
    @SuppressWarnings("rawtypes")
    Enumeration enumer = getNodeRepresent().children();
    while (enumer.hasMoreElements()) {
      DefaultMutableTreeNode aChild = (DefaultMutableTreeNode) enumer
          .nextElement();
      if (((TagWord) aChild.getUserObject()).getTag().equals("CC")) {
        return true;
      }
    }
    return false;
  }

  public void setNodeRepresent(DefaultMutableTreeNode t) {
    // as it's stored in the tree
    nodeRepresent = t;
  }

  public DefaultMutableTreeNode getNodeRepresent() {
    return this.nodeRepresent;
  }

  int getSalience(NP otherNP) {
    return this.getSentenceIdx() == otherNP.getSentenceIdx() ?
        getFixedSalience() + 100
        : getFixedSalience();
  }

  /**
   * merge all salience factors true for palNP, salience factors for palNP
   * remain unchanged where palNP is in the same co-reference chain as this NP.
   * 
   * @param palNP
   */
  public void mergeSalience(NP palNP) {
    subject = subject || palNP.subject;
    existential = existential || palNP.existential;
    directObj = directObj || palNP.directObj;
    indirectObj = indirectObj || palNP.indirectObj;
    isHead = isHead || palNP.isHead;
    isInADVP = isInADVP || palNP.isInADVP;
  }

  private int getFixedSalience() {
    int fSalience = 0;
    // 6 of the 7 salience factors are considered here: (sentence recency is
    // relevant and considered in getSalience(NP otherNP))
    // Subject emphasis
    // Existential emphasis
    // Accusative emphasis (object)
    // Indirect object emphasis
    // Head noun emphasis
    // Non-adverbial emphasis
    if (subject) {
      fSalience += 80;
    }

    if (existential) {
      fSalience += 70;
    }

    if (directObj) {
      fSalience += 50;
    }

    if (indirectObj) {
      fSalience += 40;
    }

    if (isHead) {
      fSalience += 0;
    }

    if (!isInADVP) {
      fSalience += 50;
    }

    if (hasNNXsibling) {
      fSalience -= 0; // Right. Reduce the salience score.
    }

    return fSalience;
  }

  public void setIsInADVP(boolean b) {
    isInADVP = b;
  }

  public String toString() {
    return sentIdx + "," + offset + "," + type + "," + number + "," + gender
        // + "," + toDisplay()
        + ",EX "
        + this.existential
        + ",SUB "
        + this.subject
        + ",DOBJ "
        + this.directObj
        + ",INDOBJ "
        + this.indirectObj
        + ",isHEAD "
        + this.isHead
        + ",SAL <"
        + getFixedSalience()
        + ">";
  }

  public String toDisplay() {
    String text = new String();
    text += ((TagWord) tagWord.elementAt(0)).sIdx + " ";
    for (int i = 0; i < tagWord.size(); i++) {
      text += ((TagWord) tagWord.elementAt(i)).word + " ";
    }
    return text;
  }
}
