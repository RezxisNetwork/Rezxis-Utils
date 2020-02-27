package net.rezxis.utils.scripts;

import java.io.IOException;
import java.io.Writer;

import net.rezxis.utils.WebAPI;
import net.rezxis.utils.WebAPI.DiscordWebHookEnum;

public class ScriptOutput extends Writer {

	private String scriptURL;
	public String content = "";
	
	public ScriptOutput(String scriptURL) {
		this.scriptURL = scriptURL;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		content += String.valueOf(cbuf);
		System.out.println(content);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}
}
