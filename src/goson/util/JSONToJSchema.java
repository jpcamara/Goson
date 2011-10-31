package goson.util;

import goson.parser.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility for inputting json files and outputting json schemas
 */
public class JSONToJSchema {
  public static void main(String[] arguments) throws IOException {
    List<String> args = java.util.Arrays.asList(arguments);
    validateArgs(args);
//    loadGosu();

    File jsonFile = new File(args.get(1));
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

    Map json = (Map) new JSONParser(jsonContent).parseJSONFragment();
    Map jschema = (Map) JSchemaUtils.convertJsonToJSchema(json);
    if (args.size() == 2) {
      System.out.println(JSchemaUtils.serializeJson(jschema));
    } else {
      writeFile(args.get(3), JSchemaUtils.serializeJson(jschema));
    }
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

//  private static void loadGosu() {
//    String gosuHome = System.getenv("GOSU_HOME");
//    if (gosuHome == null) {
//      throw new RuntimeException("GOSU_HOME needs to be set for the utility to work properly");
//    }
//    try {
//      URLClassLoader loader = URLClassLoader.newInstance(new URL[] {
//              new File(gosuHome + "/ext/").toURI().toURL(),
//              new File(gosuHome + "/jars/").toURI().toURL()
//      }, JSONToJSchema.class.getClassLoader());
//      Class.forName("gw.lang.reflect.IType", true, loader);
//    } catch (MalformedURLException e) {
//      throw new RuntimeException("GOSU_HOME needs to be set for the utility to work properly", e);
//    } catch (ClassNotFoundException e) {
//      throw new RuntimeException("GOSU_HOME needs to be set for the utility to work properly", e);
//    }
//  }

  private static void writeFile(String fileName, String content) throws IOException {
    File outputFile = new File(fileName);
    if (!outputFile.exists()) {
      outputFile.createNewFile();
    }
    FileWriter write = new FileWriter(outputFile);
    try {
      write.append(content);
    } finally {
      write.close();
    }
  }
}
