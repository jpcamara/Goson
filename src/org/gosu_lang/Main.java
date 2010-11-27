package org.gosu_lang;
import gw.lang.reflect.TypeSystem;
import gw.lang.shell.Gosu;

public class Main {
	public static void main(String... args) throws Exception {
		Gosu.main("-e", "print( \"foo\" )"); // initialize Gosu - I'm sure
												// there's a better way
		TypeSystem.pushGlobalTypeLoader(new com.jpcamara.gosu.json.JsonTypeLoader());
		Gosu.main("-g");
	}
}