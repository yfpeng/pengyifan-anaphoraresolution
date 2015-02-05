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

import com.google.common.collect.Lists;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

class NP {

  public final static int PLEO = 2; // pleonastic pronoun;
  public final static int PRON = 3; // other pronoun;
  public final static int INDEF = 4; // indefinite NP;
  // type of the Unit;
  private int type = NP.INDEF;
  private boolean existential = false;
  private boolean subject = false;
  private boolean directObj = false;
  private boolean indirectObj = false;
  private boolean isHead = false;
  private boolean isInADVP = false;
  // Indicates whether this NP is part of a "NNX (NNX)+" combination
  // The probability of such a NP being a good antecedent of an anaphora
  // plumbs.
  private boolean hasNNXsibling = false;
  // index of the sentence where UNIT is localized. 0 based
  private int sentenceIndex;
  // distance between the beginning of the sentence and the first word in UNIT;
  private int wordIndex;

  private DefaultMutableTreeNode nodeRepresent = null;

  // containing instances of TagWord
  List<TagWord> tagWords = Lists.newArrayList();

  NP(int sIdx, int offset) {
    this.sentenceIndex = sIdx;
    this.wordIndex = offset;
  }

  private NP(int sentenceIndex, int wordIndex, String annotatedNP) {
    this.sentenceIndex = sentenceIndex;
    this.wordIndex = wordIndex;
    tagWords.addAll(Utils.parseTagWordPairs(annotatedNP, sentenceIndex));
    setSlots();
  }
  
  NP(TagWord tagWord) {
    this.sentenceIndex = tagWord.getSentenceIndex();
    this.wordIndex = tagWord.getWordIndex();
    tagWords = Lists.newArrayList(tagWord);
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
    switch (tagWords.size()) {
    case 1:
      TagWord aTagWord = tagWords.get(0);
      String tag = aTagWord.getTag();
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
    return this.sentenceIndex;
  }

  public int getOffset() {
    return this.wordIndex;
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
    if (tagWords.size() != 1) {
      return false;
    } else if (tagWords.get(0).getTag().startsWith("PRP")) {
      return true;
    } else {
      return false;
    }
  }

  public boolean isReflexive() {
    return isPRP() && tagWords.get(0).getText().indexOf("sel") > 0;
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
  void mergeSalience(NP palNP) {
    subject |= palNP.subject;
    existential |= palNP.existential;
    directObj |= palNP.directObj;
    indirectObj |= palNP.indirectObj;
    isHead |= palNP.isHead;
    isInADVP |= palNP.isInADVP;
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
    return sentenceIndex + "," + wordIndex + "," + type + ","
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
}
