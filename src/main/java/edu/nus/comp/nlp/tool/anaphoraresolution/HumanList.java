package edu.nus.comp.nlp.tool.anaphoraresolution;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;

public class HumanList {

  private final static String[] maleList = new String("he him himself his")
      .split(" ");
  private final static String[] femaleList = new String("she her herself")
      .split(" ");
  private final static String[] thirdPersonList = new String(
      "he him himself his she her herself they them their themselves it its itself")
      .split(" ");
  private final static String[] secondPersonList = new String(
      "you your yourself yourselves").split(" ");
  private final static String[] firstPersonList = new String(
      "i me my myself we us our ourselves").split(" ");
  private final static String[] pluralList = new String(
      "we us ourselves our they them themselves their").split(" ");
  private final static String[] wholeList = new String(
      "he him himself his she her herself"
          + " i me myself my we us ourselves our you your yourself").split(" ");
  private final static String[] complementList = new String("it its itself")
      .split(" ");
  private final static String[] titleList = new String("Mr. Mrs. Miss Ms.")
      .split(" ");

  private final static int numberOfNameToCheck = -1; // 3000; //check only the
                                                     // first xx
  // most common first names,
  // respectively
  // final static Map maleNameTb =
  // getNameTb(System.getProperty("dataPath") + File.separator
  // +"male_first.txt",numberOfNameToCheck);
  private final static Map<String, String> maleNameTb = getNameTb(
      System.getProperty("dataPath")
          + File.separator
          + "MostCommonMaleFirstNamesInUS.mongabay.txt",
      numberOfNameToCheck);
  private final static Map<String, String> femaleNameTb = getNameTb(
      System.getProperty("dataPath") + File.separator + "female_first.txt",
      numberOfNameToCheck);

  public HumanList() {

  }

  public static boolean isMale(String wd) {
    // People's name should start with a capital letter
    return contains(maleList, wd)
        || (wd.matches("[A-Z][a-z]*") && contains(maleNameTb, wd));
  }

  public static boolean isFemale(String wd) {
    // People's name should start with a capital letter
    return contains(femaleList, wd)
        || (wd.matches("[A-Z][a-z]*") && contains(femaleNameTb, wd));
  }

  public static boolean isHuman(String wd) {
    if (wd.indexOf(" ") > 0 && contains(titleList, wd.split(" ")[0], true)) {
      // contains more than a single word and starts with a title
      return true;
    }
    return contains(wholeList, wd)
        // || contains((humanOccupationTb),wd)
        || isMale(wd)
        || isFemale(wd);
  }

  public static boolean isNotHuman(String wd) {
    return contains(complementList, wd);
  }

  public static boolean isPlural(String wd) {
    return contains(pluralList, wd);
  }

  public static boolean isThirdPerson(String wd) {
    return contains(thirdPersonList, wd);
  }

  public static boolean isSecondPerson(String wd) {
    return contains(secondPersonList, wd);
  }

  public static boolean isFirstPerson(String wd) {
    return contains(firstPersonList, wd);
  }

  // public static boolean isHumanTitle(String wd){
  // return contains(humanTitleTb,wd.toLowerCase());
  // }

  public static boolean contains(String[] list, String str) {
    return contains(list, str, false);
  }

  public static boolean contains(String[] list, String str,
      boolean caseSensitive) {
    boolean contain = false;

    if (caseSensitive) { // make this a outer check for efficiency's sake
      for (int i = 0; i < list.length; i++) {
        if (list[i].equals(str)) {
          contain = true;
          break;
        }
      }
    } else {
      for (int i = 0; i < list.length; i++) {
        if (list[i].equalsIgnoreCase(str)) {
          contain = true;
          break;
        }
      }
    }

    return contain;
  }

  public static boolean contains(Map<String, String> tb, String wd) {
    return tb.containsKey(wd);
  }

  private static String[] retriveList(String fileName) {
    try {
      return FileUtils.readFileToString(new File(fileName)).split("\\s+");
    } catch (IOException e) {
      e.printStackTrace();
      return new String[0];
    }
  }

  private static Map<String, String>
      getNameTb(String listFile, int range) {
    String[] nameArray = retriveList(listFile);

    Map<String, String> tb = Maps.newHashMap();
    checkArgument(nameArray.length > 0, "%s not found", listFile);

    if (nameArray != null) {
      int stopAt;
      if (range == -1) {
        stopAt = nameArray.length;
      } else {
        stopAt = Math.min(range, nameArray.length);
      }
      for (int i = 0; i < stopAt; i++) {
        String name = nameArray[i].substring(0, 1);
        if (nameArray[i].length() > 1) {
          name += nameArray[i].substring(1).toLowerCase();
        }
        tb.put(name, name);
      }
    }
    return tb;
  }
}
