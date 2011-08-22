package org.jschema.util;

import gw.util.GosuStringUtil;

public class JSONUtils {

  //TODO use Character.isJavaIdentifierPart() to scrub bad characters?
  public static String convertJSONStringToGosuIdentifier(String name) {
    return convertJSONStringToGosuIdentifier(name, true);
  }

  public static String convertJSONStringToGosuIdentifier(String name, boolean capitalizeFirst) {
    StringBuilder typeName = new StringBuilder();
    String normalizedName = name.replaceAll("\\s", "_");
    for (String piece : normalizedName.split("_")) {
      if (capitalizeFirst) {
        typeName.append(GosuStringUtil.capitalize(piece));
      }
      else
      {
        capitalizeFirst = true;
        typeName.append(piece);
      }
    }
    return typeName.toString();
  }

}
