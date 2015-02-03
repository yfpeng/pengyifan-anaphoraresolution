package com.pengyifan.anaphoraresolution;

import java.util.List;

import edu.nus.comp.nlp.tool.anaphoraresolution.AnnotatedText;
import edu.nus.comp.nlp.tool.anaphoraresolution.CorreferencialPair;
import edu.nus.comp.nlp.tool.anaphoraresolution.TagWord;
import edu.nus.comp.nlp.tool.anaphoraresolution.AnaphoraResolver;

public class RAPClient {

  public static void main(String[] args) {

    String parseText = "(S1 (S (S (NP (PRP We)) (VP (VBD demonstrated) (SBAR (IN that) (S (NP (NN katX)) (VP (VBZ is) (ADVP (RB also)) (NP (DT a) (JJ sigmaB-dependent) (JJ general) (NN stress) (NN gene)) (, ,) (SBAR (IN since) (S (NP (PRP it)) (VP (VBZ is) (ADVP (RB strongly)) (VP (VBN induced) (PP (PP (IN by) (NP (NP (NN heat)) (, ,) (NP (NN salt)) (CC and) (NP (NN ethanol) (NN stress)))) (, ,) (CONJP (RB as) (RB well) (IN as)) (PP (IN by) (NP (NN energy) (NN depletion))))))))))))) (. .)))";

    AnnotatedText aText = new AnnotatedText(parseText.toString());
    AnaphoraResolver u = new AnaphoraResolver();
    List<CorreferencialPair> vet = u.resolverV1(
        aText.getNPList(),
        aText.getPRPList());

    for (CorreferencialPair p : vet) {
      System.out.println(p);
    }
  }
}
