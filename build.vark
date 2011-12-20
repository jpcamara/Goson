ProjectName = "Goson"
DefaultTarget = "jar"

function compile() {
  Maven.compile()
}

function test() {
  Maven.test()
}

function jar() {
  Maven.package( :skipTest = true )
}

function clean() {
  Maven.clean()
}
