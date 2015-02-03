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

import java.io.File;
import java.util.Hashtable;
import java.util.List;

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

  private int sentenceIndex; // indicates sentence
  private int wordIndex;
  private Number number = Number.UNCLEAR;
  private Gender gender = Gender.UNCLEAR;
  private Human human = Human.UNCLEAR;
  private int people = 0;// o for unclear, 1 for first, 2 for second and 3 for
                         // third
  private boolean pleonastic = false; // represents a pleonastic pronoun

  private String tag;
  private String word;
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
  private NP npRepresentation = null;
  private TagWord antecedent = null;

  // the dynamically updated salience value
  int tmpSalience = 0;

  public TagWord(String tag, String word, int sentenceIndex, int wordIndex) {
    this.tag = tag;
    this.word = word;
    this.sentenceIndex = sentenceIndex;
    this.wordIndex = wordIndex;
  }

  public String getWord() {
    return this.word;
  }

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

  public void setWordIndex(int wordIndex) {
    this.wordIndex = wordIndex;
  }

  /**
   * Returns index of the word as a whole in the sentence
   * 
   * @return index of the word as a whole in the sentence
   */
  public int getWordIndex() {
    return wordIndex;
  }

  public int getSentenceIndex() {
    return sentenceIndex;
  }

  public void setNP(NP n) {
    this.npRepresentation = n;
  }

  public void setNumber(Number number) {
    this.number = number;
  }

  public Number getNumber() {
    if (number != Number.UNCLEAR) {
      return number;
    }
    if (npRepresentation.tagWord.size() == 1) {
      String tag = npRepresentation.tagWord.get(0).getTag();
      if (tag.endsWith("S")) { // NNS, NPS
        number = Number.PLURAL;
      } else if (HumanList.isPlural(getText())) {
        number = Number.PLURAL;
      } else {
        number = Number.SINGLE;
      }
    } else if (getNPRepresentation().hasAnd()) {
      number = Number.PLURAL;
    } else if (this.head != null) {
      number = ((TagWord) head.getUserObject()).getNumber();
    }
    return this.number;
  }

  public NP getNPRepresentation() {
    return this.npRepresentation;
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
      h = word; // for prp
    } else {
      TagWord tw = (TagWord) head.getUserObject();
      h = tw.getContent();
    }

    if (HumanList.isMale(h)) {
      this.gender = Gender.MALE;
    } else if (HumanList.isFemale(h)) {
      this.gender = Gender.FEMALE;
    }
    return gender;
  }

  public int getPronounIdx() {

    if (people != 0) {
      return people;
    }
    String h;
    if (head == null) {
      h = word; // for prp,
    }
    else {
      TagWord tw = (TagWord) (head.getUserObject());
      h = tw.getContent();
    }

    if (HumanList.isThirdPerson(h)) {
      return people = 3;
    } else if (HumanList.isSecondPerson(h)) {
      return people = 2;
    } else if (HumanList.isFirstPerson(h)) {
      return people = 1;
    } else {
      return 0;
    }
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
    String h = getContent();
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
    h = ((TagWord) head.getUserObject()).getContent();
    if (HumanList.isHuman(h)) {
      human = Human.HUMAN;
      return human;
    } else if (HumanList.isNotHuman(h)) {
      human = Human.NON_HUMAN;
      return human;
    }

    return human;
  }

  public void setPeople(int i) {
    people = i;
  }

  public int getPeople() {
    if (people != 0) {
      return people;
    }

    if (this.getContent().toLowerCase().matches("we|us")) {
      return people = 1;
    }

    if (this.getContent().toLowerCase().matches("you")) {
      return people = 2;
    }

    return people = 3; // default

  }

  public String getTag() {
    return this.tag;
  }

  public boolean isPRP() {
    return this.getNPRepresentation().isPRP();
  }

  public String getText() {
    return getContent();
  }

  public void setHead(DefaultMutableTreeNode n) {
    if (this.number != Number.UNCLEAR) {
      // number should be set afterword
      System.err.println("Number shouldn't be set before setHead.");
    }
    this.head = n;
  }

  public DefaultMutableTreeNode getHead() {
    return this.head;
  }

  public void setDeterminer(DefaultMutableTreeNode n) {
    determiner = n;
  }

  public DefaultMutableTreeNode getDeterminer() {
    return determiner;
  }

  public void setDeterminee(DefaultMutableTreeNode n) {
    determinee = n;
  }

  public DefaultMutableTreeNode getDeterminee() {
    return determinee;
  }

  /**
   * @param argumentHost: the NP in the same argument domain
   */
  public void setArgumentHost(DefaultMutableTreeNode n) {
    this.argumentHost = n;
  }

  public DefaultMutableTreeNode getArgumentHost() {
    return this.argumentHost;
  }

  public void setArgumentHead(DefaultMutableTreeNode n) {
    this.argumentHead = n;
  }

  public DefaultMutableTreeNode getArgumentHead() {
    return this.argumentHead;
  }

  public void setAdjunctHost(DefaultMutableTreeNode n) {
    this.adjunctHost = n;
  }

  public DefaultMutableTreeNode getAdjunctHost() {
    return this.adjunctHost;
  }

  public void setNPDomainHost(DefaultMutableTreeNode n) {
    this.NPDomainHost = n;
  }

  public DefaultMutableTreeNode getNPDomainHost() {
    return this.NPDomainHost;
  }

  public void setContainHost(DefaultMutableTreeNode n) {
    this.containHost.add(n);
  }

  public void setContainHost(List<DefaultMutableTreeNode> n) {
    this.containHost.addAll(n);
  }

  public java.util.List<DefaultMutableTreeNode> getContainHost() {
    return this.containHost;
  }

  public void setPleonastic(boolean b) {
    this.pleonastic = b;
  }

  public boolean isPleonastic() {
    return this.pleonastic;
  }

  /**
   * @param npAlien The np that salience weight of this TagWord is considered
   *          for.
   */
  public int getSalience(NP npAlien) {

    int sal = 0;
    NP np = this.getNPRepresentation();
    if ((np != null) && (npAlien != null)) {
      sal = np.getSalience(npAlien);
    }
    // dampen the salience as distance increases
    sal = sal
        / (Math.abs(this.getSentenceIndex() - npAlien.getSentenceIdx()) + 1);

    // penalize cataphora (if this appears after npAlien)
    if ((this.getSentenceIndex() == npAlien.getSentenceIdx()
        && this.getNPRepresentation().getOffset() > npAlien.getOffset())
        || this.getSentenceIndex() > npAlien.getSentenceIdx()) {
      sal = sal / 4; // reduce the weight substantially
    }
    return sal;
  }

  /**
   * @return true if thsi NP is not contained in another NP
   */
  public boolean isHeadNP() {
    if (argumentHead != null) {
      return false;
    }

    if (adjunctHost != null) {
      return false;
    }

    if (hasNPAncestor) {
      return false;
    }
    isHeadNP = true;
    return isHeadNP;
  }

  public boolean hasNPAncestor() {
    return hasNPAncestor;
  }

  public void setHasNPAncestor(boolean b) {
    hasNPAncestor = b;
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

    NP np = this.getNPRepresentation();
    NP npGuest = tw.getNPRepresentation();

    if (np != null && npGuest != null) {
      np.mergeSalience(npGuest);
    }
  }

  public int getSalience(TagWord tw) {
    return getSalience(tw.getNPRepresentation());
  }

  public void setTmpSalience(int s) {
    this.tmpSalience = s;
  }

  public int getTmpSalience() {
    return this.tmpSalience;
  }

  /**
   * amplify sentence index difference by multiply 100
   */
  public int distanceInText(TagWord tw) {
    return Math.abs(this.getSentenceIndex() - tw.getSentenceIndex()) * 100
        + Math.abs(this.getWordIndex() - tw.getWordIndex());
  }

  public String getContent() {

    if (word.indexOf(")") == -1) {
      // word = " something"
      return word;
    }
    int pointerR = word.indexOf(")");
    int pointerL = word.substring(0, pointerR).lastIndexOf(" ");
    int spaceAfter;
    String text = word.substring(pointerL + 1, pointerR);
    // check for the leading "("
    if (text.endsWith("-LRB-")) {
      text = "(";
    } else if (text.endsWith("-RRB-")) {
      text = ")";
    }

    while (((spaceAfter = word.indexOf(" ", pointerR)) != -1)
        && ((pointerR = word.indexOf(")", spaceAfter)) != -1)) {
      pointerL = word.substring(0, pointerR).lastIndexOf(" ");
      String tmp = " " + word.substring(pointerL + 1, pointerR);
      if (tmp.endsWith("-LRB-")) {
        tmp = " (";
      } else if (tmp.endsWith("-RRB-")) {
        tmp = " )";
      }

      text += tmp;
    }
    return text;
  }

  public String getSubstitutedContent() {
    return this.getAntecedent().getContent();
  }

  public void setAntecedent(TagWord ant) {
    if (ant.getAntecedent() == this) {
      return;
    }
    antecedent = ant;
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

  public String toStringBrief() {
    return "(" + sentenceIndex + "," + wordIndex + ") " + getContent();
  }

  public String toString() {
    String localhead = " NULL";
    if (head != null) {
      localhead = ((TagWord) (head.getUserObject())).getContent();
    }

    String argHStr = " NULL";
    if (this.argumentHost != null) {
      argHStr = " (ARG " +
          ((TagWord) (this.argumentHost.getUserObject())).getContent() + ")";
    }

    String adjHStr = " NULL";
    if (this.adjunctHost != null) {
      adjHStr = " (ADJ " +
          ((TagWord) (this.adjunctHost.getUserObject())).getContent() + ")";
    }

    String NPDHStr = " NULL";
    if (this.NPDomainHost != null) {
      NPDHStr = " (NPDomain " +
          ((TagWord) (this.NPDomainHost.getUserObject())).getContent() + ")";
    }

    String argHeadStr = " NULL";
    if (this.argumentHead != null) {
      NPDHStr = " (ARGHead " +
          ((TagWord) (this.argumentHead.getUserObject())).getContent() + ")";
    }

    String containHostStr = " NULL";
    if (this.containHost.size() > 0) {
      containHostStr = " (containHost ";
      for (int i = 0; i < containHost.size(); i++) {
        containHostStr +=
            ((TagWord) ((DefaultMutableTreeNode) containHost.get(i)).
                getUserObject()).getContent() + "/";
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
    NP np = this.getNPRepresentation();
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
        + getContent()
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

}

class HumanList {

  final static String[] maleList = new String("he him himself his").split(" ");
  final static String[] femaleList = new String("she her herself").split(" ");
  final static String[] thirdPersonList = new String(
      "he him himself his she her herself they them their themselves it its itself")
      .split(" ");
  final static String[] secondPersonList = new String(
      "you your yourself yourselves").split(" ");
  final static String[] firstPersonList = new String(
      "i me my myself we us our ourselves").split(" ");
  final static String[] list = new String(
      "i me myself my we us ourselves our they them themselves their")
      .split(" ");
  final static String[] pluralList = new String(
      "we us ourselves our they them themselves their").split(" ");
  final static String[] wholeList = new String(
      "he him himself his she her herself"
          + " i me myself my we us ourselves our you your yourself").split(" ");
  final static String[] complementList = new String("it its itself").split(" ");
  final static String[] auxZList = new String("is does has was").split(" ");
  final static String[] titleList = new String("Mr. Mrs. Miss Ms.").split(" ");

  final static int numberOfNameToCheck = -1; // 3000; //check only the first xx
                                             // most common first names,
                                             // respectively
  // final static Hashtable maleNameTb =
  // getNameTb(System.getProperty("dataPath") + File.separator
  // +"male_first.txt",numberOfNameToCheck);
  final static Hashtable<String, String> maleNameTb = getNameTb(
      System.getProperty("dataPath")
          + File.separator
          + "MostCommonMaleFirstNamesInUS.mongabay.txt",
      numberOfNameToCheck);
  final static Hashtable<String, String> femaleNameTb = getNameTb(
      System.getProperty("dataPath") + File.separator + "female_first.txt",
      numberOfNameToCheck);
  final static Hashtable<String, String> humanOccupationTb = getNameTb(System
      .getProperty("dataPath") + File.separator + "personTitle.txt");
  final static Hashtable<String, String> lastNameTb = getNameTb(System
      .getProperty("dataPath") + File.separator + "name_last.txt");

  public HumanList() {

  }

  public static boolean isMale(String wd) {
    // People's name should start with a capital letter
    return contains(maleList, wd)
        || (wd.matches("[A-Z][a-z]*") && contains(maleNameTb, wd));
  }

  public static boolean isFemale(String wd) {
    // People's name should start with a capital letter
    return contains(femaleList, wd)
        || (wd.matches("[A-Z][a-z]*") && contains(femaleNameTb, wd));
  }

  public static boolean isHuman(String wd) {
    if (wd.indexOf(" ") > 0 && contains(titleList, wd.split(" ")[0], true)) {
      // contains more than a single word and starts with a title
      return true;
    }
    return contains(wholeList, wd)
        // || contains((humanOccupationTb),wd)
        || isMale(wd)
        || isFemale(wd);
  }

  public static boolean isNotHuman(String wd) {
    return contains(complementList, wd);
  }

  public static boolean isPlural(String wd) {
    return contains(pluralList, wd);
  }

  public static boolean isThirdPerson(String wd) {
    return contains(thirdPersonList, wd);
  }

  public static boolean isSecondPerson(String wd) {
    return contains(secondPersonList, wd);
  }

  public static boolean isFirstPerson(String wd) {
    return contains(firstPersonList, wd);
  }

  // public static boolean isHumanTitle(String wd){
  // return contains(humanTitleTb,wd.toLowerCase());
  // }

  public static boolean contains(String[] list, String str) {
    return contains(list, str, false);
  }

  public static boolean contains(String[] list, String str,
      boolean caseSensitive) {
    boolean contain = false;

    if (caseSensitive) { // make this a outer check for efficiency's sake
      for (int i = 0; i < list.length; i++) {
        if (list[i].equals(str)) {
          contain = true;
          break;
        }
      }
    } else {
      for (int i = 0; i < list.length; i++) {
        if (list[i].equalsIgnoreCase(str)) {
          contain = true;
          break;
        }
      }
    }

    return contain;
  }

  public static boolean contains(Hashtable<String, String> tb, String wd) {
    return tb.containsKey(wd);
  }

  private static String[] retriveList(String listFile) {
    return AnaphoraResolver.read(listFile).toString().split("\\s+");
  }

  private static Hashtable<String, String> getNameTb(String listFile) {
    return getNameTb(listFile, -1);
  }

  private static Hashtable<String, String>
      getNameTb(String listFile, int range) {
    String[] nameArray = retriveList(listFile);
    Hashtable<String, String> tb = new Hashtable<String, String>();

    if (nameArray.length <= 0) {
      System.err
          .println(listFile
              + " not found. Please download the latest data files. \n System quit.");
      System.exit(0);
    }

    if (nameArray != null) {
      int stopAt;
      if (range == -1) {
        stopAt = nameArray.length;
      } else {
        stopAt = Math.min(range, nameArray.length);
      }
      for (int i = 0; i < stopAt; i++) {
        String name = nameArray[i].substring(0, 1);
        if (nameArray[i].length() > 1) {
          name += nameArray[i].substring(1).toLowerCase();
        }
        tb.put(name, name);
      }
    }
    return tb;
  }

}
