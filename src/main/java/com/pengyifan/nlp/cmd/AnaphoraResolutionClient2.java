package com.pengyifan.nlp.cmd;

import com.pengyifan.nlp.biocprocess.AnaphoraResolution2;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AnaphoraResolutionClient2 {

  public void doMain(String[] args) throws Exception {
    Options options = new Options();
    options.addOption(BasicCommand.help);
    options.addOption(BasicCommand.in);
    options.addOption(BasicCommand.out);
    options.addOption(BasicCommand.verbose);

    CommandLine cmd = BasicCommand.parseArguments(
        getClass().getName(), options, args);

    Path inFile = Paths.get(cmd.getOptionValue("i"));
    Path outFile = Paths.get(cmd.getOptionValue("o"));

    new AnaphoraResolution2().annotate(inFile, outFile);
  }

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      String cmdline =
          " -i tmp/bionlp2011_ge_devel-ptb.xml"
              + " -o tmp/bionlp2011_ge_devel-coref.xml";
      args = cmdline.split(" ");
    }
    new AnaphoraResolutionClient2().doMain(args);
  }
}
