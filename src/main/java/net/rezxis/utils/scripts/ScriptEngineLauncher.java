package net.rezxis.utils.scripts;

import java.io.IOException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptEngineLauncher {
	
	public static void run(String messageURL, String script) {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		ScriptOutput output = new ScriptOutput(messageURL);
		ScriptContext context = engine.getContext();
		context.setWriter(output);
		engine.setContext(context);
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
