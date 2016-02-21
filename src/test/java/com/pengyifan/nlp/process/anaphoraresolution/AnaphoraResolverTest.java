package com.pengyifan.nlp.process.anaphoraresolution;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

public class AnaphoraResolverTest {

  // The woman said that he is funny.
  public static final String s1 = "(S1 (S (S (NP (DT The) (NN woman)) (VP (VBD said) (SBAR (IN" +
      " that) (S (NP (NN he)) (VP (VBZ is) (ADJP (JJ funny))))))) (. .)))";
  // She likes her.
  public static final String s2 = "(S1 (S (S (NP (PRP She)) (VP (VBZ likes) (NP (PRP$ her)))) (. " +
      ".)))";
  // John seems to want to see him.
  public static final String s3 = "(S1 (S (S (NP (NN John)) (VP (VBZ seems) (S (VP (TO to) (VP " +
      "(VBP want) (S (VP (TO to) (VP (VB see) (NP (NN him)))))))))) (. .)))";
  // She sat near her .
  public static final String s4 = "(S1 (S (S (NP (PRP She)) (VP (VBD sat) (PP (IN near) (NP (PRP$" +
      " her))))) (. .)))";
  // He believes that the man is amusing.
  public static final String s5 = "(S1 (S (S (NP (NN He)) (VP (VBZ believes) (SBAR (IN that) (S " +
      "(NP (DT the) (NN man)) (VP (VBZ is) (NP (NN amusing))))))) (. .)))";
  // This is the man he said John wrote about.
  public static final String s6 = "(S1 (S (S (S (NP (DT This)) (VP (VBZ is) (NP (DT the) (NN man)" +
      " (NN he) (NN said)))) (S (NP (NNP John)) (VP (VBP wrote) (PRT (RP about))))) (. .)))";
  // John's portrait of him is interesting.
  public static final String s7 = "(S1 (S (S (NP (NP (NP (NN John) (POS 's)) (NN portrait)) (PP " +
      "(IN of) (NP (NN him)))) (VP (VBZ is) (ADJP (JJ interesting)))) (. .)))";
  // His portrait of John is interesting.
  public static final String s8 = "(S1 (S (S (NP (NP (NN His) (NN portrait)) (PP (IN of) (NP (NNP" +
      " John)))) (VP (VBZ is) (ADJP (JJ interesting)))) (. .)))";
  // His description of the portrait by John is interesting.
  public static final String s9 = "(S1 (S (S (NP (NP (NN His) (NN description)) (PP (IN of) (NP " +
      "(DT the) (NN portrait))) (PP (IN by) (NP (NNP John)))) (VP (VBZ is) (ADJP (JJ interesting)))) (. .)))";

  @Test
  public void testNo() throws Exception {
    for (String s: Lists.newArrayList(s1, s2, s3, s4, s5, s6, s7, s8, s9)) {
      System.out.println(s);
      AnnotatedText aText = new AnnotatedText(s);
      AnaphoraResolver u = new AnaphoraResolver();
      List<CorreferencialPair> vet = u.resolverV1(
          aText.getNPList(),
          aText.getPRPList());

      for (CorreferencialPair p : vet) {
        System.out.println(p);
      }
    }
  }

  // They wanted to see themselves .
  public static final String s10 = "(S1 (S (S (NP (PRP They)) (VP (VBD wanted) (S (VP (TO to) (VP" +
      " (VB see) (NP (PRP themselves))))))) (. .)))";
  // Mary knows the people who John introduced to each other.
  public static final String s11 = "(S1 (S (S (NP (JJ Mary)) (VP (VBZ knows) (NP (NP (DT the) (NN" +
      " people)) (SBAR (WHNP (WP who) (NN John)) (S (VP (VBN introduced) (PP (TO to) (NP (DT each)" +
      " (JJ other))))))))) (. .)))";
  // He worked by himself.
  public static final String s12 = "(S1 (S (S (NP (NN He)) (VP (VBD worked) (PP (IN by) (NP (PRP " +
      "himself))))) (. .)))";
  // Which friends plan to travel with each other?
  public static final String s13 = "(S1 (S (S (SBAR (WHNP (WDT Which)) (S (VP (NNS friends) (NP " +
      "(NN plan))))) (TO to) (VP (VBP travel) (PP (IN with) (NP (DT each) (JJ other))))) (. ?)))";
  // John likes Bill's portrait of himself.
  public static final String s14 = "(S1 (S (S (NP (NN John)) (VP (VBZ likes) (NP (NP (NP (NN " +
      "Bill) (POS 's)) (NN portrait)) (PP (IN of) (NP (PRP himself)))))) (. .)))";
  // They told stories about themselves.
  public static final String s15 = "(S1 (S (S (NP (PRP They)) (VP (VBN told) (NP (NNS stories)) " +
      "(PP (IN about) (NP (PRP themselves))))) (. .)))";
  // John and Mary like each other's portraits.
  public static final String s16 = "(S1 (S (S (NP (NP (NN John)) (CC and) (NP (JJ Mary))) (PP (IN" +
      " like) (NP (NP (DT each) (JJ other) (POS 's)) (NNS portraits)))) (. .)))";

  @Test
  public void testYes() throws Exception {
    for (String s: Lists.newArrayList(s10, s11, s12, s13, s14, s15, s16)) {
      System.out.println(s);
      AnnotatedText aText = new AnnotatedText(s);
      AnaphoraResolver u = new AnaphoraResolver();
      List<CorreferencialPair> vet = u.resolverV1(
          aText.getNPList(),
          aText.getPRPList());

      for (CorreferencialPair p : vet) {
        System.out.println(p);
      }
    }
  }
}