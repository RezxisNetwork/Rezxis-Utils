package net.rezxis.utils;

public class DiscordWebHookRequest {
	public final String username;
	public final String avatar_url;
	public final String content;
	
	public DiscordWebHookRequest(String username, String avatar_url, String content) {
		this.username = username;
		this.avatar_url = avatar_url;
		this.content = content;
	}
}
