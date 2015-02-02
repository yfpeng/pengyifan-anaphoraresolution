/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005,2006 Long Qiu
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

import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.tree.*;

// import edu.nus.comp.nlp.gadget.*;
/**
 * @author Qiu Long
 * @version 1.0
 * @history Feb 12, 2006 Make it work on windows. Long Qiu
 * @author "Yifan Peng"
 */

public class Util {

  static {
    Env env = new Env();
  }

  public Util() {
  }

  /**
   * Filter out none-sentence part in TREC plain text corpus 27 nusmml2003
   * XIE19960317.0175 Like most of the world 's subway systems , the lines
   * operate at a loss .
   * 
   * @param fileName
   * @return
   */
  public static StringBuffer corpusRead(String fileName) {
    StringBuffer sb = new StringBuffer();
    try {
      BufferedReader in =
          new BufferedReader(new FileReader(fileName));
      String s;
      Pattern p = Pattern.compile("(\\d)+\\s+\\S+\\s+\\S+\\s+");
      while ((s = in.readLine()) != null) {
        Matcher m = p.matcher(s);
        if (m.find() && (m.start() == 0)) {
          sb.append(s.substring(m.end()));
          sb.append(System.getProperty("line.separator"));
        }
      }
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    return sb;
  }

  // older version of readFile.
  public static StringBuffer readFile(String fileName) {
    return readFile(fileName, null);
  }

  /**
   * @param fileName
   * @param commentFlag The leading character indicating a line of comment.
   * @return The content, preferably plain text, of the file "fileName", with
   *         the comments ignored.
   */
  public static StringBuffer readFile(String fileName, String commentFlag) {
    StringBuffer sb = new StringBuffer();
    try {
      BufferedReader in =
          new BufferedReader(new FileReader(fileName));
      String s;
      while ((s = in.readLine()) != null) {
        if (commentFlag != null
            && s.trim().startsWith(commentFlag)) {
          // ignore this line
          continue;
        }
        sb.append(s);
        // sb.append("/n"); to make it more platform independent (Log July 12,
        // 2004)
        sb.append(System.getProperty("line.separator"));
      }
      in.close();
    } catch (IOException ex) {
      ex.printStackTrace();
      System.exit(-1);
    }
    return sb;

  }

  public static StringBuffer read(String fileName) {
    StringBuffer sb = new StringBuffer();
    try {
      BufferedReader in =
          new BufferedReader(new FileReader(fileName));
      String s;

      while ((s = in.readLine()) != null) {
        sb.append(s);
        sb.append(System.getProperty("line.separator"));
      }
      in.close();
    } catch (IOException ex) {
      System.err.println(fileName + " not found. Please check it." +
          System.getProperty("line.separator") + "Skipping...");
    }
    return sb;
  }

  public static String removeTag(String str) {
    String rst = new String();
    Pattern p = Pattern.compile("<[.[^<>]]+>");
    Matcher m = p.matcher(str);
    rst = m.replaceAll(" ");
    return rst;
  }

  public static String removeBlankLine(String str) {
    String rst = new String();
    Pattern p = Pattern.compile("\n([\\s^\n]*\n[\\s^\n]*)+");
    Matcher m = p.matcher(str);
    rst = m.replaceAll("\n");
    return rst;
  }

  public static String removeSpace(String str) {
    return str.replaceAll("\\s", "");
  }

  public static String mergeDoubleSingleQuots(String str) {
    return str.replaceAll("''|``", "\"");
  }

  public static void UnixSystemCall(String command, String outputFileName) {
    try {
      String[] cmd = {
          "/bin/sh",
          "-c",
          "ulimit -s unlimited;" + command + "  > " + outputFileName };
      Process proc = Runtime.getRuntime().exec(cmd);
      proc.waitFor();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("\"Wrong.\"Murmurs Util.java.");
      System.exit(-1);
    }
  }

  public static String UnixSystemCall(String command) {
    String output = new String();
    try {
      String[] cmd = {
          "/bin/sh",
          "-c",
          // "ulimit -s unlimited;" +
          command };
      Process proc = Runtime.getRuntime().exec(cmd);
      BufferedReader in = new BufferedReader(new InputStreamReader(proc.
          getInputStream()));
      String s;
      while ((s = in.readLine()) != null) {
        output += s + "\n";
      }
      proc.waitFor();
      in.close(); // Added by Qiu Long on Nov. 25, 2004
      proc.destroy();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println("\"Wrong.\"Murmurs Util.java.");
      System.exit(-1);
    }
    return output;
  }

  public static void write(String fileName, String content, boolean append) {

    try {
      PrintWriter out = new PrintWriter(
          new BufferedWriter(new FileWriter(fileName, append)));
      out.print(content);
      out.close();
    } catch (IOException ex) {
      System.err.println("Can not open \"" + fileName +
          "\" to write. Please check.");
      System.exit(-1);
    }
  }

  public static void write(String fileName, String content) {
    write(fileName, content, false);
  }

  public static Vector<TagWord> analyseTagWordPairs(String aNP,
      Vector<TagWord> vec, int sIdx) {
    int pointer = 0; // to indicate position in the string
    int adjPointer = 0; // adjunct pointer
    String tag = null;
    String word;
    if (aNP.length() == 0) {
      return vec;
    }

    while (pointer >= 0) {
      pointer = aNP.indexOf("(", pointer);
      if (pointer == -1) {
        break;
      }

      adjPointer = aNP.indexOf(" ", pointer);

      if (adjPointer < 0) {
        break;
      }
      tag = aNP.substring(pointer + 1, adjPointer);

      // testing if it's (TAG word)
      if (aNP.startsWith("(", adjPointer + 1)) {
        pointer = adjPointer;
        continue;
      }

      word = aNP.substring(adjPointer, pointer = aNP.indexOf(")", adjPointer));
      // vec.add(new TagWord(tag,word,sIdx,offset++));
      vec.add(new TagWord(tag, word, sIdx, adjPointer));
    }

    return vec;
  }

  public static
      int
      findMatcher(String target, String matcheeL, String matcherR) {
    int loc = 0;
    int depth = 0;
    if (target.indexOf(matcheeL) == -1) {
      return 0;
    }

    for (int i = 0; i < target.length(); i++) {
      if (target.charAt(i) == matcheeL.charAt(0)) {
        depth++;
      }
      else if (target.charAt(i) == matcherR.charAt(0)) {
        depth--;
      }
      if (depth == 0) {
        return i;
      }
    }
    return loc;
  }

  /**
   * Convert the output of Charniak parser for ~A single sentence~ into a
   * TreeNode.
   * */
  static public DefaultMutableTreeNode convertSentenceToTreeNode(int sIdx,
      String annotedText, String delimL, String delimR) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
    int endPos = findMatcher(annotedText, delimL, delimR);

    if (endPos == (annotedText.length() - 1)) {
      node.setUserObject(new TagWord(annotedText, sIdx, -1));
      addChildren(sIdx, node,
          annotedText.substring(annotedText.indexOf(" ") + 1,
              annotedText.length() - 1), delimL,
          delimR);
    }
    else {
      System.err.print("Parsing result error:\n" + annotedText + "\n");
      Util.errLog("Parsing result error:\n" + annotedText + "\n");
      return convertSentenceToTreeNode(sIdx,
          "(S1 (FRAG (NP (CD XIE20030000.0000)) (. .)))", delimL, delimR);
    }
    Util.computeOffset(node);
    return node;
  }

