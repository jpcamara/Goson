package org.jschema.test;

import gw.fs.ResourcePath;
import gw.fs.physical.PhysicalDirectoryImpl;
import gw.lang.init.GosuInitialization;
import gw.lang.init.GosuPathEntry;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaBackedType;
import gw.lang.shell.Gosu;
import junit.framework.TestSuite;
import org.jschema.typeloader.JsonTypeLoader;

import java.util.ArrayList;
import java.util.List;

public class GosonSuite extends TestSuite {

  private static String[] getTests() {
    ArrayList<String> allTestNames = new ArrayList<String>();
    for (CharSequence nameCharSeq : TypeSystem.getAllTypeNames()) {
      String name = nameCharSeq.toString();
      if (name.startsWith("org.jschema") && name.endsWith("Test")) {
        allTestNames.add(name);
      }
    }
    return allTestNames.toArray(new String[allTestNames.size()]);
  }

  public GosonSuite() {
    super(getTestClasses());
  }

  private static Class[] getTestClasses() {
    GosonSuite.maybeInitGosu();
    String[] testNames = getTests();
    List<Class> tests = new ArrayList<Class>();
    for (String test : testNames) {
      IJavaBackedType byFullName = (IJavaBackedType) TypeSystem.getByFullName(test);
      tests.add(byFullName.getBackingClass());
    }
    return tests.toArray(new Class[tests.size()]);
  }

  public static TestSuite suite() {
    return new GosonSuite();
  }

  public static void maybeInitGosu() {
    if (!GosuInitialization.isInitialized()) {
      Gosu.init();

      // check to see if there is a JsonTypeLoader around, and create one if not
      if (TypeSystem.getTypeLoader(JsonTypeLoader.class) == null) {
        TypeSystem.pushGlobalTypeLoader(new JsonTypeLoader(TypeSystem.getCurrentModule()));
      }

      TypeSystem.refresh(true);

      //verify that the JSON types are around
      IType exampleType = TypeSystem.getByFullNameIfValid("org.jschema.examples.NameAndAge");
      if (exampleType == null) {
        throw new IllegalStateException("The Goson Test Environment is not set up correctly: could not find the NameAndAge type");
      }
    }
  }

}
