package com.pengyifan.nlp.process.anaphoraresolution2;

import edu.stanford.nlp.dcoref.Dictionaries.Gender;
import edu.stanford.nlp.dcoref.Dictionaries.Number;
import edu.stanford.nlp.dcoref.Dictionaries.Person;

public class ArgumentFeature {
  // features
  Number number;
  Person person;
  Gender gender;

  ArgumentFeature(){
    number = Number.UNKNOWN;
    person = Person.UNKNOWN;
    gender = Gender.UNKNOWN;
  }
}