  static void addChildren(int sIdx, DefaultMutableTreeNode parentNode,
      String annotedText, String delimL, String delimR) {
    int leadPos = annotedText.indexOf(delimL);

    if (leadPos == -1) {
      return;
    }
    int endPos = findMatcher(annotedText, delimL, delimR);
    if (endPos == (annotedText.length() - 1)) {
      DefaultMutableTreeNode singleChild = new DefaultMutableTreeNode(new
          TagWord(annotedText, sIdx, -1));
      if (!singleChild.toString().equalsIgnoreCase(parentNode.toString())) {
        parentNode.add(singleChild);
        if (annotedText.indexOf(delimL, 1) == -1) {
          return;
        }
        addChildren(sIdx, singleChild,
            annotedText.substring(annotedText.indexOf(delimL, 1),
                annotedText.length() - 1), delimL,
            delimR);
      }
      else {
        if (annotedText.indexOf(delimL, 1) == -1) {
          return;
        }
        addChildren(sIdx, parentNode,
            annotedText.substring(annotedText.indexOf(delimL, 1),
                annotedText.length() - 1), delimL,
            delimR);
      }

      return;
    }
    while (endPos <= (annotedText.length() - 1)) {
      DefaultMutableTreeNode aChild =
          new DefaultMutableTreeNode(new TagWord(annotedText.substring(leadPos,
              endPos + 1), sIdx, -1));
      parentNode.add(aChild);

      addChildren(sIdx, aChild, annotedText.substring(leadPos, endPos + 1),
          delimL, delimR);
      leadPos = annotedText.indexOf(delimL, endPos);
      if (leadPos == -1) {
        return;
      }
      endPos = findMatcher(annotedText.substring(annotedText.indexOf(delimL,
          leadPos)), delimL, delimR);
      endPos += annotedText.indexOf(delimL, leadPos);
    }

  }

