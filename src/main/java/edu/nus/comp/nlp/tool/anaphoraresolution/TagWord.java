/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005,2011 Long Qiu
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package edu.nus.comp.nlp.tool.anaphoraresolution;

import java.util.List;
import java.util.Objects;

import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.collect.Lists;

/**
 * @author Qiu Long
 * @version 1.0
 * @history Feb 08, 2011 To getGender() given a person name, first check
 *          against the 300 most common US male names (by the 1990 census). If
 *          no match is found, then move on to a list of known female names.
 *          Previously, a more exhaustive male name list is first checked and
 *          regardless of the result the above mentioned female name list is
 *          checked. In this case, names such as John are potentially taken as
 *          female as they do appear in the female name list. This is not
 *          absolutely a mistake but in most of the cases it tend to be.
 * @author "Yifan Peng"
 */

public class TagWord {

  /***
   * s: (Tag content), where content could be also a combinedStr
   */
  public static TagWord parseTagWord(String s, int sentenceIndex,
      int wordIndex) {
    String tag = null;
    String word = null;
    try {
      tag = s.substring(s.indexOf("(") + 1, s.indexOf(" "));
      word = s.substring(s.indexOf(" ") + 1, s.lastIndexOf(")"));
    } catch (Exception ex) {
      s = "(-LRB- -LRB-)";// dummy element
      tag = s.substring(s.indexOf("(") + 1, s.indexOf(" "));
      word = s.substring(s.indexOf(" ") + 1, s.lastIndexOf(")"));
    }
    return new TagWord(tag, word, sentenceIndex, wordIndex);
  }
  private int sentenceIndex; // indicates sentence
  private int wordIndex;
  private Number number = Number.UNCLEAR;
  private Gender gender = Gender.UNCLEAR;
  private Human human = Human.UNCLEAR;
  private People people = People.UNCLEAR;

  private boolean pleonastic = false; // represents a pleonastic pronoun
  private String tag;
  private String text;
  private boolean isHeadNP = false;
  private boolean hasNPAncestor = false;
                                              private DefaultMutableTreeNode head = null; // reference to the head for this
                                                      // NP
  private DefaultMutableTreeNode argumentHead = null; // the head as this NP is
                                                      // augument for
  private DefaultMutableTreeNode argumentHost = null; // the other np as
  // augument for the same
                                                      // head
  private DefaultMutableTreeNode adjunctHost = null; // the unit as adjunct for
  private DefaultMutableTreeNode NPDomainHost = null;
  private DefaultMutableTreeNode determiner = null;
  private DefaultMutableTreeNode determinee = null;
  private List<DefaultMutableTreeNode> containHost = Lists.newArrayList();
  private NP np = null;

  private TagWord antecedent = null;

  // the dynamically updated salience value
  int tmpSalience = 0;

  public TagWord(String tag, String text, int sentenceIndex, int wordIndex) {
    this.tag = tag;
    this.text = text;
    this.sentenceIndex = sentenceIndex;
    this.wordIndex = wordIndex;
  }

