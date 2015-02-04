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

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

import edu.stanford.nlp.trees.Tree;

/**
 * @author Qiu Long
 * @version 1.0
 * @author "Yifan Peng"
 */

public class AnnotatedText {

  public static AnnotatedText parseAnnotatedText(List<String> sentences) {
    return new AnnotatedText(sentences);
  }

  public static AnnotatedText parseAnnotatedText(String s) {
    List<String> sents = Lists.newArrayList();
    String[] sentenceList = s.split("\\(S1 ");
    for (int i = 0; i < sentenceList.length; i++) {
      String sentence = sentenceList[i];
      if (!sentence.trim().isEmpty()) {
        sents.add("(S1 " + sentence.trim());
      }
    }
    return new AnnotatedText(sents);
  }

  // DefaultMutableTreeNode instance inside.overlapping allowed
  private List<TagWord> NPList;
  // DefaultMutableTreeNode instance inside.overlapping disallowed
  private List<TagWord> SNPList;
  // DefaultMutableTreeNode instance inside.overlapping disallowed
  private List<TagWord> PRPList;

  private List<List<TagWord>> globalList;

  private DefaultMutableTreeNode rootNode;

  private AnnotatedText(List<String> sentences) {
    globalList = Lists.newArrayList();
    rootNode = new DefaultMutableTreeNode();
    for (int i = 0; i < sentences.size(); i++) {
      String sentence = sentences.get(i);
      TreeAdapter adpater = new TreeAdapter(Tree.valueOf(sentence), i);
      DefaultMutableTreeNode tn = adpater.getDefaultMutableTreeNode();
      rootNode.add(tn);
      // leaves
      List<TagWord> tags = Lists.newArrayList();
      @SuppressWarnings("rawtypes")
      Enumeration e = tn.preorderEnumeration();
      while (e.hasMoreElements()) {
        TreeNode t = (TreeNode) e.nextElement();
        if (t.isLeaf()) {
          tags.add(Utils.getTagWord(t));
        }
      }
      globalList.add(tags);
    }

    // rootNode = buildParseTree(sents);
    NPExtractor ex = new NPExtractor(rootNode);
    ex.extractNP();
    NPList = ex.getNPList();
    PRPList = ex.getPRPList();

    identifyPleonasticPronoun(rootNode);
    SNPList = buildSNPList(NPList);
  }

  private List<TagWord> buildSNPList(List<TagWord> npList) {
    if (npList.isEmpty()) {
      return Collections.emptyList();
    }
    TagWord sTW = npList.get(0);
    List<TagWord> snpList = Lists.newArrayList(sTW);
    for (int i = 1; i < npList.size(); i++) {
      TagWord tw = npList.get(i);
      if (!sTW.getNP().contains(tw.getNP())) {
        sTW = tw;
        snpList.add(sTW);
      }
    }
    return snpList;
  }

  public List<TagWord> getNPList() {
    return NPList;
  }

  public List<TagWord> getPRPList() {
    return PRPList;
  }

  public List<TagWord> getSNPList() {
    return SNPList;
  }

  public DefaultMutableTreeNode getTree() {
    return rootNode;
  }

  private void identifyPleonasticPronoun(DefaultMutableTreeNode root) {
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = root.preorderEnumeration();

    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode parentNode = null;
      DefaultMutableTreeNode NPnode = null;
      DefaultMutableTreeNode siblingNode = null;
      DefaultMutableTreeNode PrevSiblingNode = null;
      DefaultMutableTreeNode nephewNode1 = null;
      DefaultMutableTreeNode nephewNode2 = null;
      DefaultMutableTreeNode nephewNode3 = null;
      boolean isPleonastic = false;

      DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.
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
          isPleonastic |= ModalAdj.findAny(Utils.getText(adjpNode).split(" "));
        }

        // really appreciate it
        if ((PrevSiblingNode != null)
            && (((TagWord) PrevSiblingNode.getUserObject()).getTag()
                .startsWith("VB"))) {
          isPleonastic |= ModalAdj.findAny(Utils.getText(PrevSiblingNode)
              .split(
                  " "));
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
              isPleonastic |= ModalAdj.findAny(Utils.getText(nephewNode2)
                  .split(
                      " "));
            }
          }
        }

        DefaultMutableTreeNode uncleNode = (DefaultMutableTreeNode) parentNode
            .getPreviousSibling();
        // I will/could appreciate/ believe it
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("VB"))
            && (uncleNode != null)
            && (((TagWord) uncleNode.getUserObject()).getTag()
                .equalsIgnoreCase("MD"))) {
          isPleonastic |= ModalAdj
              .findAny(Utils.getText(siblingNode).split(" "));
        }

        // find it important
        if ((siblingNode != null)
            && (((TagWord) siblingNode.getUserObject()).getTag()
                .equalsIgnoreCase("ADJP"))) {
          isPleonastic |= ModalAdj
              .findAny(Utils.getText(siblingNode).split(" "));
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
          isPleonastic |= ModalAdj
              .findAny(Utils.getText(nephewNode2).split(" "));
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

          String word = ((TagWord) nephewNode1.getUserObject()).getText();
          isPleonastic |= ModalAdj.find(word);
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

          String[] words = Utils.getTagWord(nephewNode2).getText().split(" ");
          isPleonastic |= ModalAdj.find(words[0]);
        }

        tagWd.setPleonastic(isPleonastic);
        // set parent NP as pleonastic also
        ((TagWord) NPnode.getUserObject()).setPleonastic(isPleonastic);

      } // if it's (PRP it)
    } // /~while

  }

}
