ProjectName = "Goson"
DefaultTarget = "jar"
BaseDir = file(".")

var srcDir = file("src")
var classesDir = file("build/classes")
var distDir = file("build/dist")
var libDir = file("lib")
var gosuHome = java.lang.System.getenv().get("GOSU_HOME")
if (gosuHome == null) {
	throw "Please set GOSU_HOME environment variable!" 
} 
var gosuDir = file("${gosuHome}/jars")

@Depends("clean")
function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :classpath = classpath()
              .withFileset(libDir.fileset())
              .withFileset(gosuDir.fileset()),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file("META-INF/MANIFEST.MF").write("Gosu-Typeloaders: com.jpcamara.gosu.json.JsonTypeLoader\n\n")
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