  /**
   * amplify sentence index difference by multiply 100
   */
  public int distanceInText(TagWord tw) {
    return Math.abs(this.getSentenceIndex() - tw.getSentenceIndex()) * 100
        + Math.abs(this.getWordIndex() - tw.getWordIndex());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof TagWord)) {
      return false;
    }
    TagWord rhs = (TagWord) obj;
    return Objects.equals(text, rhs.text)
        && Objects.equals(tag, rhs.tag)
        && Objects.equals(sentenceIndex, rhs.sentenceIndex)
        && Objects.equals(wordIndex, rhs.wordIndex)
        && Objects.equals(number, rhs.number)
        && Objects.equals(gender, rhs.gender)
        && Objects.equals(human, rhs.human)
        && Objects.equals(people, rhs.people)
        && Objects.equals(pleonastic, rhs.pleonastic)
        && Objects.equals(isHeadNP, rhs.isHeadNP)
        && Objects.equals(hasNPAncestor, rhs.hasNPAncestor)
        && Objects.equals(head, rhs.head)
        && Objects.equals(np, rhs.np)
        && Objects.equals(antecedent, rhs.antecedent)
        ;
  }

  public DefaultMutableTreeNode getAdjunctHost() {
    return this.adjunctHost;
  }

  /**
   *
   * @return the anaphoric antecedent of the TagWord, if there is one. Itself
   *         is returned otherwise.
   */
  public TagWord getAntecedent() {
    if (antecedent == null) {
      return this;
    } else {
      return this.antecedent.getAntecedent();
    }
  }

  public DefaultMutableTreeNode getArgumentHead() {
    return this.argumentHead;
  }

  public DefaultMutableTreeNode getArgumentHost() {
    return this.argumentHost;
  }

  public java.util.List<DefaultMutableTreeNode> getContainHost() {
    return this.containHost;
  }

  public DefaultMutableTreeNode getDeterminee() {
    return determinee;
  }

  public DefaultMutableTreeNode getDeterminer() {
    return determiner;
  }

  /**
   * @return 0 for Male, 2 for Female and 1 for unclear
   */
  public Gender getGender() {
    if (gender != Gender.UNCLEAR) {
      return gender;
    }
    String h;
    if (head == null) {
      h = text; // for prp
    } else {
      TagWord tw = (TagWord) head.getUserObject();
      h = tw.getText();
    }

    if (HumanList.isMale(h)) {
      this.gender = Gender.MALE;
    } else if (HumanList.isFemale(h)) {
      this.gender = Gender.FEMALE;
    }
    return gender;
  }

  public DefaultMutableTreeNode getHead() {
    return this.head;
  }

  /**
   *
   * @return 0 for human, 2 for none-human and 1 for unclear
   */
  public Human getHuman() {
    if (human != Human.UNCLEAR) {
      return human;
    }

    if (gender != Gender.UNCLEAR) {
      human = Human.HUMAN;
      return human;
    }

    // check the content of this NP as the first attempt
    String h = getText();
    if (HumanList.isHuman(h)) {
      human = Human.HUMAN;
      return human;
    } else if (HumanList.isNotHuman(h)) {
      human = Human.NON_HUMAN;
      return human;
    }

    if (head == null) {
      return human;
    }

    // If above fails, check the head of this NP
    h = ((TagWord) head.getUserObject()).getText();
    if (HumanList.isHuman(h)) {
      human = Human.HUMAN;
      return human;
    } else if (HumanList.isNotHuman(h)) {
      human = Human.NON_HUMAN;
      return human;
    }

    return human;
  }

  public NP getNP() {
    return this.np;
  }

  public DefaultMutableTreeNode getNPDomainHost() {
    return this.NPDomainHost;
  }

  public Number getNumber() {
    if (number != Number.UNCLEAR) {
      return number;
    }
    if (np.tagWords.size() == 1) {
      String tag = np.tagWords.get(0).getTag();
      if (tag.endsWith("S")) { // NNS, NPS
        number = Number.PLURAL;
      } else if (HumanList.isPlural(getText())) {
        number = Number.PLURAL;
      } else {
        number = Number.SINGLE;
      }
    } else if (getNP().hasAnd()) {
      number = Number.PLURAL;
    } else if (this.head != null) {
      number = ((TagWord) head.getUserObject()).getNumber();
    }
    return this.number;
  }

  public People getPeople() {
    if (people != People.UNCLEAR) {
      return people;
    }
    if (this.getText().toLowerCase().matches("we|us")) {
      people = People.FIRST;
    } else if (this.getText().toLowerCase().matches("you")) {
      people = People.SECOND;
    } else {
      people = People.THIRD; // default
    }
    return people;
  }

  public People getPronounPeople() {
    if (people != People.UNCLEAR) {
      return people;
    }
    String h;
    if (head == null) {
      h = text; // for prp,
    } else {
      h = ((TagWord) head.getUserObject()).getText();
    }

    if (HumanList.isThirdPerson(h)) {
      people = People.THIRD;
    } else if (HumanList.isSecondPerson(h)) {
      people = People.SECOND;
    } else if (HumanList.isFirstPerson(h)) {
      people = People.FIRST;
    } else {
      people = People.UNCLEAR;
    }
    return people;
  }

  /**
   * @param npAlien The np that salience weight of this TagWord is considered
   *          for.
   */
  public int getSalience(NP npAlien) {

    int sal = 0;
    NP np = this.getNP();
    if ((np != null) && (npAlien != null)) {
      sal = np.getSalience(npAlien);
    }
    // dampen the salience as distance increases
    sal = sal
        / (Math.abs(this.getSentenceIndex() - npAlien.getSentenceIdx()) + 1);

    // penalize cataphora (if this appears after npAlien)
    if ((this.getSentenceIndex() == npAlien.getSentenceIdx()
        && this.getNP().getOffset() > npAlien.getOffset())
        || this.getSentenceIndex() > npAlien.getSentenceIdx()) {
      sal = sal / 4; // reduce the weight substantially
    }
    return sal;
  }

  public int getSalience(TagWord tw) {
    return getSalience(tw.getNP());
  }

  public int getSentenceIndex() {
    return sentenceIndex;
  }

  public String getTag() {
    return this.tag;
  }

  public String getText() {
    return text;
  }

  public int getTmpSalience() {
    return this.tmpSalience;
  }

  public String getWord() {
    return this.text;
  }

  /**
   * Returns index of the word as a whole in the sentence
   * 
   * @return index of the word as a whole in the sentence
   */
  public int getWordIndex() {
    return wordIndex;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        text,
        tag,
        sentenceIndex,
        wordIndex,
        number,
        gender,
        human,
        people,
        pleonastic,
        isHeadNP,
        hasNPAncestor,
        head);
  }

  public boolean hasNPAncestor() {
    return hasNPAncestor;
  }

  /**
   * @return true if thsi NP is not contained in another NP
   */
  public boolean isHeadNP() {
    if (argumentHead != null) {
      return false;
    } else if (adjunctHost != null) {
      return false;
    } else if (hasNPAncestor) {
      return false;
    }
    isHeadNP = true;
    return isHeadNP;
  }

  public boolean isPleonastic() {
    return this.pleonastic;
  }

  public boolean isPRP() {
    return this.getNP().isPRP();
  }

  /**
   * merge all salience factors true for tw, salience factors for tw remain
   * unchanged
   * 
   * @param tw
   */
  public void mergeSalience(TagWord tw) {
    // In theory: merge salience factors for members in a equvalent class
    // (coreferencial chain)
    // In fact: accumulate salience factors in the chain, a member in the chain
    // has all the factors processed by the leading members

    NP np = this.getNP();
    NP npGuest = tw.getNP();

    if (np != null && npGuest != null) {
      np.mergeSalience(npGuest);
    }
  }

  public void setAdjunctHost(DefaultMutableTreeNode n) {
    this.adjunctHost = n;
  }

  public void setAntecedent(TagWord ant) {
    if (ant.getAntecedent() == this) {
      return;
    }
    antecedent = ant;
  }

  public void setArgumentHead(DefaultMutableTreeNode n) {
    this.argumentHead = n;
  }

  /**
   * @param argumentHost: the NP in the same argument domain
   */
  public void setArgumentHost(DefaultMutableTreeNode n) {
    this.argumentHost = n;
  }

  public void setContainHost(DefaultMutableTreeNode n) {
    this.containHost.add(n);
  }

  public void setContainHost(List<DefaultMutableTreeNode> n) {
    this.containHost.addAll(n);
  }

  public void setDeterminee(DefaultMutableTreeNode n) {
    determinee = n;
  }

  public void setDeterminer(DefaultMutableTreeNode n) {
    determiner = n;
  }

  public void setHasNPAncestor(boolean b) {
    hasNPAncestor = b;
  }

  public void setHead(DefaultMutableTreeNode n) {
    if (this.number != Number.UNCLEAR) {
      // number should be set afterword
      System.err.println("Number shouldn't be set before setHead.");
    }
    this.head = n;
  }

  public void setNP(NP np) {
    this.np = np;
  }

  public void setNPDomainHost(DefaultMutableTreeNode n) {
    this.NPDomainHost = n;
  }

  public void setNumber(Number number) {
    this.number = number;
  }

  public void setPeople(People people) {
    this.people = people;
  }
  
  public void setPleonastic(boolean b) {
    this.pleonastic = b;
  }

  public void setTmpSalience(int s) {
    this.tmpSalience = s;
  }

  public void setWordIndex(int wordIndex) {
    this.wordIndex = wordIndex;
  }

  public String toString() {
    String localhead = " NULL";
    if (head != null) {
      localhead = ((TagWord) (head.getUserObject())).getText();
    }

    String argHStr = " NULL";
    if (this.argumentHost != null) {
      argHStr = " (ARG " +
          ((TagWord) (this.argumentHost.getUserObject())).getText() + ")";
    }

    String adjHStr = " NULL";
    if (this.adjunctHost != null) {
      adjHStr = " (ADJ " +
          ((TagWord) (this.adjunctHost.getUserObject())).getText() + ")";
    }

    String NPDHStr = " NULL";
    if (this.NPDomainHost != null) {
      NPDHStr = " (NPDomain " +
          ((TagWord) (this.NPDomainHost.getUserObject())).getText() + ")";
    }

    String argHeadStr = " NULL";
    if (this.argumentHead != null) {
      NPDHStr = " (ARGHead " +
          ((TagWord) (this.argumentHead.getUserObject())).getText() + ")";
    }

    String containHostStr = " NULL";
    if (this.containHost.size() > 0) {
      containHostStr = " (containHost ";
      for (int i = 0; i < containHost.size(); i++) {
        containHostStr +=
            ((TagWord) ((DefaultMutableTreeNode) containHost.get(i)).
                getUserObject()).getText() + "/";
      }
      containHostStr += ") ";
    }

    if (this.tag.startsWith("NP")
        || this.tag.startsWith("PP")
        || this.tag.startsWith("VP")) {
      localhead = " (HEAD " + localhead + ")";
    }
    else {
      localhead = "";
    }

    String npShow;
    NP np = this.getNP();
    if (np != null) {
      npShow = np.toString();
    }
    else {
      npShow = "no NP";
    }

    return wordIndex
        + " in "
        + sentenceIndex
        + "         "
        + tag
        + " "
        + getText()
        + " <NUMBER> "
        + this.number
        + localhead
        + argHStr
        + argHeadStr
        + adjHStr
        + NPDHStr
        + containHostStr
        + "\t "
        + npShow;

  }

  public String toStringBrief() {
    return "(" + sentenceIndex + "," + wordIndex + ") " + getText();
  }

}
