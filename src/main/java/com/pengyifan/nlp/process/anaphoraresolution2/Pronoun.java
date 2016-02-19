package com.pengyifan.nlp.process.anaphoraresolution2;

import com.google.common.collect.Sets;
import edu.stanford.nlp.dcoref.Dictionaries;
import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import edu.stanford.nlp.dcoref.Dictionaries.Number;
import edu.stanford.nlp.dcoref.Dictionaries.Person;

import java.util.Set;

public class Pronoun {

  private static Dictionaries dictionaries = new Dictionaries();

  public final static Set<String> iPersonPronouns = Sets.newHashSet("i", "me", "myself", "mine", "my");
  public final static Set<String> youPersonPronouns = dictionaries.secondPersonPronouns;
  public final static Set<String> hePersonPronouns = Sets.newHashSet("he", "him", "himself", "his");
  public final static Set<String> shePersonPronouns = Sets.newHashSet("she", "her", "herself", "hers", "her");
  public final static Set<String> wePersonPronouns = Sets.newHashSet("we", "us", "ourself", "ourselves", "ours", "our");
  public final static Set<String> itPersonPronouns = Sets.newHashSet("it", "itself", "its", "one", "oneself", "one's");
  public final static Set<String> theyPersonPronouns = Sets.newHashSet("they", "them", "themself", "theirs", "their", "'em", "themselves");

  public static Gender getGender(String lemma) {
    if (dictionaries.malePronouns.contains(lemma)) {
      return Gender.MALE;
    } else if (dictionaries.femalePronouns.contains(lemma)) {
      return Gender.FEMALE;
    } else if (dictionaries.neutralPronouns.contains(lemma)) {
      return Gender.NEUTRAL;
    } else {
      return Gender.UNKNOWN;
    }
  }

  public static Number getNumber(String lemma) {
    if (dictionaries.singularPronouns.contains(lemma)) {
      return Number.SINGULAR;
    } else if (dictionaries.pluralPronouns.contains(lemma)) {
      return Number.PLURAL;
    } else {
      return Number.UNKNOWN;
    }
  }

  public static Person getPerson(String lemma) {
    if (iPersonPronouns.contains(lemma)) {
      return Person.I;
    } else if (youPersonPronouns.contains(lemma)) {
      return Person.YOU;
    } else if (hePersonPronouns.contains(lemma)) {
      return Person.HE;
    } else if (shePersonPronouns.contains(lemma)) {
      return Person.SHE;
    } else if (wePersonPronouns.contains(lemma)) {
      return Person.WE;
    } else if (theyPersonPronouns.contains(lemma)) {
      return Person.THEY;
    } else if (itPersonPronouns.contains(lemma)) {
      return Person.IT;
    } else {
      return Person.UNKNOWN;
    }
  }
}
