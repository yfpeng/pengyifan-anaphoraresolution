/*
 JavaRAP: a freely-available JAVA anaphora resolution implementation
 of the classic Lappin and Leass (1994) paper:

 An Algorithm for Pronominal Anaphora Resolution.
 Computational Linguistics, 20(4), pp. 535-561.

 Copyright (C) 2005,2006  Long Qiu

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

import java.util.Comparator;

/**
 * <p>Title: Anaphora Resolution</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author Qiu Long
 * @version 1.0
 */

public class TagWordSalienceComp implements Comparator {

  public TagWordSalienceComp() {
  }
  public static void main(String[] args) {
    TagWordSalienceComp tagWordSalienceComp1 = new TagWordSalienceComp();
  }
  public int compare(Object o1, Object o2) {
    TagWord tw1 = (TagWord)o1;
    TagWord tw2 = (TagWord)o2;
    return tw1.getTmpSalience()-tw2.getTmpSalience();
  }
  public boolean equals(Object obj) {
    /**@todo Implement this java.util.Comparator method*/
    throw new java.lang.UnsupportedOperationException("Method equals() not yet implemented.");
  }
}
