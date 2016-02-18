package com.pengyifan.nlp.process.anaphoraresolution2;

import com.pengyifan.commons.collections.dependencygraph.DependencyGraphVertex;

import java.util.Collection;

public class Info {
  ArgumentFeature argumentFeature;
  DependencyGraphVertex head;
  Collection<DependencyGraphVertex> arguments;
  Collection<DependencyGraphVertex> adjuncts;
}
