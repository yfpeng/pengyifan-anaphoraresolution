package com.pengyifan.anaphoraresolution;

import java.util.Vector;

import edu.nus.comp.nlp.tool.anaphoraresolution.AnnotatedText;
import edu.nus.comp.nlp.tool.anaphoraresolution.CorreferencialPair;
import edu.nus.comp.nlp.tool.anaphoraresolution.TagWord;
import edu.nus.comp.nlp.tool.anaphoraresolution.Util;

public class RAPClient {

  public static void main(String[] args) {

    String parseText = "(S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX)) (VP (VBZ is) (ADVP (RB also)) (NP (DT a) (JJ sigmaB-dependent) (JJ general) (NN stress) (NN gene)) (, ,) (SBAR (IN since) (S (NP (PRP it)) (VP (VBZ is) (ADVP (RB strongly)) (VP (VBN induced) (PP (PP (IN by) (NP (NP (NN heat)) (, ,) (NP (NN salt)) (CC and) (NP (NN ethanol) (NN stress)))) (, ,) (CONJP (RB as) (RB well) (IN as)) (PP (IN by) (NP (NN energy) (NN depletion))))))))))))) (. .)))";

    AnnotatedText aText = new AnnotatedText(parseText.toString());
    Vector<CorreferencialPair> vet = Util.resolverV1(
        aText.getNPList(),
        aText.getPRPList());

    for (CorreferencialPair p : vet) {
      TagWord referer = p.getReferer();
      TagWord referee = p.getReferee();

      if (referee == null || referer == null) {
        continue;
      }

      // T0 Entity 21 25 katX
      // T1 Entity 80 82 it
      // R0 Coreference Referer:T1 Referee:T0

      System.out.println(referee.getOffset() + "<--" + referer.getOffset());
      System.out.println(referee.getContent() + "<--" + referer.getContent());
    }
  }
}
