package com.pengyifan.nlp.biocprocess;

import com.google.common.collect.Lists;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCNode;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCRelation;
import com.pengyifan.bioc.BioCSentence;
import com.pengyifan.bioc.util.BioCUtils;
import com.pengyifan.nlp.process.PtbAlignment;
import com.pengyifan.nlp.process.anaphoraresolution.AnaphoraResolver;
import com.pengyifan.nlp.process.anaphoraresolution.AnnotatedText;
import com.pengyifan.nlp.process.anaphoraresolution.CorreferencialPair;
import com.pengyifan.nlp.trees.OffsetTreeTransformer;
import com.pengyifan.nlp.trees.TreeBuilder;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.DeterministicCorefAnnotator;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class StanfordDcoref implements Annotator {

  private final Logger logger = LoggerFactory.getLogger(StanfordDcoref.class);
  private static final TreeBuilder treeBuilder = new TreeBuilder(
      OffsetTreeTransformer.treeTransformer());

  private final DeterministicCorefAnnotator annotator;

  public StanfordDcoref(Properties props) {
    annotator = new DeterministicCorefAnnotator(props);
  }

  @Override
  public void enter(BioCCollection collection) throws Exception {
    logger.info("Anaphora resolution from the collection: {}", collection.getSource());
    collection.clearInfons();
    collection.putInfon("tool", "Stanford CoreNLP");
    collection.putInfon("process", "dcoref");
  }

  @Override
  public void next(BioCDocument document) throws Exception {
    int annIndex = 0;
    int relIndex = 0;

    List<CoreMap> ssentences = Lists.newArrayList();
    int setIndex = 0;
    int labelIndex = 0;
    for (BioCPassage passage : document.getPassages()) {
      for (BioCSentence sentence : passage.getSentences()) {
        if (!sentence.getText().isPresent()) {
          logger.warn("{}: no text", BioCUtils.getXPathString(document, passage, sentence));
        }
        logger.trace("{}/text: {}", BioCUtils.getXPathString(document, passage, sentence),
            sentence.getText().get());
        Collection<BioCAnnotation> annotations = Lists.newArrayList(sentence.getAnnotations());

        // remove all annotations/relations
        sentence.clearAnnotations();
        sentence.clearRelations();

        for (BioCAnnotation annotation : annotations) {
          if (annotation.getInfon("parse tree").isPresent()) {
            int offset = annotation.getTotalLocation().getOffset();
            logger.trace("{}/offset: {}", BioCUtils.getXPathString(document, passage, sentence,
                annotation), offset);

            String text = annotation.getText().get();
            logger.trace("{}/text: {}", BioCUtils.getXPathString(document, passage, sentence,
                annotation), text);

            Tree tree = treeBuilder.build(annotation.getInfon("parse tree").get());
            logger.trace("{}/infon[@key='parse tree']: {}",
                BioCUtils.getXPathString(document, passage, sentence, annotation), tree);

            // align
            try {
              new PtbAlignment().alignment(text, Collections.singletonList(tree), offset);
            } catch (Exception e) {
              logger.error("Cannot align tree: {}", e.getMessage());
              continue;
            }

            CoreMap ssentence = new ArrayCoreMap();
            // tree
            ssentence.set(TreeCoreAnnotations.TreeAnnotation.class, tree);
            // token
            List<CoreLabel> coreLabels = Lists.newArrayList();
            for (Tree leaf: tree.getLeaves()) {
              CoreLabel label = (CoreLabel)leaf.label();
              label.setIndex(labelIndex ++);
              label.setSentIndex(setIndex);
              coreLabels.add(label);
            }
            ssentence.set(CoreAnnotations.TokensAnnotation.class, coreLabels);
            setIndex++;

            ssentences.add(ssentence);
          }
        }
      }
    }
    Annotation sannotation = new Annotation(BioCUtils.getText(document));
    sannotation.set(CoreAnnotations.SentencesAnnotation.class, ssentences);
    annotator.annotate(sannotation);
  }
}
