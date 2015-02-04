package edu.nus.comp.nlp.tool.anaphoraresolution;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;

public class TreeAdapter {

  private DefaultMutableTreeNode tnRoot;
  private Tree tRoot;
  private int sentenceIndex;

  public TreeAdapter(Tree root, int sentenceIndex) {
    this.tRoot = root;
    this.sentenceIndex = sentenceIndex;
    tnRoot = convertHelper(tRoot);
    computeWordIndex(tnRoot);
  }

  private DefaultMutableTreeNode convertHelper(Tree t) {
    if (t.isPreTerminal()) {
      String tag = ((CoreLabel) t.label()).value();
      String word = ((CoreLabel) t.firstChild().label()).value();
      return new DefaultMutableTreeNode(new TagWord(tag, word, sentenceIndex,
          -1));
    }

    String tag = ((CoreLabel) t.label()).value();
    checkNotNull(tag);
    StringBuffer word = new StringBuffer();
    for (Tree leaf : t.getLeaves()) {
      word.append(((CoreLabel) leaf.label()).value()).append(' ');
    }
    DefaultMutableTreeNode tn = new DefaultMutableTreeNode(new TagWord(tag,
        word.toString().trim(), sentenceIndex, -1));
    for (Tree c : t.children()) {
      tn.add(convertHelper(c));
    }
    return tn;
  }
  
  private void computeWordIndex(DefaultMutableTreeNode t) {
    String rootTag = Utils.getTag(t);
    checkArgument(
        rootTag.equalsIgnoreCase("S1"),
        "shouldn't assign offset to sentence not starting with S1: %s", t);

    int offset = 0; // Syntactic unit index, zero based
    @SuppressWarnings("rawtypes")
    Enumeration enumeration = t.postorderEnumeration();
    while (enumeration.hasMoreElements()) {
      DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumeration
          .
          nextElement();
      TagWord tw = Utils.getTagWord(currentNode);
      if (currentNode.isLeaf()) {
        tw.setWordIndex(offset++);
      } else {
        TagWord firstChildtw = Utils.getTagWord(currentNode.getFirstChild());
        tw.setWordIndex(firstChildtw.getWordIndex());
      }
    }
  }

  public Tree getTree() {
    return tRoot;
  }

  public DefaultMutableTreeNode getDefaultMutableTreeNode() {
    return tnRoot;
  }

  public static class TagWordAnnotation implements CoreAnnotation<TagWord> {

    public Class<TagWord> getType() {
      return TagWord.class;
    }
  }
}
