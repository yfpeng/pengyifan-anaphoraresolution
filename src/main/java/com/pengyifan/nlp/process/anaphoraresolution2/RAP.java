package com.pengyifan.nlp.process.anaphoraresolution2;

import com.pengyifan.commons.collections.dependencygraph.DependencyGraph;
import com.pengyifan.commons.collections.dependencygraph.DependencyGraphVertex;
import com.sun.javafx.collections.MappingChange;
import org.javatuples.Pair;

import java.util.List;
import java.util.Optional;

public class RAP {
  MappingChange.Map<DependencyGraphVertex, ArgumentFeature> maps;

  DependencyGraph graph;
  List<DependencyGraphVertex> npList;
  List<DependencyGraphVertex> prpList;

  List<Pair<DependencyGraphVertex, DependencyGraphVertex>> coreference;

  public RAP(DependencyGraph graph) {
    this.graph = graph;
  }

  public void resolve() {
    getNpList();
    getPrpList();
    setInfo();

    for (DependencyGraphVertex prp: prpList) {
      Optional<DependencyGraphVertex> maybeNp = findNp();
      if (maybeNp.isPresent()) {
        coreference.add(Pair.with(prp, maybeNp.get()));
      }
    }
  }
}
