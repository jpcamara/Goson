var srcDir = file("src")
var classesDir = file("build/classes")
var distDir = file("build/dist")
var libDir = file(".")

function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :classpath = classpath(libDir.fileset(:includes = "**/*.jar")),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file("META-INF/MANIFEST.MF").write("Gosu-Typeloaders: com.jpcamara.gosu.json.JsonTypeLoader")
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