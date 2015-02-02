/*
 JavaRAP: a freely-available JAVA anaphora resolution implementation
 of the classic Lappin and Leass (1994) paper:

 An Algorithm for Pronominal Anaphora Resolution.
 Computational Linguistics, 20(4), pp. 535-561.

 Copyright (C) 2005,2011  Long Qiu

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

import javax.swing.tree.*;
import java.util.regex.*;
import java.io.*;
import java.util.*;

/**
 * <p>Title: Anaphora Resolution</p>
 * <p>Description: Class used during first development stage. Expired.</p>
 * <p>Copyright: Copyright (c) 2011</p>
 * <p>Company: </p>
 * @author Qiu Long
 * @version 1.0
 * Feb 12, 2006
 * Make it work on windows. Long Qiu
 */

public class MainProcess {
  static String inputDir = null;
  static String outputDir = null;
  private static final boolean fullProcess = true;
  static Env env = new Env(); //initiallize path informations

  public MainProcess() {
  }
}

