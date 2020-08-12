package net.rezxis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebAPI {

	private static OkHttpClient client;
	private static Gson gson = new Gson();
	
	static {
		client = new OkHttpClient.Builder().build();
	}
	
	public static void downloadWorld(File file, String secret, String uuid) throws Exception {
		String url = "http://172.18.0.1/worlds/api.php?type=download&secretKey="+secret+"&uuid="+uuid;
		Response res = client.newCall(new Request.Builder().url(url).get().build()).execute();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			IOUtils.copy(res.body().byteStream(), fos);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			fos.close();
			res.close();
		}
	}
	
	public static McuaResponse checkIP(String ip) throws IOException {
		String url = "https://api.mcua.net/checkip/"+ip;
		Response response = client.newCall(new Request.Builder().url(url).get().build()).execute();
		McuaResponse rs = gson.fromJson(response.body().string(), McuaResponse.class);
		response.close();
		return rs;
	}
	
	public static void webhook(DiscordWebHookEnum en, String contents) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					@SuppressWarnings("deprecation")
					Request request = new Request.Builder().url(en.url).addHeader("User-Agent", "Rezxis")
							.post(RequestBody.create(MediaType.parse("application/JSON; charset=utf-8"), new Gson().toJson(new DiscordWebHookRequest(en.name,"https://i.gyazo.com/141e75149b5cfe462af38d922027043f.png",contents)))).build();
					Response response = client.newCall(request).execute();
					response.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}}).start();
	}
	
	public enum DiscordWebHookEnum {
		REPORT("https://discordapp.com/api/webhooks/743081693006528592/Jis3_zTTNlo65_Ch7JmqBkq5sdMXwmiIVQkO5hiIOXm00p-axPi_PULLtwanEnHHa2ya","rezxis-reports"),
		CONNECT("https://discordapp.com/api/webhooks/743081771540676618/AhVHOtG_NSGTrNdZrVFIWTKOfDzLKvU7Gq6d8hFw24RcRqIOVv0cVpKrTlmDgshpjD2S","rezxis-connections"),
		PUNISHMENT("https://discordapp.com/api/webhooks/743082110876909639/rB1JfXCjfcz5kPimxRdtmNNLowqper2P8GXAZaIHoRr2mr_VKbYvaCJvitgCK4T0fdpf","rezxis-punishments"),
		PRIVATE("https://discordapp.com/api/webhooks/743083080209924097/cxm8fcYBxDXThtPkbSmJXAB7qdlNkakZGzsa0gxOb9ZZ_0BVTHxUMiNBSphRF40g0HZo","rezxis-private-log"),
		SCRIPTS("https://discordapp.com/api/webhooks/743083200624066591/ylhOJS4bgQDCqmcTLB25JlJ-Yg6VDyoeR5VE0J1-E7mHTwLYCw1_v-2UFN04ELkrYY-5","rezxis-scripts");
		String url;
		String name;
		
		DiscordWebHookEnum(String url, String name) {
			this.url = url;
			this.name = name;
		}
	}
	
	public class McuaResponse {
		private String ip;
		private String bad;
		private String type;
		private String country;
		
		public McuaResponse (String ip, String bad, String type, String country) {
			this.ip = ip;
			this.bad = bad;
			this.type = type;
			this.country = country;
		}
		
		public String getIP() {
			return this.ip;
		}
		
		public boolean isBad() {
			return Boolean.valueOf(bad);
		}
		
		public String getType() {
			return this.type;
		}
		
		public String getCountry() {
			return this.country;
		}
	}
}