  public static void computeOffset(DefaultMutableTreeNode n) {
    String rootTag = null;

    try {
      rootTag = ((TagWord) n.getUserObject()).getTag();
    } catch (Exception ex) {
      System.out.println(n);
      System.exit(0);
    }

    if (!rootTag.equalsIgnoreCase("S1")) {
      // shouldn't assign offset to none-sentence
      Util.errLog("//shouldn't assign offset to none-sentence");
      return;
    }
    int offset = 0; // syntatic unit index, zero based
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = n.postorderEnumeration();
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumeration
          .
          nextElement();
      if (currentNode.isLeaf()) {
        TagWord tw = (TagWord) currentNode.getUserObject();
        tw.setOffset(offset++);
      }
      else {
        TagWord tw = (TagWord) currentNode.getUserObject();
        TagWord firstChildtw = (TagWord) (((DefaultMutableTreeNode)
            currentNode.getFirstChild()).
                getUserObject());
        tw.setOffset(firstChildtw.getOffset());
      }
    }
  }

  public static void splitFile(String fileName, String delim) {
    String[] segS = Util.read(fileName).toString().split(delim);
    for (int i = 0; i < segS.length; i++) {
      Util.write(fileName + "." + i, segS[i] + delim);
    }
  }

  public static void treeNodeTest(DefaultMutableTreeNode n) {
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = n.breadthFirstEnumeration();
    int count = 0;
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.
          nextElement();
      System.out.println(node.getDepth() + node.toString());
      count++;
    }
    System.out.println(count + " nodes in total.");
  }

  public static void errLog(String errMsg) {
    if (System.getProperty("keep log") != null &&
        System.getProperty("keep log").equals("true")) {
      write(System.getProperty("outputDir") + File.separator + "log",
          errMsg + System.getProperty("line.separator"), true);
    }
    if (System.getProperty("display log") != null &&
        System.getProperty("display log").equals("true")) {
      System.out.println(errMsg);
    }
  }

  public static void showMessage(String message, boolean b) {
    if (!b) {
      // quiet mode
      return;
    }
    else {
      System.out.println(message);
    }
  }

  public static String parse(String command, String dataDir, String inputFile,
      String outputFile) {
    // OutputFile is not used anymore. The parse is returned instead.
    String output = new String();

    try {
      if (!new java.io.File(dataDir).exists()) {
        // can't find the parserDate dir

        // java.net.InetAddress a=java.net.InetAddress.getLocalHost();
        // String str = a.getHostName();
        // System.err.println(str);

        System.err.println(
            "Can't initialize the parser properly. Please check the path: " +
                command + " " + dataDir);
        System.exit(-1);
      }

      // Old code, only works on unix
      // String[] cmd = {
      // "/bin/sh",
      // "-c",
      // "ulimit -s unlimited;" + command + " " +
      // System.getProperty("parserOption") + dataDir + " " + inputFile +
      // " > " + outputFile};

      Process proc = null;

      if (System.getProperty("os.name").startsWith("Windows")) {
        String[] cmd = {
            "cmd",
            "/c",
            command
                + " "
                + System.getProperty("parserOption")
                + dataDir
                + " "
                + inputFile };
        // System.err.println("cmd /c  "+command + " " +
        // System.getProperty("parserOption") + dataDir + " " + inputFile);
        proc = Runtime.getRuntime().exec(cmd);
      } else {
        String[] cmd = {
            "/bin/sh",
            "-c",
            "ulimit -s unlimited;" + command + " " +
                System.getProperty("parserOption") + dataDir + " " + inputFile };
        proc = Runtime.getRuntime().exec(cmd);
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(proc.
          getInputStream()));
      String s;
      while ((s = in.readLine()) != null) {
        output += s + "\n";
      }
      proc.waitFor();
      in.close(); // Added by Qiu Long on Nov. 25, 2004
      proc.destroy();
    } catch (Exception e) {
      System.err.println("Wrong");
    }
    return output;
  }

  public static void resolverBaseline(Vector<TagWord> aNPList) {
    Iterator<TagWord> iterator = aNPList.iterator();
    NP np = new NP(0, 0);
    Stack<NP> NPStack = new Stack<NP>();
    boolean matched = false;
    while (iterator.hasNext()) {
      np = ((TagWord) iterator.next()).getNPRepresentation();
      matched = false;
      if (np.isPRP()) {

        while (NPStack.size() != 0) {
          NP inStack = (NP) NPStack.pop();
          if (inStack.isHuman() == np.isHuman()) {
            matched = true;
            System.err.println(inStack.toDisplay() + "<--->" + np.toDisplay());
            NPStack.clear();
            break;
          }
        }
        if (!matched) {
          System.err.println("NULL" + "<--->" + np.toDisplay());
        }
      }
      else {
        NPStack.push(np);
      }
    }
  }

  public static Vector<CorreferencialPair> resolverV1(Vector<TagWord> aNPList,
      Vector<TagWord> aPRPList) {
    Vector<String> results = new Vector<String>(); // to display
    Vector<CorreferencialPair> resultsOut = new Vector<CorreferencialPair>(); // for
                                                                              // substitution
    int scope = 1; // How many sentences to look back. /****para****/
    int threshhold = 30;
    TagWordSalienceComp twComp = new TagWordSalienceComp();

    Iterator<TagWord> npIterator = aNPList.iterator();
    Iterator<TagWord> prpIterator = aPRPList.iterator();
    boolean foundMatcher = false;

    while (prpIterator.hasNext()) {
      foundMatcher = false;

      TagWord prpTw = (TagWord) prpIterator.next();

      // label pleonastic pronoun's anaphoraic antecedence as NULL and procede
      if (prpTw.isPleonastic()) {
        TagWord obj = null;
        results.add(processResult(obj, prpTw));
        resultsOut.add(new CorreferencialPair(obj, prpTw));
        continue;
      }

      // consider only third person pronoun
      if (!HumanList.isThirdPerson(prpTw.getText())) {
        continue;
      }

      npIterator = aNPList.iterator(); // rewind
      Vector<TagWord> npCandidates = new Vector<TagWord>();
      while (npIterator.hasNext()) {
        TagWord npTw = (TagWord) npIterator.next();
        // skip pleonastic NP, whose only child is pleonastic pronoun 'it'
        if (npTw.isPleonastic()) {
          continue;
        }

        if ((npTw.getSentenceIdx() + scope) < prpTw.getSentenceIdx()) {
          // ignore NP 'scope' sentences ahead
          continue;
        }

        boolean b1 = prpTw == npTw;
        boolean b2 = npTw.getNPRepresentation().getNodeRepresent().isNodeChild(
            prpTw.getNPRepresentation().getNodeRepresent());
        boolean b3 = npTw.getNPRepresentation().getNodeRepresent().
            getChildCount() == 1;
        boolean b4 = (npTw.getNPRepresentation().getNodeRepresent().
            isNodeDescendant(prpTw.getNPRepresentation().
                getNodeRepresent()));
        boolean b5 = b1 || (b2 && b3) || b4;

        if (b5) {
          // self reference :)
          // Case 1: (PRP$ xxx) (PRP$ xxx)
          // Case 2: (NP (PRP xxx)) (PRP xxx)
          continue;
        }

        if (npTw.getSentenceIdx() > prpTw.getSentenceIdx()) {
          // only consider anaphora
          break;
        }

        // System.out.println("A     "+prpTw);
        // System.out.println("      "+npTw);

        // filtering
        NP prpNP = prpTw.getNPRepresentation();
        DefaultMutableTreeNode prpNode = prpNP.getNodeRepresent();

        if (prpNP.isReflexive()) {
          if (matchLexcialAnaphor(npTw, prpTw)) {
            foundMatcher = true;

            // building NP chains whose rings are refering to the same thing.
            if (prpNode.getSiblingCount() == 1) {
              // this PRP is the only child of the NP parent
              DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) prpNode
                  .getParent();
              if (parentNode != null) {
                ((TagWord) parentNode.getUserObject()).setAntecedent(npTw);
              }
            }

            results.add(processResult(npTw, prpTw));
            if (System.getProperty("referenceChain").equals("false")) { // true/undefine
                                                                        // by
                                                                        // default
              resultsOut.add(new CorreferencialPair(npTw, prpTw));
            }
            else {
              resultsOut
                  .add(new CorreferencialPair(npTw.getAntecedent(), prpTw));
            }

            break;
          }
        }
        else {
          if (!matchPronominalAnaphor(npTw, prpTw)) {
            continue;
          }
        }

        // grading
        if (npTw.getSalience(prpNP) < threshhold) {
          // ignore those with small salience weight
          continue;
        }
        npTw.setTmpSalience(npTw.getSalience(prpNP));
        npCandidates.add(npTw);

      }

      if (!foundMatcher) {
        TagWord[] sortedCandidates = npCandidates.toArray(new TagWord[0]);
        Arrays.sort(sortedCandidates, twComp);

        // result

        TagWord obj = getBestCandidate(sortedCandidates, prpTw);
        results.add(processResult(obj, prpTw));

        if (obj != null) {
          NP prpNP = prpTw.getNPRepresentation();
          DefaultMutableTreeNode prpNode = prpNP.getNodeRepresent();
          // building NP chains whose 'rings' are refering to the same thing.
          if (prpNode.getSiblingCount() == 1) {
            // this PRP is the only child of the NP parent
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) prpNode
                .getParent();
            if (parentNode != null) {
              ((TagWord) parentNode.getUserObject()).setAntecedent((TagWord)
                  obj);
            }
          }

          if (System.getProperty("referenceChain").equals("false")) { // true/undefine
                                                                      // by
                                                                      // default
            resultsOut.add(new CorreferencialPair((TagWord) obj, prpTw));
          }
          else {
            resultsOut.add(new CorreferencialPair(((TagWord) obj).
                getAntecedent(), prpTw));
          }

        }
        else {
          // no candidate is found
          resultsOut.add(new CorreferencialPair((TagWord) obj, prpTw));
        }
      }

    }

    String toWrite = null;
    if (results.toString().length() > 2) {
      toWrite = results.toString()
          .substring(2, results.toString().length() - 1);
    }
    else {
      toWrite = "null";
    }

    if (System.getProperty("write resolving results") == null ||
        System.getProperty("write resolving results").equals("true")) { // make
                                                                        // use
                                                                        // of
                                                                        // shortcut
      String resultsFileName = System.getProperty("outputDir")
          + File.separator
          +
          "resolvingresults.txt";
      Util.write(resultsFileName, toWrite); // remove enclosing brackets
      Util.errLog("Resolving Results written to file " + resultsFileName);
    }
    Util.errLog("***********Head of Results**************\n" + toWrite +
        "\n***********End of Results***************\n");
    Util.showMessage(
        "********Anaphor-antecedent pairs*****\n" + toWrite + "\n",
        System.getProperty("display resolving results").equals(
            "true"));
    return resultsOut;
  }

  private static
      TagWord
      getBestCandidate(TagWord[] sortedCandidates, TagWord tw) {
    TagWord obj = null;

    // Check for empty candidate list
    if (sortedCandidates.length == 0) {
      return obj;
    }
    else if (sortedCandidates.length == 1) {
      return sortedCandidates[0];
    }
    else { // with more in the list
      TagWord tw0 = (TagWord) sortedCandidates[sortedCandidates.length - 1];
      TagWord tw1 = (TagWord) sortedCandidates[sortedCandidates.length - 2];
      if (tw0.getTmpSalience() > tw1.getTmpSalience()) {
        return tw0;
      }
      else {
        // if(tw0.distanceInText(tw) <= tw1.distanceInText(tw)){
        /*
         * if(Math.abs(tw0.getDepth() - tw.getDepth()) <
         * Math.abs(tw1.getDepth() - tw.getDepth()) ){ //closer depth obj =
         * tw0; }else if(tw0.distanceInText(tw) <= tw1.distanceInText(tw)){ obj
         * = tw0; }else{ obj = tw1; }
         */

        if (tw0.distanceInText(tw) < tw1.distanceInText(tw)) {
          // take closer one
          obj = tw0;
        }
        else if (tw0.getNPRepresentation().getNodeRepresent().isNodeAncestor(
            tw1.getNPRepresentation().getNodeRepresent())) {
          // take child
          obj = tw0;
        }
        else {
          obj = tw1;
        }

      }
    }
    return obj;
  }

  /**
   *
   * @param npTw
   * @param lexTw
   * @return true if the two NPs are highly likely to be co-reference
   */
  private static boolean matchLexcialAnaphor(TagWord npTw, TagWord lexTw) {
    // Anaphor Binding Algorithm (Lappin and Leass)
    boolean judge = false;
    DefaultMutableTreeNode npNode = npTw.getNPRepresentation()
        .getNodeRepresent();

    if (lexTw.getArgumentHost() == npNode) {
      // lexical anaphor is in the argument domain of N
      return true;
    }
    else if (lexTw.getAdjunctHost() == npNode) {
      // lexcial anaphor is in the adjunct domain of N
      return true;
    }
    else if (lexTw.getNPDomainHost() == npNode) {
      // lexcial anaphor is in the NP domain of N
      return true;
    }
    else if (morphologicalFilter(npTw, lexTw) == false) {
      return false;
    }
    /**
     * @todo : 4,5 code is not working. Removed form this release
     * */
    return judge;
  }

  /**
   * @param npTw
   * @param prpTw
   * @return true if the two NPs are possible to be co-reference
   */
  private static boolean matchPronominalAnaphor(TagWord npTw, TagWord prpTw) {
    // Syntactic Filter (Lappin and Leass)
    boolean judge = true;
    DefaultMutableTreeNode npNode = npTw.getNPRepresentation()
        .getNodeRepresent();

    if (prpTw.getArgumentHost() == npNode) {
      // 2.pronominal anaphor is in the argument domain of N
      return false;
    }
    else if (prpTw.getAdjunctHost() == npNode) {
      // 3.pronominal anaphor is in the adjunct domain of N
      return false;
    }
    else if (prpTw.getNPDomainHost() == npNode) {
      // 5. pronominal anaphor is in the NP domain of N
      return false;
    }
    else if (npTw.getContainHost().contains(prpTw.getArgumentHead())) {
      // 4.
      if (!npTw.isPRP()) {
        return false;
      }
    }
    else if (npTw.getContainHost().contains(prpTw.getDeterminee())) {
      // 6.
      return false;
    }
    else if (morphologicalFilter(npTw, prpTw) == false) {
      // 1
      return false;
    }

    // Todo: improve 1,6
    return judge;
  }

  /**
   * A morphological filter for ruling out anaphoric dependence of a pronoun on
   * an NP due to non-agreement of person, number, or gender features.
   * 
   * @return false if disagree.
   */
  private static boolean morphologicalFilter(TagWord npTw, TagWord prpTw) {

    if (Math.abs(prpTw.getGender() - npTw.getGender()) > 1) {
      return false;
    }
    else if (Math.abs(npTw.getNumber() - prpTw.getNumber()) > 1) {
      /**** para ****/
      return false;
    }
    else if ((npTw.getPronounIdx() != prpTw.getPronounIdx())
        && (npTw.getPronounIdx() * prpTw.getPronounIdx() != 0)) {
      // getPronounIdx also assigns the predicate "people" as well
      return false;
    }
    else if (Math.abs(npTw.getHumanIdx() - prpTw.getHumanIdx()) > 1) {
      return false;
    }
    else if ((npTw.getPeople() != prpTw.getPeople())
        && (npTw.getPeople() * prpTw.getPeople() != 0)) {
      return false;
    }
    else {
      return true;
    }
  }

  static String processResult(TagWord np, TagWord referer) {
    String refereeStr = null;
    String anaphorStr = null;
    if (np == null) {
      refereeStr = "NULL";
      /*
       * }else if(
       * ((TagWord)np).getAntecedent().getNPRepresentation().getNodeRepresent
       * ().isNodeChild(
       * ((TagWord)referer).getNPRepresentation().getNodeRepresent()) ){ //self
       * refering refereeStr = "NULL"; //Todo: Util.java processResult()
       * //April 29, 2004: In //
       * "But they have 200 % of the (high-end) market they want, which is not bad"
       * //One "they" is regarded as another "they"'s child, which is to be
       * looked at.
       */
    }
    else {
      if (System.getProperty("referenceChain").equals("false")) { // true/undefine
                                                                  // by default
        refereeStr = np.toStringBrief();
      }
      else {
        refereeStr = np.getAntecedent().toStringBrief(); // Bind to the
                                                         // earliest
                                                         // NP****para****
      }
      // update salience factors for the detected coreferencial pair
      np.mergeSalience(referer);
      referer.mergeSalience(np);
    }
    anaphorStr = ((TagWord) referer).toStringBrief();
    return "\n" + refereeStr + " <-- " + anaphorStr;
  }

  static public String substitutionV0(DefaultMutableTreeNode root,
      Vector result) {
    // Build sentArray

    String aSentStr;
    @SuppressWarnings("rawtypes")
    Enumeration sentences = root.children();
    StringBuffer[] sentArray = new StringBuffer[root.getChildCount()];
    int i = 0;
    while (sentences.hasMoreElements()) {
      aSentStr = ((TagWord) ((DefaultMutableTreeNode) sentences.nextElement()).
          getUserObject()).getContent();
      sentArray[i++] = new StringBuffer(aSentStr);
    }

    // substitution
    Iterator iterator = result.iterator();

    while (iterator.hasNext()) {
      CorreferencialPair cp = (CorreferencialPair) iterator.next();
      if (cp.referee == null) {
        continue;
      }
      int refererSIdx = cp.referer.getSentenceIdx();
      int leng = cp.referer.getContent().length();
      aSentStr = sentArray[refererSIdx].toString();
      // Locate the beginning position of the referer in the possiblly altered
      // sentence.
      int begin = findOffset(aSentStr, cp.referer.getOffset());
      sentArray[refererSIdx].replace(begin
          , begin + leng
          ,
          "<" + cp.referee.getSubstitutedContent() + ">");
    }

    String substitutedOutput = " ";
    for (i = 0; i < sentArray.length; i++) {
      substitutedOutput += sentArray[i].toString() + "\n";
    }
    return substitutedOutput;
  }

  public static boolean arrayMatch(String[] strArr1, String[] strArr2) {
    int l1 = strArr1.length;
    int l2 = strArr2.length;
    String str1 = "";
    String str2 = "";

    for (int i = 0; i < l1; i++) {
      str1 += strArr1[i];
    }
    for (int i = 0; i < l2; i++) {
      str2 += strArr2[i];
    }

    if ((float) Math.max(l1, l2) / (float) (Math.min(l1, l2)) > 1.3) {
      if (Math.min(l1, l2) < 4) {
        if (str1.indexOf(str2) == 0 || str2.indexOf(str1) == 0) {
          // same beginning
          return true;
        }
        return false;
      }
    }

    return str1.indexOf(str2) >= 0 || str2.indexOf(str1) >= 0;
  }

  static public String substitution(DefaultMutableTreeNode root, Vector result,
      String sent) {
    boolean partialSubstitution = false;
    int sentID = -1;
    if (sent != null) {
      partialSubstitution = true;
    }
    // Build sentArray
    String aSentStr;
    @SuppressWarnings("rawtypes")
    Enumeration sentences = root.children();
    String[][] sentArray = new String[root.getChildCount()][];
    int i = 0;
    while (sentences.hasMoreElements()) {
      aSentStr = ((TagWord) ((DefaultMutableTreeNode) sentences.nextElement()).
          getUserObject()).getContent();
      if (partialSubstitution) {
        String spli = "[^a-zA-Z]+";
        if ((sentID == -1)
            && arrayMatch(aSentStr.split(spli), sent.split(spli))) {
          sentID = i;
        }
      }
      sentArray[i++] = (aSentStr).toString().split(" ");
    }

    // substitution
    Iterator iterator = result.iterator();

    while (iterator.hasNext()) {

      CorreferencialPair cp = (CorreferencialPair) iterator.next();

      if (cp.referee == null) {
        continue;
      }

      // ignore those referencial cycle ( A<--B<--C<--A )
      if (cp.referee.toStringBrief().equals(cp.referer.toStringBrief())) {
        continue;
      }

      int refererSIdx = cp.referer.getSentenceIdx();

      String str = cp.referee.getSubstitutedContent();
      if (cp.referer.getTag().equalsIgnoreCase("PRP$")) {
        if (cp.referer.getNumber() == 2) {
          str += "'";
        }
        else {
          str += "'s";
        }
      }

      try {
        if (System.getProperty("mode").equals("TagPresent")) {
          sentArray[refererSIdx][cp.referer.getOffset()] = "<" + str + ">";
        }
        else {
          sentArray[refererSIdx][cp.referer.getOffset()] = str;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
        System.exit(0);
      }

    }

    String substitutedOutput = " ";

    if (partialSubstitution) {
      if (sentID != -1) {
        for (int j = 0; j < sentArray[sentID].length; j++) {
          substitutedOutput += sentArray[sentID][j].toString() + " ";
        }

      }
      else {
        substitutedOutput = "**********sentence not found";
      }
    }
    else {
      for (i = 0; i < sentArray.length; i++) {
        for (int j = 0; j < sentArray[i].length; j++) {
          String item = sentArray[i][j].toString();
          if (item.equals(",")
              || item.equals(";")
              || item.equals(".")
              || item.equals("!")
              || item.equals("?")
              || item.equals(")")) {
            substitutedOutput = substitutedOutput.trim();
          }
          if (substitutedOutput.endsWith("( ")) {
            substitutedOutput = substitutedOutput.trim();
          }

          substitutedOutput += item + " ";

        }
        substitutedOutput += "\n";
      }
    }

    return substitutedOutput;
  }

  static public String parseSingleSentnce(String sent) {
    String output = null;
    String tmpIn = System.getProperty("tmpDir") + File.separator +
        "Charniak.in";
    String tmpOut = System.getProperty("tmpDir") + File.separator +
        "Charniak.out";
    try {
      // write the sent to a tmpfile
      Util.write(tmpIn, "<s> " + sent + " </s>");
      // parse it and store it in another tmpfile
      Util.parse(System.getProperty("parserHomeDir") + File.separator +
          "parseIt",
          System.getProperty("parserHomeDir") + File.separator + "DATA/",
          tmpIn,
          tmpOut);
      // retrive the parse result
      output = Util.read(tmpOut).toString();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return output;
  }

  static public String parseSingleArticle(String sent, boolean delimited) {
    String output = null;
    String tmpIn = System.getProperty("tmpDir") + File.separator +
        "Charniak.in";
    String tmpOut = System.getProperty("tmpDir") + File.separator +
        sent.substring(4, 20);
    if (!delimited) {
      // split the sentences first

    }
    else {
      try {
        // write the sent to a tmpfile
        Util.write(tmpIn, sent);
        // parse it and store it in another tmpfile
        Util.parse(System.getProperty("parserHomeDir") + File.separator +
            "parseIt",
            System.getProperty("parserHomeDir") + File.separator +
                "DATA/",
            tmpIn,
            tmpOut);
        // retrive the parse result

        output = Util.read(tmpOut).toString();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
    return output;
  }

  static public String substitutionV1(DefaultMutableTreeNode root,
      Vector result) {
    return substitution(root, result, null);
  }

  /**
   * * @param root
   * 
   * @param result
   * @param sent
   * @return The sentence after substitution.
   */
  static public String substitutionSent(DefaultMutableTreeNode root,
      Vector result, String sent) {
    return substitution(root, result, sent);
  }

  /**
   *
   * @param offset word index inside a sentence
   * @return char index of the word's first char inside the sentence
   */
  static private int findOffset(String str, int offset) {
    int i = 0;
    int loc = -1;
    String tmp = str;

    while (i < offset) {

      loc = tmp.indexOf(" ", loc + 1);
      // tmp = tmp.substring(loc + 1);
      i++;
    }

    return loc + 1;
  }

  static public boolean contains(String[] pack, String item) {

    for (int i = 0; i < pack.length; i++) {
      if (pack[i].equals(item)) {
        return true;
      }
    }
    return false;
  }

  static public String checkFile(String dir, String suffix) {

    String fileName = null;
    try {
      File[] inputFile = new File(dir).listFiles();
      if (inputFile != null) {
        for (int i = 0; i < inputFile.length; i++) {

          if (inputFile[i].getName().endsWith("." + suffix)) {
            fileName = inputFile[i].getCanonicalPath();
            break;
          }
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      return fileName;
    }
  }

  static public void unlimitMemory() {
    try {
      String[] cmd = {
          "/bin/sh",
          "-c",
          "ulimit -s unlimited" };
      Process proc = Runtime.getRuntime().exec(cmd);
      proc.waitFor();

    } catch (Exception e) {
      System.err.println("Wrong");
    }
  }

}
