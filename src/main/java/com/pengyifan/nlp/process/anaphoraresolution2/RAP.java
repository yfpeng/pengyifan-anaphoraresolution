package com.pengyifan.nlp.process.anaphoraresolution2;

import com.google.common.collect.Lists;
import com.pengyifan.commons.collections.dependencygraph.DependencyGraph;
import com.pengyifan.commons.collections.dependencygraph.DependencyGraphVertex;
import com.pengyifan.commons.collections.dependencygraph.PartOfSpeech;
import org.javatuples.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import edu.stanford.nlp.dcoref.Dictionaries.Number;
import edu.stanford.nlp.dcoref.Dictionaries.Gender;

public class RAP {
  Map<DependencyGraphVertex, Info> maps;

  DependencyGraph graph;
  List<DependencyGraphVertex> nounPhraseList;
  List<DependencyGraphVertex> pronounList;

  List<Pair<DependencyGraphVertex, DependencyGraphVertex>> coreference;

  public RAP(DependencyGraph graph) {
    this.graph = graph;
  }

  public void resolve() {
    getList();
    setInfo();

    // syntactic filter

    // morphological filter

    // pleonastic pronouns

    // binding algorithm

    // salience

    // equivalence class

    // decision
  }

  private void setInfo() {
    for (DependencyGraphVertex v: maps.keySet()) {
      Info info = maps.get(v);
      // argument feature
      // number
      if (v.getTag().isNoun()) {
        switch (v.getTag()) {
        case NN:
        case NNP:
          info.argumentFeature.number = Number.SINGULAR;
          break;
        case NNS:
        case NNPS:
          info.argumentFeature.number = Number.PLURAL;
          break;
        }
      } else if (v.getTag().isPronoun()) {
        info.argumentFeature.number = Number.UNKNOWN;
      }

      // gender
      if (v.getTag().isPronoun()) {
        info.argumentFeature.gender = Pronoun.getGender(v.getLemma());
      }

      // person
      if (v.getTag().isPronoun()) {
        info.argumentFeature.person = Pronoun.getPerson(v.getLemma());
      }
    }
  }

  private void getList() {
    nounPhraseList = Lists.newArrayList();
    pronounList = Lists.newArrayList();

    for (DependencyGraphVertex v: graph.vertexSet()) {
      if (v.getTag().isNoun()) {
        nounPhraseList.add(v);
        maps.put(v, new Info());
      } else if (v.getTag().isPronoun()) {
        pronounList.add(v);
        maps.put(v, new Info());
      }
    }
  }
}
