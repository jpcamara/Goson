uses java.lang.*

var srcDir = file("src")
var classesDir = file("build/classes")
var distDir = file("build/dist")
var libDir = file(".")
var gosuHome = System.getenv().get("GOSU_HOME")
if ( gosuHome == null ) {
	throw "Please set GOSU_HOME environment variable!" 
} 
var gosuDir = file(gosuHome + "/jars")


function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :classpath = classpath(gosuDir.fileset()),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file("META-INF/MANIFEST.MF").write("Gosu-Typeloaders: com.jpcamara.gosu.json.JsonTypeLoader\n\n")
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = distDir)
  Ant.jar(:destfile = distDir.file("goson.jar"),
          :basedir = classesDir)
}

function clean() {
  Ant.delete(:dir = classesDir)
  Ant.delete(:dir = distDir)
}