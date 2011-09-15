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
              .withFileset(libDir.fileset())
              .withFileset(gosuExtDir.fileset()),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file("META-INF/MANIFEST.MF").write("Gosu-Typeloaders: org.jschema.typeloader.JsonTypeLoader\n\n")
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
      new org.apache.tools.ant.taskdefs.optional.junit.JUnitTest("org.jschema.test.GosonSuite")
    })
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = distDir)
  Ant.jar(:destfile = distDir.file("goson.jar"),
          :manifest = classesDir.file("META-INF/MANIFEST.MF"),
          :basedir = classesDir)
}

function clean() {
  Ant.delete(:dir = classesDir)
  Ant.delete(:dir = distDir)
}