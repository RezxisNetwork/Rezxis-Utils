package net.rezxis.utils.scripts;

import java.io.IOException;
import java.io.StringWriter;

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
		ScriptContext context = engine.getContext();
		context.setWriter(new StringWriter());
		engine.setContext(context);
		try {
			engine.eval(script);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		WebAPI.webhook(DiscordWebHookEnum.SCRIPTS, messageURL+"\r\n"+((StringWriter)context.getWriter()).toString());
	}
}
