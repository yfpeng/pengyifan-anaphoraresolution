/*
 JavaRAP: a freely-available JAVA anaphora resolution implementation
 of the classic Lappin and Leass (1994) paper:

 An Algorithm for Pronominal Anaphora Resolution.
 Computational Linguistics, 20(4), pp. 535-561.

 Copyright (C) 2005  Long Qiu

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package edu.nus.comp.nlp.tool.anaphoraresolution;
import java.io.*;
import java.util.regex.*;
/**
 * <p>Title: Anaphora Resolution</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Qiu Long
 * @version 1.0
 */

public class Env {

  static {


    //resolver mode
    System.setProperty("referenceChain", "true");
    System.setProperty("mode", "TagPresent");
    System.setProperty("keep log", "false");
    System.setProperty("display log", "true");
    System.setProperty("EvaluationVerbose", "false");
    System.setProperty("display resolving results", "false"); //Results will be shown as a part of log, if log is displayed. So set this to true only  if log is dampened.
    System.setProperty("Substitution", "true");
    System.setProperty("display substitution results", "false");
    System.setProperty("write substitution results", "false");
    System.setProperty("write resolving results", "false");


    //environment
    String environmentFileName = "env.jrap";//the file stores the environment variables (path to the Charniak parser, etc.)
    String parserHomeDir = null;
    String dataPath = null;
    String outputDir = null;
    String tmpDir = null;
    try{
      File environmentFile =  new File(".", environmentFileName);

        //locate
        String pathDelim = System.getProperty("path.separator");
        String classpath = System.getProperty("java.class.path");
        Pattern p = Pattern.compile(pathDelim + "?([\\S/&&[^" + pathDelim +
                                    "]]+)AnaphoraResolution.jar");
        Matcher m = p.matcher(classpath);
        if (m.find()) {
          classpath = m.group(1);
         // System.err.println(classpath);
        }else{
          classpath = "./";
        }
        if (!environmentFile.exists()) {
          //can't find the environment file, first try to find it under the directory were the jar file is stored
          environmentFile = new File(classpath + environmentFileName);
        }

      if(!environmentFile.exists()){
        System.err.println("Can't find the file \""+environmentFile.getCanonicalPath()+"\", where environment variables are stored. It should be in the directory where JavaRAP is invoked.");
        System.exit(-1);
      }

      environmentFileName = environmentFile.getCanonicalPath();

      String[] records = Util.readFile(environmentFileName).toString().split(System.getProperty("line.separator"));
      for(int i=0; i<records.length;i++){
        if(records[i].startsWith("#")){
          //comments
          continue;
        }
        String[] fields = records[i].trim().split("\\s+");
        if(fields.length!=2){
          //skip those lines not in "variable value" pairs
          continue;
        }
        if (fields[0].equalsIgnoreCase("parserHomeDir")) {
          parserHomeDir = fields[1].trim();
          if(parserHomeDir.startsWith(".")){ //./... relative path
             parserHomeDir = classpath+parserHomeDir.substring(2);
          }
        }
        if (fields[0].equalsIgnoreCase("dataPath")) {
          dataPath = fields[1].trim();
          if(dataPath.startsWith(".")){ //./... relative path
             dataPath = classpath+dataPath.substring(2);
          }

        }
        if (fields[0].equalsIgnoreCase("outputDir")) {
          outputDir = fields[1].trim();
          if(outputDir.startsWith(".")){ //./... relative path
             outputDir = classpath+outputDir.substring(2);
          }
        }
        if (fields[0].equalsIgnoreCase("tmpDir")) {
          tmpDir = fields[1].trim();
          if(tmpDir.startsWith(".")){ //./... relative path
             tmpDir = classpath+tmpDir.substring(2);
          }
        }
      }

      if(parserHomeDir == null
         ||dataPath == null
         ||outputDir == null
         ||tmpDir == null){
        System.err.println("Please make sure you have specified \"parserHomeDir, dataPath, outputDir and tmpDir\" in file \"env.jrap\".");
        System.exit(-1);
      }

      if( !(new File(parserHomeDir).exists()
         && new File(dataPath).exists()
         && new File(outputDir).exists()
         && new File(tmpDir).exists())){
        System.err.println("Please make sure \"parserHomeDir, dataPath, outputDir and tmpDir\" in file \"env.jrap\" all exist.");
        System.exit(-1);
      }

    }catch (Exception e){
      System.err.println("Something wrong while initialization: can't find the file \"env.jrap\", where environment variables are stored.");
      System.exit(-1);
    }

     //global

    //parser
    System.setProperty("parserHomeDir",parserHomeDir);
    System.setProperty("dataPath",dataPath);
    System.setProperty("parserOption"," ");

    //for aquaint corpus only
    System.setProperty("pathCorporaParsedSec","$HOME/NLP/data/corpora/aquaint-parsed");
    System.setProperty("pathCorporaParsed","$HOME/LinuxSwap/aquaint-parsed");
    System.setProperty("pathCorporaSentenceSplitted","$HOME/NLP/data/corpora/aquaint-boundaryed");


    //local to differ host
    //System.setProperty("inputDir","/swap/test");
    System.setProperty("outputDir",outputDir);//working directory
    System.setProperty("tmpDir",tmpDir);




  }

  public Env() {
  }
}
