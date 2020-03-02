package net.rezxis.utils.scripts;

import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.rezxis.utils.WebAPI;
import net.rezxis.utils.WebAPI.DiscordWebHookEnum;

public class ScriptEngineLauncher {
	
	private static final String RETURN = "\r\n";
	
	public static void run(String messageURL, String script) {
		ScriptContext context = null;
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			context = engine.getContext();
			context.setWriter(new StringWriter());
			engine.setContext(context);
			engine.eval(script);
		} catch (Exception e) {
			e.printStackTrace();
			if (context == null) {
				WebAPI.webhook(DiscordWebHookEnum.SCRIPTS, messageURL+RETURN+"EXCEPTION : "+RETURN+e.getMessage());
				return;
			}
			WebAPI.webhook(DiscordWebHookEnum.SCRIPTS, messageURL+RETURN+((StringWriter)context.getWriter()).toString()
					+RETURN+"EXCEPTION : "+RETURN+e.getMessage());
			return;
		}
		WebAPI.webhook(DiscordWebHookEnum.SCRIPTS, messageURL+RETURN+((StringWriter)context.getWriter()).toString());
	}
}
