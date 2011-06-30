classpath "../src,../lib/json-20080701.jar,../test"
typeloader com.jpcamara.gosu.json.JsonTypeLoader

var intialize = new typeloading.TestJsonTypes()
var junit = new org.junit.runner.JUnitCore()

/*org.junit.runner.JUnitCore.main(new String[] {"com.jpcamara.json.TestJsonTypes"})*/
var result = junit.run({typeloading.TestJsonTypes})
print("Run: ${result.getRunCount()} | Failed: ${result.getFailureCount()}")
result.getFailures().each(\ failure -> {
	print(failure.Message)
/*	print(failure.TestHeader)*/
	print(failure.Description)
	print(failure.Trace)
})

typeloading.TestJsonTypes.TypeInfo.Properties.each(\ p -> print(p.FeatureType))
/**
[equals( java.lang.Object ), getClass(), hashCode(), notify(), notifyAll(), toString(), 
wait(), wait( long ), wait( long, int ), createListener(), getFailureCount(), getFailures(), 
getIgnoreCount(), getRunCount(), getRunTime(), wasSuccessful()]
*/