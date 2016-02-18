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
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AnaphoraResolution2 implements Annotator {

  private final Logger logger = LoggerFactory.getLogger(AnaphoraResolution2.class);
  private static final TreeBuilder treeBuilder = new TreeBuilder(
      OffsetTreeTransformer.treeTransformer());

  @Override
  public void enter(BioCCollection collection) throws Exception {
    logger.info("Anaphora resolution from the collection: {}", collection.getSource());
    collection.clearInfons();
    collection.putInfon("tool", "JavaRAP");
    collection.putInfon("process", "anaphora resolution");
  }

  @Override
  public void next(BioCDocument document) {
    int annIndex = 0;
    int relIndex = 0;
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
            List<Tree> leaves = tree.getLeaves();

            AnnotatedText aText =new AnnotatedText(annotation.getInfon("parse tree").get());
            AnaphoraResolver u = new AnaphoraResolver();
            List<CorreferencialPair> vet = u.resolverV1(
                aText.getNPList(),
                aText.getPRPList());

            for (CorreferencialPair p : vet) {
              if (p.getReferee() == null || p.getReferer() == null) {
                continue;
              }

              BioCAnnotation referee = new BioCAnnotation();
              referee.setID("T" + annIndex++);
              referee.putInfon("type", "Entity");
              CoreLabel label = (CoreLabel) leaves.get(p.getReferee().getWordIndex()).label();
              referee.setText(label.word());
              referee.addLocation(new BioCLocation(
                  label.beginPosition(), label.endPosition() - label.beginPosition()));
              sentence.addAnnotation(referee);

              BioCAnnotation referer = new BioCAnnotation();
              referer.setID("T" + annIndex++);
              referer.putInfon("type", "Entity");
              label = (CoreLabel) leaves.get(p.getReferer().getWordIndex()).label();
              referer.setText(label.word());
              referer.addLocation(new BioCLocation(
                  label.beginPosition(), label.endPosition() - label.beginPosition()));
              sentence.addAnnotation(referer);

              BioCRelation abbr = new BioCRelation();
              abbr.setID("R" + relIndex++);
              abbr.putInfon("type", "coreference");
              abbr.addNode(new BioCNode(referee.getID(), "referee"));
              abbr.addNode(new BioCNode(referer.getID(), "referer"));
              sentence.addRelation(abbr);
            }
          }
        }
      }
    }
  }
}
