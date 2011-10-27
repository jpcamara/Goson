package org.jschema.util;

import gw.lang.reflect.java.IJavaType;
import org.jschema.parser.JSONParser;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility for inputting json files and outputting json schemas
 */
public class JSONToJSchema {
  public static void main(String[] arguments) {
    List<String> args = java.util.Arrays.asList(arguments);
    validateArgs(args);

    File jsonFile = new File(args.get(1));
//    File jschemaFile = new File(args.get(3));
    String jsonContent = null;

    Scanner scan = null;
    try {
      scan = new Scanner(jsonFile);
      scan.useDelimiter("\\Z");
      jsonContent = scan.next();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      scan.close();
    }

    Map json = (Map) JSONParser.parseJSONValue(jsonContent, IJavaType.MAP);
    System.out.println(JSchemaUtils.convertJsonToJSchema(json));
  }

  private static void validateArgs(List<String> args) {
    if (args.size() != 4 && args.size() != 2) {
      System.out.println(args);
      throw new IllegalArgumentException("2 or 4 arguments are expected: " +
        "[-json], with a second argument of the filename. [optionally, [-jschema], with a second argument of the filename.]");
    }

    if (!args.get(0).equals("-json")) {
      throw new IllegalArgumentException("first argument should be [-json].");
    }

    if (args.size() == 4) {
      if (!args.get(2).equals("-jschema")) {
        throw new IllegalArgumentException("third argument should be [-jschema].");
      }
    }


  }
}
