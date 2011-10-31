ProjectName = "Goson"
DefaultTarget = "jar"
BaseDir = file(".")

uses org.apache.tools.ant.BuildException
uses org.apache.tools.ant.taskdefs.optional.junit.JUnitTest

var srcDir = file("src")
var testDir = file("test")
var libDir = file("lib")
var classesDir = file("build/classes")
var testClassesDir = file("build/tests")
var distDir = file("build/dist")
var gosuHome = java.lang.System.getenv().get("GOSU_HOME")
if (gosuHome == null) {
	throw "Please set GOSU_HOME environment variable!" 
} 
var gosuDir = file("${gosuHome}/jars")
var gosuExtDir = file("${gosuHome}/jars")
var jarName = "goson-0.1.jar"

function deps() {
  Ivy.retrieve(:sync = true, :log = "download-only")
}

@Depends("deps")
function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :debug = true,
            :classpath = classpath()
              .withFileset(gosuDir.fileset())
              .withFileset(libDir.fileset()),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file("META-INF/MANIFEST.MF")//Main-Class: goson.util.JSONToJSchema\n\n
    .write("Main-Class: goson.util.JSONToJSchema\n" +
           "Gosu-Typeloaders: goson.typeloader.JSchemaTypeLoader\n\n")
}

@Depends("compile")
function compileTests() {
  Ant.mkdir(:dir = testClassesDir)
  Ant.javac(:srcdir = path(testDir),
            :debug = true,
            :classpath = classpath().withFile(classesDir)
              .withFileset(gosuDir.fileset())
              .withFileset(libDir.fileset()),
            :destdir = testClassesDir,
            :includeantruntime = false)
}

@Depends("compileTests")
function test() {

  var formatterElement = new org.apache.tools.ant.taskdefs.optional.junit.FormatterElement()
  var attr = org.apache.tools.ant.types.EnumeratedAttribute.getInstance(org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute, "plain")
  formatterElement.setType(attr as org.apache.tools.ant.taskdefs.optional.junit.FormatterElement.TypeAttribute)
  
  Ant.junit(:fork = true, :printsummary = Yes, :haltonfailure = true, :haltonerror = true,
    :classpathBlocks = {
      \ p -> p.withFileset(libDir.fileset("*.jar", null)),
      \ p -> p.withFileset(gosuDir.fileset("*.jar", null)),
      \ p -> p.withFile(classesDir),
      \ p -> p.withFile(testClassesDir),
      \ p -> p.withFile(testDir)
    },
    :formatterList = {
      formatterElement
    },
    :testList = {
      new org.apache.tools.ant.taskdefs.optional.junit.JUnitTest("goson.test.GosonSuite")
    })
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = distDir)
  Ant.jar(:destfile = distDir.file("${jarName}"),
          :manifest = classesDir.file("META-INF/MANIFEST.MF"),
          :basedir = classesDir)
}

@Depends("jar")
function runJar() {
  Ant.java(
    :jar = file("build/dist/${jarName}"),
    :args = "-json test/goson/examples/json/GithubCreate.json -jschema sample.jsc",
    :fork = true
  )
  print(file("sample.jsc").exists())
  Ant.delete(:file = file("sample.jsc"))
}

function clean() {
  Ant.delete(:dir = classesDir)
  Ant.delete(:dir = distDir)
}