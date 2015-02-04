package edu.nus.comp.nlp.tool.anaphoraresolution;

import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.google.common.collect.Lists;

public class NPExtractor {

  private DefaultMutableTreeNode rootNode;
  private List<TagWord> NPList = Lists.newArrayList();
  private List<TagWord> PRPList = Lists.newArrayList();

  public NPExtractor(DefaultMutableTreeNode rootNode) {
    this.rootNode = rootNode;
    NPList = Lists.newArrayList();
    PRPList = Lists.newArrayList();
  }
  
  public List<TagWord> getNPList() {
    return NPList;
  }
  
  public List<TagWord> getPRPList() {
    return PRPList;
  }

  public void extractNP() {
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
          DefaultMutableTreeNode h = findFirstChildNode(
              node,
              "N",
              -1,
              true); // for
          // 'NP'
          if (h == null) {
            h = findFirstChildNode(node, "PRP", -1, false); // for "PRP"
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
          DefaultMutableTreeNode argH = findFirstChildNode(
              parentNode,
              "VP",
              1,
              false);
          if (argH != null) {
            tagWd.setArgumentHead(argH);
            while (((TagWord) argH.getUserObject()).getTag().equalsIgnoreCase(
                "VP")) {
              DefaultMutableTreeNode tmp = findFirstChildNode(
                  argH,
                  "VP",
                  1,
                  false);
              if (tmp == null) {
                break;
              }
              argH = tmp;
            }
            argH = findFirstChildNode(argH, "NP", 1, false);

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
            argH = findFirstChildNode(upperNode, "NP", 1, false);
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
                    1, false);
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
                          1, false);
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
              1, false);
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
          DefaultMutableTreeNode h = findFirstChildNode(node, "N", -1, false); // for
          // 'NP'

          if (h == null) {
            h = findFirstChildNode(node, "PR", -1, false); // for "PRP"
          }

          if (h != null) {
            tagWd.setHead(h);
          }

        }
        else if (tagWd.getTag().equalsIgnoreCase("VP")) {
          // set head
          DefaultMutableTreeNode h = findFirstChildNode(node, "V", 1, false); // for
          // 'V'

          if (h == null) {
            h = findFirstChildNode(node, "AU", 1, false); // for "PRP"
          }

          if (h != null) {
            tagWd.setHead(h);
          }
        }

      }
    }// while
    NPList.addAll(NPVec);
  }

  private boolean hasAncestor(TreeNode node, String tag) {
    while (node.getParent() != null) {
      node = node.getParent();
      TagWord tw = Utils.getTagWord(node);
      if (tw != null && tw.getTag().equals(tag)) {
        return true;
      }
    }
    return false;
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
      TreeNode parentNode, String taghead, int direction,
      boolean recursive) {
    if (parentNode.isLeaf()) {
      return null;
    }
    int size = parentNode.getChildCount();
    for (int i = 0; i < size; i++) {
      int idx = (direction > 0 ? i : size - 1 - i);
      TreeNode checkee = parentNode.getChildAt(idx);
      TagWord tw = Utils.getTagWord(checkee);
      if (tw.getTag().startsWith(taghead)) {
        if (recursive && !checkee.isLeaf()) {
          return findFirstChildNode(checkee, taghead, direction, true);
        } else {
          return (DefaultMutableTreeNode)checkee;
        }
      }
    }
    return null;
  }
}
