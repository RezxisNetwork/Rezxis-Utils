package net.rezxis.utils.scripts;

import java.io.IOException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.rezxis.utils.WebAPI;
import net.rezxis.utils.WebAPI.DiscordWebHookEnum;

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
		System.out.println(output.content);
		WebAPI.webhook(DiscordWebHookEnum.SCRIPTS, messageURL+"\r\n"+output.content);
		try {
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
