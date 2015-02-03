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

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

// import edu.nus.comp.nlp.gadget.*;
/**
 * @author Qiu Long
 * @version 1.0
 * @history Feb 12, 2006 Make it work on windows. Long Qiu
 * @author "Yifan Peng"
 */

public class AnaphoraResolver {

  public AnaphoraResolver() {
    loadEnv();
  }

  private void loadEnv() {
    // resolver mode
    System.setProperty("referenceChain", "true");
    System.setProperty("mode", "TagPresent");
    System.setProperty("keep log", "false");
    System.setProperty("display log", "true");
    System.setProperty("EvaluationVerbose", "false");
    // Results will be shown as a part of log, if log is displayed. So set this
    // to true only if log is dampened.
    System.setProperty("display resolving results", "false");
    System.setProperty("Substitution", "true");
    System.setProperty("display substitution results", "false");
    System.setProperty("write substitution results", "false");
    System.setProperty("write resolving results", "false");

    // environment
    ClassLoader classLoader = this.getClass().getClassLoader();
    // global
    String dataPath = classLoader.getResource("Data").getFile();
    System.setProperty("dataPath", dataPath);

    // working directory
    File outputDir = Files.createTempDir();
    System.setProperty("outputDir", outputDir.toString());

    File tmpDir = Files.createTempDir();
    System.setProperty("tmpDir", tmpDir.toString());

    System.setProperty("parserOption", " ");
  }

  // private void write(String fileName, String content) {
  // try {
  // FileUtils.write(new File(fileName), content);
  // } catch (IOException e) {
  // e.printStackTrace();
  // }
  // }

  public static List<TagWord> analyseTagWordPairs(String aNP, int sIdx) {
    int pointer = 0; // to indicate position in the string
    int adjPointer = 0; // adjunct pointer
    String tag = null;
    String word;
    if (aNP.length() == 0) {
      return Collections.emptyList();
    }

    List<TagWord> vec = Lists.newArrayList();
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

  public List<CorreferencialPair> resolverV1(List<TagWord> aNPList,
      List<TagWord> aPRPList) {
    // to display
    List<String> results = Lists.newArrayList();
    // for substitution
    List<CorreferencialPair> resultsOut = Lists.newArrayList();
    // How many sentences to look back. /****para****/
    int scope = 1;
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

      // rewind
      npIterator = aNPList.iterator();
      List<TagWord> npCandidates = Lists.newArrayList();
      while (npIterator.hasNext()) {
        TagWord npTw = (TagWord) npIterator.next();
        // skip pleonastic NP, whose only child is pleonastic pronoun 'it'
        if (npTw.isPleonastic()) {
          continue;
        }

        if ((npTw.getSentenceIndex() + scope) < prpTw.getSentenceIndex()) {
          // ignore NP 'scope' sentences ahead
          continue;
        }

        boolean b1 = prpTw == npTw;
        boolean b2 = npTw.getNP().getNodeRepresent().isNodeChild(
            prpTw.getNP().getNodeRepresent());
        boolean b3 = npTw.getNP().getNodeRepresent().
            getChildCount() == 1;
        boolean b4 = (npTw.getNP().getNodeRepresent().
            isNodeDescendant(prpTw.getNP().
                getNodeRepresent()));
        boolean b5 = b1 || (b2 && b3) || b4;

        if (b5) {
          // self reference :)
          // Case 1: (PRP$ xxx) (PRP$ xxx)
          // Case 2: (NP (PRP xxx)) (PRP xxx)
          continue;
        }

        if (npTw.getSentenceIndex() > prpTw.getSentenceIndex()) {
          // only consider anaphora
          break;
        }

        // filtering
        NP prpNP = prpTw.getNP();
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

            // true/undefine by default
            if (System.getProperty("referenceChain").equals("false")) {
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
          NP prpNP = prpTw.getNP();
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
          // true/undefine by default
          if (System.getProperty("referenceChain").equals("false")) {
            resultsOut.add(new CorreferencialPair((TagWord) obj, prpTw));
          } else {
            resultsOut.add(new CorreferencialPair(obj.getAntecedent(), prpTw));
          }
        } else {
          // no candidate is found
          resultsOut.add(new CorreferencialPair(obj, prpTw));
        }
      }

    }
    return resultsOut;
  }

  private TagWord
      getBestCandidate(TagWord[] sortedCandidates, TagWord tw) {
    TagWord obj = null;

    // Check for empty candidate list
    if (sortedCandidates.length == 0) {
      return obj;
    } else if (sortedCandidates.length == 1) {
      return sortedCandidates[0];
    } else { // with more in the list
      TagWord tw0 = (TagWord) sortedCandidates[sortedCandidates.length - 1];
      TagWord tw1 = (TagWord) sortedCandidates[sortedCandidates.length - 2];
      if (tw0.getTmpSalience() > tw1.getTmpSalience()) {
        return tw0;
      }
      else {
        if (tw0.distanceInText(tw) < tw1.distanceInText(tw)) {
          // take closer one
          obj = tw0;
        }
        else if (tw0.getNP().getNodeRepresent().isNodeAncestor(
            tw1.getNP().getNodeRepresent())) {
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
  private boolean matchLexcialAnaphor(TagWord npTw, TagWord lexTw) {
    // Anaphor Binding Algorithm (Lappin and Leass)
    boolean judge = false;
    DefaultMutableTreeNode npNode = npTw.getNP()
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
  private boolean matchPronominalAnaphor(TagWord npTw, TagWord prpTw) {
    // Syntactic Filter (Lappin and Leass)
    boolean judge = true;
    DefaultMutableTreeNode npNode = npTw.getNP()
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
  private boolean morphologicalFilter(TagWord npTw, TagWord prpTw) {
    if (prpTw.getGender() != npTw.getGender()
        && prpTw.getGender() != Gender.UNCLEAR
        && npTw.getGender() != Gender.UNCLEAR) {
      return false;
    } else if (npTw.getNumber() != prpTw.getNumber()
        && npTw.getNumber() != Number.UNCLEAR
        && prpTw.getNumber() != Number.UNCLEAR) {
      return false;
    } else if (npTw.getPronounPeople() != prpTw.getPronounPeople()
        && npTw.getPronounPeople() != People.UNCLEAR
        && prpTw.getPronounPeople() != People.UNCLEAR) {
      // getPronounIdx also assigns the predicate "people" as well
      return false;
    } else if (npTw.getHuman() != prpTw.getHuman()
        && npTw.getHuman() != Human.UNCLEAR
        && prpTw.getHuman() != Human.UNCLEAR) {
      return false;
    } else if (npTw.getPeople() != prpTw.getPeople()
        && npTw.getPeople() != People.UNCLEAR
        && prpTw.getPeople() != People.UNCLEAR) {
      return false;
    } else {
      return true;
    }
  }

  private String processResult(TagWord np, TagWord referer) {
    String refereeStr = null;
    String anaphorStr = null;
    if (np == null) {
      refereeStr = "NULL";
    } else {
      // true/undefined by default
      if (System.getProperty("referenceChain").equals("false")) {
        refereeStr = np.toStringBrief();
      } else {
        // bind to the earliest NP
        refereeStr = np.getAntecedent().toStringBrief();
      }
      // update salience factors for the detected coreferential pair
      np.mergeSalience(referer);
      referer.mergeSalience(np);
    }
    anaphorStr = ((TagWord) referer).toStringBrief();
    return "\n" + refereeStr + " <-- " + anaphorStr;
  }
}
