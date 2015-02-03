/**
 * JavaRAP: a freely-available JAVA anaphora resolution implementation of the
 * classic Lappin and Leass (1994) paper:
 * 
 * An Algorithm for Pronominal Anaphora Resolution. Computational Linguistics,
 * 20(4), pp. 535-561.
 * 
 * Copyright (C) 2005 Long Qiu
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.google.common.collect.Lists;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

public class AnnotatedText {

  // DefaultMutableTreeNode instance inside.overlapping allowed
  private List<TagWord> NPList = Lists.newArrayList();
  // DefaultMutableTreeNode instance inside.overlapping disallowed
  private List<TagWord> SNPList = Lists.newArrayList();
  // DefaultMutableTreeNode instance inside.overlapping disallowed
  private List<TagWord> PRPList = Lists.newArrayList();

  private List<String> sents;
  private List<List<TagWord>> globalList;
  private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

  public AnnotatedText(String parseText) {
    checkNotNull(parseText, "Input text is null");
    segment(parseText);
    buildParseTree(sents);
    buildNPList();
    identifyPleonasticPronoun(rootNode);
    buildSNPList();
  }

  private void segment(String text) {
    globalList = Lists.newArrayList();
    sents = Lists.newArrayList();
    String[] sentenceList = text.split("\\(S1 ");
    for (int i = 0; i < sentenceList.length; i++) {
      String sentence = sentenceList[i];
      if (!sentence.trim().isEmpty()) {
        sents.add("(S1 " + sentence.trim());
        globalList.add(Utils.analyseTagWordPairs(sentence, i));
      }
    }
  }

  public List<TagWord> getNPList() {
    if (NPList.size() == 0) {
      System.err
          .println("NPList empty. Ensure that the parser is working. If it is, run segment() first. ");
    }
    return NPList;
  }

  public List<TagWord> getPRPList() {
    return PRPList;
  }

  private void identifyPleonasticPronoun(DefaultMutableTreeNode root) {
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = root.preorderEnumeration();

    DefaultMutableTreeNode parentNode = null;
    DefaultMutableTreeNode uncleNode = null;
    DefaultMutableTreeNode node = null;
    DefaultMutableTreeNode NPnode = null;
    DefaultMutableTreeNode siblingNode = null;
    DefaultMutableTreeNode PrevSiblingNode = null;
    DefaultMutableTreeNode nephewNode1 = null;
    DefaultMutableTreeNode nephewNode2 = null;
    DefaultMutableTreeNode nephewNode3 = null;
    boolean isPleonastic = false;

    while (enumeration.hasMoreElements()) {

      node = (DefaultMutableTreeNode) enumeration.
          nextElement();
      TagWord tagWd = (TagWord) (node.getUserObject());
      if (tagWd == null) {
        continue;
      }

      if (tagWd.getTag().equalsIgnoreCase("PRP")
          && tagWd.getText().equalsIgnoreCase("it")) {
        isPleonastic = false;

        NPnode = (DefaultMutableTreeNode) node.getParent();
        checkNotNull(NPnode, "Weird: (PRP it) has no parent");

        parentNode = (DefaultMutableTreeNode) NPnode.getParent();
        checkNotNull(parentNode, "Weird: (PRP it) has no grandparent");

        uncleNode = (DefaultMutableTreeNode) parentNode.getPreviousSibling();
        siblingNode = (DefaultMutableTreeNode) NPnode.getNextSibling();
        if ((siblingNode != null) && (siblingNode.getChildCount() > 0)) {

          nephewNode1 = (DefaultMutableTreeNode) siblingNode.getChildAt(0);
          nephewNode2 = (DefaultMutableTreeNode) nephewNode1.getNextSibling();
          if (nephewNode2 != null) {
            nephewNode3 = (DefaultMutableTreeNode) nephewNode2.getNextSibling();
          }
        }

        PrevSiblingNode = (DefaultMutableTreeNode) NPnode.getPreviousSibling();

        // identify pleonastic pronouns

        // It is very necessary
        // It is recommended that
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))
            && (nephewNode1 != null)
            && (((TagWord) nephewNode1.getUserObject()).getTag()
                .equalsIgnoreCase("AUX"))
            && (nephewNode2 != null)
            &&
            ((((TagWord) nephewNode2.getUserObject()).getTag()
                .equalsIgnoreCase("ADJP"))
            || ((nephewNode3 != null) && (((TagWord) nephewNode3
                .getUserObject()).getTag().equalsIgnoreCase("ADJP")))
            )) {
          DefaultMutableTreeNode adjpNode = (((TagWord) nephewNode2
              .getUserObject()).getTag().equalsIgnoreCase("ADJP")) ? nephewNode2
              : nephewNode3;
          String[] words = ((TagWord) adjpNode.getUserObject()).getContent()
              .split(" ");

          for (int i = 0; i < words.length; i++) {
            if (ModalAdj.contains(words[i])) {
              isPleonastic = true;
              break; // if
            }
          }
        }

        // really appreciate it
        if ((PrevSiblingNode != null)
            && (((TagWord) PrevSiblingNode.getUserObject()).getTag()
                .startsWith("VB"))) {
          String[] words = ((TagWord) PrevSiblingNode.getUserObject())
              .getContent().split(" ");

          for (int i = 0; i < words.length; i++) {
            if (ModalAdj.contains(words[i])) {
              isPleonastic = true;
              break; // if
            }
          }
        }

        // it may/might be
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))
            && (nephewNode1 != null)
            && (((TagWord) nephewNode1.getUserObject()).getTag()
                .equalsIgnoreCase("MD"))
            && (nephewNode2 != null)
            && (((TagWord) nephewNode2.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))) {

          if (nephewNode2.getChildCount() > 1) {
            DefaultMutableTreeNode subNode1 = (DefaultMutableTreeNode) nephewNode2
                .getChildAt(0);
            DefaultMutableTreeNode subNode2 = (DefaultMutableTreeNode) nephewNode2
                .getChildAt(1);
            if (((TagWord) subNode1.getUserObject()).getTag().equalsIgnoreCase(
                "AUX")
                && ((TagWord) subNode2.getUserObject()).getTag()
                    .equalsIgnoreCase("ADJP")) {

              String[] words = ((TagWord) nephewNode2.getUserObject()).
                  getContent().split(" ");

              for (int i = 0; i < words.length; i++) {
                if (ModalAdj.contains(words[i])) {
                  isPleonastic = true;
                  break; // if
                }
              }
            }
          }
        }

        // I will/could appreciate/ believe it
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VB"))
            && (uncleNode != null)
            && (((TagWord) uncleNode.getUserObject()).getTag()
                .equalsIgnoreCase("MD"))) {

          String[] words = ((TagWord) siblingNode.getUserObject()).getContent()
              .split(" ");

          for (int i = 0; i < words.length; i++) {
            if (ModalAdj.contains(words[i])) {
              isPleonastic = true;
              break; // if
            }
          }
        }

        // find it important
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("ADJP"))) {
          String[] words = ((TagWord) siblingNode.getUserObject()).getContent()
              .split(" ");

          for (int i = 0; i < words.length; i++) {
            if (ModalAdj.contains(words[i])) {
              isPleonastic = true;
              break; // if
            }
          }
        }

        // it is thanks to
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))
            && (nephewNode1 != null)
            && (((TagWord) nephewNode1.getUserObject()).getTag()
                .equalsIgnoreCase("AUX"))
            && (nephewNode2 != null)
            && (((TagWord) nephewNode2.getUserObject()).getTag()
                .equalsIgnoreCase("NP"))) {
          String[] words = ((TagWord) nephewNode2.getUserObject()).getContent()
              .split(" ");

          for (int i = 0; i < words.length; i++) {
            if (ModalAdj.contains(words[i])) {
              isPleonastic = true;
              break; // if
            }
          }
        }

        // it follows that
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))
            && (nephewNode1 != null)
            && (((TagWord) nephewNode1.getUserObject()).getTag()
                .startsWith("VB"))
            && (nephewNode2 != null)
            && (((TagWord) nephewNode2.getUserObject()).getTag()
                .startsWith("S"))) {

          String word = ((TagWord) nephewNode1.getUserObject()).getContent();
          if (ModalAdj.contains(word)) {
            isPleonastic = true;
          }
        }

        // it is time to
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP"))
            && (nephewNode1 != null)
            && (((TagWord) nephewNode1.getUserObject()).getTag()
                .equalsIgnoreCase("AUX"))
            && (nephewNode2 != null)
            && (((TagWord) nephewNode2.getUserObject()).getTag()
                .equalsIgnoreCase("NP"))) {

          String[] words = ((TagWord) nephewNode2.getUserObject()).getContent()
              .split(" ");
          if (ModalAdj.contains(words[0])) {
            isPleonastic = true;
          }
        }

        tagWd.setPleonastic(isPleonastic);
        // set parent NP as pleonastic also
        ((TagWord) NPnode.getUserObject()).setPleonastic(isPleonastic);

      } // if it's (PRP it)
    } // /~while

  }

  private List<TagWord> extractNP() {
    List<TagWord> NPVec = Lists.newArrayList();
    @SuppressWarnings("rawtypes")
    // refer to 'Need preorder here.' line
    Enumeration enumeration = rootNode.preorderEnumeration();

    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.
          nextElement();
      TagWord tagWd = (TagWord) (node.getUserObject());

      if (tagWd != null) {
        if (tagWd.getTag().startsWith("N")
            || tagWd.getTag().startsWith("PRP")) {
          NP aNP = new NP(tagWd.getSentenceIndex(), tagWd.getWordIndex(),
              "(" + tagWd.getTag() + " " + tagWd.getWord() + ")");

          DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node.
              getParent();
          String parentTag = ((TagWord) (parentNode).getUserObject()).getTag();

          /**********************************************************/
          // set 'Subject' true if it's a child of a unit tagged as "S"

          if (parentTag.equals("S")) {
            aNP.setSubject(true);
          }

          /**********************************************************/
          // set 'existential' true if it's a object of a VP,
          // which in turn follows a NP(subject?) who has a single child
          // tagging as 'EX'
          if (parentTag.equalsIgnoreCase("VP")) {
            if (parentNode.getIndex(node) == 1) {
              // object (second child) of a VP
              DefaultMutableTreeNode parentSibNode =
                  (DefaultMutableTreeNode) parentNode.getPreviousSibling();
              if ((parentSibNode != null) &&
                  (((TagWord) parentSibNode.getUserObject()).getTag().
                      equalsIgnoreCase("NP"))) {
                // preceeding sibling is a NP
                if (((TagWord) ((DefaultMutableTreeNode) parentSibNode.
                    getChildAt(0)).getUserObject()).getTag().
                    equalsIgnoreCase("EX")) {
                  aNP.setExistential(true);
                }
              }
            }
          }

          /**********************************************************/
          // set 'directObj' true if it's the only NP child of a VP, or second
          // NP of a VP
          // whileas the first NP is 'indirectObj'
          /*
           * Handles give him the book kick him NOT give the book to HER
           */
          /**
           * To be a stand alone function
           */
          if (parentTag.equalsIgnoreCase("VP")) {
            if (parentNode.getIndex(node) == 1) {

              // object (second child) of a VP
              DefaultMutableTreeNode nextSib = (DefaultMutableTreeNode) node.
                  getNextSibling();

              if ((nextSib != null) &&
                  ((TagWord) nextSib.getUserObject()).getTag().
                      equalsIgnoreCase("NP")) {
                // eg.give HIM a book
                aNP.setIndirectObj(true);
              }
              else {
                // eg. kick him
                aNP.setDirectObj(true);
              }

            }
            else {
              // not the first NP child of the VP
              aNP.setDirectObj(true);
            }
          }

          /**********************************************************/
          // set head
          // for a NP, set the rightmost N* as the head,even it's not a leaf
          DefaultMutableTreeNode h = findFirstChildNode(node, "N", -1, true); // for
                                                                              // 'NP'
          if (h == null) {
            h = findFirstChildNode(node, "PRP", -1); // for "PRP"
          }
          if (h != null) {
            tagWd.setHead(h);
          }
          // reselect head for people
          //
          if (node.getChildCount() > 1) {
            boolean allNNP = true;
            @SuppressWarnings("rawtypes")
            Enumeration emu = node.children();
            while (emu.hasMoreElements()) {
              DefaultMutableTreeNode hPeople = (DefaultMutableTreeNode) emu
                  .nextElement();
              TagWord hPTw = (TagWord) hPeople.getUserObject();
              if (!(hPTw).getTag().equalsIgnoreCase("NNP")) {
                allNNP = false;
                break;
              }
            }

            while (allNNP && emu.hasMoreElements()) {
              DefaultMutableTreeNode hPeople = (DefaultMutableTreeNode) emu
                  .nextElement();
              TagWord hPTw = (TagWord) hPeople.getUserObject();
              // lable the first name, if there is one, as the head of a NP
              // representing a people
              if (HumanList.isFemale(hPTw.getText())
                  || HumanList.isMale(hPTw.getText())) {
                tagWd.setHead(hPeople);
                break;
              }
            }
          }

          /**********************************************************/
          // Connect NP with its argumentHost, i.e., the NP in the same
          // argument domain
          // We consider NP and the NP in it's sibling VP. Lets say
          // case 1 NP, find the NP contained in it's sibling VP
          DefaultMutableTreeNode argH = findFirstChildNode(parentNode, "VP", 1);
          if (argH != null) {
            tagWd.setArgumentHead(argH);
            while (((TagWord) argH.getUserObject()).getTag().equalsIgnoreCase(
                "VP")) {
              DefaultMutableTreeNode tmp = findFirstChildNode(argH, "VP", 1);
              if (tmp == null) {
                break;
              }
              argH = tmp;
            }
            argH = findFirstChildNode(argH, "NP", 1);

            if (argH != null) {
              tagWd.setArgumentHost(argH);
            }
          }
          // case 2 NP under a VP, find the sibling NP of the VP
          if (parentTag.startsWith("VP")) {
            DefaultMutableTreeNode upperNode = parentNode;
            while (((TagWord) upperNode.getUserObject()).getTag()
                .equalsIgnoreCase("VP")
                || (((TagWord) upperNode.getUserObject()).getTag()
                    .equalsIgnoreCase("S")
                && (upperNode.getChildCount() < 2))) {
              upperNode = (DefaultMutableTreeNode) upperNode.getParent();
            }
            argH = findFirstChildNode(upperNode, "NP", 1);
            if (argH != null) {
              tagWd.setArgumentHost(argH);
              tagWd.setArgumentHead(parentNode);
            }
          }

          /**********************************************************/
          // connect with adjunctHost : the NP whose adjunct domain is NP in
          if (parentTag.startsWith("PP")) {
            DefaultMutableTreeNode grandParentNode = (DefaultMutableTreeNode) parentNode
                .getParent();
            if (grandParentNode.getUserObject() != null) {
              if (((TagWord) grandParentNode.getUserObject()).getTag()
                  .startsWith("VP")) {
                argH = findFirstChildNode(
                    (DefaultMutableTreeNode) grandParentNode.
                        getParent(),
                    "NP",
                    1);
                if (argH != null) {
                  tagWd.setAdjunctHost(argH);
                }
              }
            }
          }

          /**********************************************************/
          // connect with containHost: the NP containing this NP
          DefaultMutableTreeNode argadjNode = null;
          if ((argadjNode = tagWd.getArgumentHead()) != null) {
            tagWd.setContainHost(argadjNode);
          } else {
            // deepest ancestorNode NP and its containHosts and the deepest VP
            // ancestorNode; Need preorder here.

            DefaultMutableTreeNode pNode = parentNode;
            boolean gotNP = false;
            boolean gotVP = false;
            while (pNode != null) {
              if (pNode.getUserObject() == null) {
                break;
              }
              if ((!gotNP)
                  && ((TagWord) pNode.getUserObject()).getTag()
                      .startsWith("NP")) {
                tagWd.setContainHost(((TagWord) pNode.getUserObject())
                    .getContainHost());
              } else if ((!gotVP)
                  && ((TagWord) pNode.getUserObject()).getTag()
                      .startsWith("VP")) {
                tagWd.setContainHost(pNode);
              }
              if (gotNP && gotVP) {
                break;
              }
              pNode = (DefaultMutableTreeNode) pNode.getParent();
            }

          }

          /**********************************************************/
          // connect with NPDomainHost: the NP whose domain this NP is in
          if (parentTag.startsWith("PP")) {
            DefaultMutableTreeNode previousSiblingNodeOfParent = parentNode
                .getPreviousSibling();

            if (previousSiblingNodeOfParent != null
                && previousSiblingNodeOfParent.getUserObject() != null) {
              if (((TagWord) previousSiblingNodeOfParent.getUserObject())
                  .getTag().startsWith("NP")) {
                if (previousSiblingNodeOfParent.getChildCount() > 1) {
                  DefaultMutableTreeNode firstCousin = (DefaultMutableTreeNode) previousSiblingNodeOfParent
                      .getChildAt(0);
                  DefaultMutableTreeNode secondCousin = (DefaultMutableTreeNode) previousSiblingNodeOfParent
                      .getChildAt(1);
                  if (((TagWord) firstCousin.getUserObject()).getTag()
                      .startsWith("NP")
                      && ((TagWord) secondCousin.getUserObject()).getTag()
                          .startsWith("N")) {
                    if (!firstCousin.isLeaf()) {
                      DefaultMutableTreeNode posNode = findFirstChildNode(
                          firstCousin,
                          "POS",
                          1);
                      if (posNode != null) {
                        tagWd.setNPDomainHost((DefaultMutableTreeNode) posNode
                            .getPreviousSibling());
                      }
                    }
                  }
                }

              }
            }
          }

          /**********************************************************/
          // connect with third person Determiner (PRP$): the PRP$ sibling of
          // this NP
          @SuppressWarnings("rawtypes")
          Enumeration siblingNodes = parentNode.children();
          while (siblingNodes.hasMoreElements()) {
            DefaultMutableTreeNode siblingNode = (DefaultMutableTreeNode) siblingNodes
                .nextElement();
            if (siblingNode == node) {
              // Finish scanning the leading siblings
              break;
            } else if (((TagWord) siblingNode.getUserObject()).getTag().equals(
                "PRP$")) {
              tagWd.setDeterminer(siblingNode);
              ((TagWord) siblingNode.getUserObject()).setDeterminee(node);
              break;
            }
          }

          /**********************************************************/
          // If the tag starts with "NN" (NN NNS NNP NNPS), detect whether it
          // has a sibling with similar tag
          if (tagWd.getTag().startsWith("NN")) {
            DefaultMutableTreeNode siblingNode = node.getPreviousSibling();
            if (siblingNode != null) {
              if (((TagWord) siblingNode.getUserObject()).getTag().startsWith(
                  "NN")) {
                aNP.setHasNNXsibling(true);
              }
            }

            siblingNode = node.getNextSibling();
            if (siblingNode != null) {
              if (((TagWord) siblingNode.getUserObject()).getTag().startsWith(
                  "NN")) {
                aNP.setHasNNXsibling(true);
              }
            }
          }

          /**********************************************************/
          // First try to set number of the NP, based on the head of the VP
          // related
          DefaultMutableTreeNode siblingVPNode = findFirstChildNode(
              parentNode,
              "VP",
              1);
          if (siblingVPNode != null) {
            DefaultMutableTreeNode vpHead = ((TagWord) siblingVPNode
                .getUserObject()).getHead();
            if (vpHead != null) {
              String siblingVPHeadTw = ((TagWord) vpHead.getUserObject())
                  .getTag();
              if (siblingVPHeadTw.startsWith("AUX")) {

              } else if (siblingVPHeadTw.endsWith("VBP")) {
                tagWd.setNumber(Number.PLURAL);
              } else if (siblingVPHeadTw.endsWith("VBZ")) {
                tagWd.setNumber(Number.SINGLE);
              }
            }
          }

          // check the existance of potential ancestor NP (to decide whether
          // it's a head)
          boolean hasNPAncestor = hasAncestor(node, "NP");
          tagWd.setHasNPAncestor(hasNPAncestor);

          boolean hasADVPAncestor = hasAncestor(node, "ADVP");
          aNP.setIsInADVP(hasADVPAncestor);

          aNP.setNodeRepresent(node);
          aNP.setHead(tagWd.isHeadNP());
          tagWd.setNP(aNP);

          if (tagWd.getTag().startsWith("PRP")) {
            // for (NP (PRP xxx))
            // copy all the attributes from its parent node while has no
            // siblings
            if (parentTag.startsWith("NP")
                && node.getSiblingCount() == 1) {
              TagWord parentTw = (TagWord) parentNode.getUserObject();

              tagWd.setAdjunctHost(parentTw.getAdjunctHost());
              tagWd.setArgumentHead(parentTw.getArgumentHead());
              tagWd.setArgumentHost(parentTw.getArgumentHost());
              tagWd.setContainHost(parentTw.getContainHost());
              tagWd.setContainHost(parentNode);
              tagWd.setNPDomainHost(parentTw.getNPDomainHost());
            }

            PRPList.add(tagWd);
          }

          if (!tagWd.getTag().equalsIgnoreCase("PRP")) {
            // Ignore PRP, since all of them appear in (NP (PRP xxx)).
            // Rewrite here if the assumption is false.
            NPVec.add(tagWd);
          }

        }
        else if (tagWd.getTag().equalsIgnoreCase("PP")) {

          // set head
          DefaultMutableTreeNode h = findFirstChildNode(node, "N", -1); // for
                                                                        // 'NP'

          if (h == null) {
            h = findFirstChildNode(node, "PR", -1); // for "PRP"
          }

          if (h != null) {
            tagWd.setHead(h);
          }

        }
        else if (tagWd.getTag().equalsIgnoreCase("VP")) {
          // set head
          DefaultMutableTreeNode h = findFirstChildNode(node, "V", 1); // for
                                                                       // 'V'

          if (h == null) {
            h = findFirstChildNode(node, "AU", 1); // for "PRP"
          }

          if (h != null) {
            tagWd.setHead(h);
          }
        }

      }
    }// while

    NPList.addAll(NPVec);
    return NPVec;
  }

  private void buildNPList() {
    extractNP();
  }

  private boolean hasAncestor(DefaultMutableTreeNode node, String tag) {
    boolean hasAncestor = false;
    DefaultMutableTreeNode ancNode = node;
    while (ancNode.getParent() != null) {
      ancNode = (DefaultMutableTreeNode) ancNode.getParent();
      if (ancNode.getUserObject() != null) {
        TagWord ancTw = (TagWord) ancNode.getUserObject();
        if (ancTw.getTag().equals(tag)) {
          hasAncestor = true;
          break;
        }
      }
    }
    return hasAncestor;
  }

  private void buildSNPList() {
    // bug cannot handel single NP
    Iterator<TagWord> iterator = NPList.listIterator();
    if (!iterator.hasNext()) {
      return;
    }
    TagWord sTW = (TagWord) iterator.next();
    TagWord tw;

    SNPList.add(sTW);
    while (iterator.hasNext()) {
      tw = (TagWord) iterator.next();
      if (sTW.getNP().contains(tw.getNP())) {
        continue;
      }
      else {
        sTW = tw;
        if (sTW.getNP().isPRP()) {
          // exclude PRP
          // continue;
        }
        SNPList.add(sTW);
      }
    }
  }

  public List<TagWord> getSNPList() {
    return SNPList;
  }

  private void buildParseTree(List<String> sentences) {
    for (int i = 0; i < sentences.size(); i++) {
      rootNode.add(convertSentenceToTreeNode(
          i,
          sentences.get(i),
          "(",
          ")"));
    }
  }

  /**
   * Convert the output of Charniak parser for ~A single sentence~ into a
   * TreeNode.
   */
  private DefaultMutableTreeNode convertSentenceToTreeNode(
      int sentenceIndex, String annotedText, String delimL, String delimR) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
    int endPos = findMatcher(annotedText, delimL, delimR);

    if (endPos == (annotedText.length() - 1)) {
      node.setUserObject(TagWord.parseTagWord(annotedText, sentenceIndex, -1));
      addChildren(sentenceIndex, node,
          annotedText.substring(annotedText.indexOf(" ") + 1,
              annotedText.length() - 1), delimL,
          delimR);
    }
    else {
      System.err.print("Parsing result error:\n" + annotedText + "\n");
      System.err.print("Parsing result error:\n" + annotedText + "\n");
      return convertSentenceToTreeNode(sentenceIndex,
          "(S1 (FRAG (NP (CD XIE20030000.0000)) (. .)))", delimL, delimR);
    }
    computeOffset(node);
    return node;
  }

  private int findMatcher(String target, String matcheeL, String matcherR) {
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
  
  private void addChildren(int sIdx, DefaultMutableTreeNode parentNode,
      String annotedText, String delimL, String delimR) {
    int leadPos = annotedText.indexOf(delimL);

    if (leadPos == -1) {
      return;
    }
    int endPos = findMatcher(annotedText, delimL, delimR);
    if (endPos == (annotedText.length() - 1)) {
      DefaultMutableTreeNode singleChild = new DefaultMutableTreeNode(
          TagWord.parseTagWord(annotedText, sIdx, -1));
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
          new DefaultMutableTreeNode(TagWord.parseTagWord(
              annotedText.substring(leadPos,
                  endPos + 1),
              sIdx,
              -1));
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

  private void computeOffset(DefaultMutableTreeNode n) {
    String rootTag = null;

    try {
      rootTag = ((TagWord) n.getUserObject()).getTag();
    } catch (Exception ex) {
      System.out.println(n);
      System.exit(0);
    }

    if (!rootTag.equalsIgnoreCase("S1")) {
      // shouldn't assign offset to none-sentence
      System.err.println("//shouldn't assign offset to none-sentence");
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
        tw.setWordIndex(offset++);
      }
      else {
        TagWord tw = (TagWord) currentNode.getUserObject();
        TagWord firstChildtw = (TagWord) (((DefaultMutableTreeNode)
            currentNode.getFirstChild()).
                getUserObject());
        tw.setWordIndex(firstChildtw.getWordIndex());
      }
    }
  }

  public DefaultMutableTreeNode getTree() {
    return rootNode;
  }

  /**
   *
   * @param node
   * @param taghead
   * @param direction: Binary: +1 (from right) or -1 (from left)
   * @return the first child from left(+1)/right(-1) that satisfies taghead
   */
  private DefaultMutableTreeNode findFirstChildNode(
      DefaultMutableTreeNode parentNode, String taghead, int direction) {
    return findFirstChildNode(parentNode, taghead, direction, false);
  }

  /**
   *
   * @param node
   * @param taghead
   * @param direction: Binary: +1 (from right) or -1 (from left)
   * @param recursive: true to do the search recursively until a leaf node
   *          matches the requirement is found
   * @return the first child from left(+1)/right(-1) that satisfies taghead
   */
  private DefaultMutableTreeNode findFirstChildNode(
      DefaultMutableTreeNode parentNode, String taghead, int direction,
      boolean recursive) {

    int size = parentNode.getChildCount();
    if (size == 0) {
      return null;
    }
    for (int i = 0; i < size; i++) {
      int idx = (direction > 0 ? i : size - 1 - i);
      DefaultMutableTreeNode checkee = (DefaultMutableTreeNode) parentNode
          .getChildAt(
          idx);

      TagWord tw = (TagWord) checkee.getUserObject();

      if (tw.getTag().startsWith(taghead)) {
        // Util.errLog(checkee.toString());
        if (recursive && (!checkee.isLeaf())) {
          return findFirstChildNode(checkee, taghead, direction, true);
        } else {
          return checkee;
        }
      }
    }
    return null;
  }

}
