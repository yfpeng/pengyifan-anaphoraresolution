package experiment;

import com.google.common.collect.Lists;
import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCCollection;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCSentence;
import com.pengyifan.bioc.io.BioCCollectionReader;
import com.pengyifan.brat.BratDocument;
import com.pengyifan.brat.BratEntity;
import com.pengyifan.brat.io.BratIOUtils;
import com.pengyifan.brat.util.BratConfigBuilder;
import com.pengyifan.commons.convert.BioCAnnotation2Entity;
import com.pengyifan.commons.convert.Entity2BratEntity;
import com.pengyifan.commons.io.AbstractBatchProcessor;
import com.pengyifan.commons.io.BasenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PrepareText extends AbstractBatchProcessor {
  public static void main(String[] args) throws Exception {
    Path bratInDir = Paths.get("../pengyifan-rules/tmp/bionlp2011/BioNLP-ST_2011_genia_devel_data_rev1");

    PrepareText s = new PrepareText(BasenameUtils.getTextBasenames(bratInDir));
    s.bratInDir = bratInDir;
    s.bratOutDir = Paths.get("tmp/BioNLP-ST_2011_genia_devel_data_coref");
    s.sdgFile = Paths.get("../pengyifan-rules/tmp/bionlp2011/bionlp2011_ge_devel-sdg.xml");
    s.process();
  }

  Path bratInDir;
  Path bratOutDir;
  Path sdgFile;
  BioCCollection sdgCollection;
  BratDocument a1doc;
  BratConfigBuilder bratConfigBuilder;

  public PrepareText(Collection<String> basenames) {
    super(basenames);
  }


  @Override
  protected void preprocessFile(String basename) throws Exception {
    Path a1File = bratInDir.resolve(basename + ".a1");
    a1doc = BratIOUtils.read(Files.newBufferedReader(a1File), basename);
  }

  @Override
  protected void processFile(String basename) throws Exception {
    System.out.println(basename);

    // max a1 entity index
    int entityIndex = 0;
    for (BratEntity entity : a1doc.getEntities()) {
      int id = Integer.parseInt(entity.getId().substring(1));
      entityIndex = Math.max(entityIndex, id);
      bratConfigBuilder.addEntity(entity.getType(), BratConfigBuilder.YELLOW1);
    }
    entityIndex++;

    final BioCAnnotation2Entity b2e = new BioCAnnotation2Entity.BasicConverter();
    final Entity2BratEntity e2b = new Entity2BratEntity.BasicConverter();
    // find referer
    List<BioCAnnotation> referers = findReferers(basename);
    for (BioCAnnotation referer : referers) {
      referer.putInfon("type", "Pronominal");
      BratEntity entity = e2b.apply(b2e.apply(referer));
      entity.setId("T" + entityIndex++);
      a1doc.addAnnotation(entity);
      bratConfigBuilder.addEntity(entity.getType(), BratConfigBuilder.BLUE1);
    }
  }

  private List<BioCAnnotation> findReferers(String basename) {
    List<BioCAnnotation> annotations = Lists.newArrayList();
    BioCDocument document = sdgCollection.getDocuments().stream()
        .filter(d -> d.getID().equals(basename))
        .findAny()
        .get();

    for (BioCPassage passage : document.getPassages()) {
      for (BioCSentence sentence : passage.getSentences()) {
        for (BioCAnnotation annotation : sentence.getAnnotations()) {
          Optional<String> maybeTag = annotation.getInfon("tag");
          if (maybeTag.isPresent() && (maybeTag.get().equalsIgnoreCase("PRP")
              || maybeTag.get().equalsIgnoreCase("PRP$"))) {
            annotations.add(annotation);
          }
        }
      }
    }
    return annotations;
  }

  @Override
  protected void preprocess() throws Exception {
    BioCCollectionReader reader = new BioCCollectionReader(Files.newBufferedReader(sdgFile));
    sdgCollection = reader.readCollection();
    reader.close();

    bratConfigBuilder = BratConfigBuilder.newBuilder();
    bratConfigBuilder.setOutputDir(bratOutDir);
  }

  @Override
  protected void postprocessFile(String basename) throws Exception {
    Path anFile = bratOutDir.resolve(basename + ".ann");
    BratIOUtils.write(Files.newBufferedWriter(anFile), a1doc);
  }

  @Override
  protected void postprocess() throws Exception {
    bratConfigBuilder.addEntity("Nominal", BratConfigBuilder.GREEN1);
    bratConfigBuilder.addRelation("coreference",
        "referer", "Pronominal", "referee", "Protein|Nominal");
    bratConfigBuilder.build();
  }
}
