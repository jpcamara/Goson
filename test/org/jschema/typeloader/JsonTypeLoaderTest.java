package org.jschema.typeloader;

import gw.fs.IDirectory;
import gw.fs.IFile;
import gw.fs.IResource;
import gw.fs.ResourcePath;
import gw.lang.init.GosuInitialization;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.*;
import gw.lang.shell.Gosu;
import gw.util.Pair;
import junit.framework.TestCase;
import org.jschema.test.GosonTest;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JsonTypeLoaderTest extends GosonTest {

  public void testBasicGosonTypes() {
    IType nameAndAge = TypeSystem.getByFullName("org.jschema.examples.NameAndAge");
    assertNotNull(nameAndAge);
  }

  public void testNestedGosonTypes() {
    IType fullExample = TypeSystem.getByFullName("org.jschema.examples.fullexample.Example");
    assertNotNull(fullExample);

    IType someType = TypeSystem.getByFullName("org.jschema.examples.fullexample.Example.SomeType");
    assertNotNull(someType);

    IType nestedType = TypeSystem.getByFullName("org.jschema.examples.fullexample.Example.SomeType.NestedType");
    assertNotNull(nestedType);
  }

}
