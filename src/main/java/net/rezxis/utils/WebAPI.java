package net.rezxis.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.SocketFactory;
import javax.xml.bind.DatatypeConverter;

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
	private static Pattern pattern = Pattern.compile("last check (.*) (.*) ago");
	private static Pattern patternTCP = Pattern.compile("TCP\\((.*)\\)");
	private static Pattern patternUDP = Pattern.compile("UDP\\((.*)\\)");
	public static ExecutorService pool = null;
	
	static {
		client = new OkHttpClient.Builder().build();
		pool = Executors.newFixedThreadPool(3);
	}
	
	public static void downloadWorld(File file, String secret, String uuid) throws Exception {
		String url = "https://world.rezxis.net/get.php?secret="+secret+"&uuid="+uuid;
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
		res.close();
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println(gson.toJson(checkIP("60.79.209.85")));
	}
	
	public static CheckIPResponse checkIP(String ip) throws IOException {
		{
			String freevpn = "https://freevpn.gg/c/"+ip;
			Response response = client.newCall(new Request.Builder().url(freevpn).get().build()).execute();
			String body = response.body().string();
			response.close();
			if (!body.contains("Not found")) {
				Matcher matcher = pattern.matcher(body);
				if (matcher.find()) {
					boolean flag = false;
					String type = matcher.group(2);
					if (type.equalsIgnoreCase("seconds") || type.equalsIgnoreCase("minutes") || type.equalsIgnoreCase("hours"))
						flag = true;
					if (type.equalsIgnoreCase("days") && Integer.valueOf(matcher.group(1)) <= 1) {
						flag = true;
					}
					if (flag)
						return new CheckIPResponse(ip, "true", "FREEVPN-"+matcher.group(1)+matcher.group(2), "FREEVPN");
				}
				int tcp = -1;
				int udp = -1;
				Matcher m2 = patternTCP.matcher(body);
				if (m2.find())
					tcp = Integer.valueOf(m2.group(1));
				m2 = patternUDP.matcher(body);
				if (m2.find())
					udp = Integer.valueOf(m2.group(1));
				if (checkOpenVPN(ip, tcp, udp)) {
					return new CheckIPResponse(ip, "true", "FREEVPN-OpenVPN", "FREEVPN");
				}
				return new CheckIPResponse(ip, "false", "FREEVPN-PASS", "FREEVPN");
			}
		}
		String url = "https://api.mcua.net/checkip/"+ip;
		Response response = client.newCall(new Request.Builder().url(url).get().build()).execute();
		CheckIPResponse rs = gson.fromJson(response.body().string(), CheckIPResponse.class);
		response.close();
		rs.type = "MCUA-"+rs.type;
		return rs;
	}
	
	public static boolean checkOpenVPN(String ip, int tcp, int udp) {
		if (tcp != -1) {
			try {
				Socket tcpSoc = new Socket();
				tcpSoc.connect(new InetSocketAddress(ip, tcp), 1000);
				tcpSoc.close();
				return true;
			} catch (Exception e) {
				if (!(e instanceof SocketTimeoutException)) {
					e.printStackTrace();
				}
			}
		}
		if (udp != -1) {
			DatagramSocket socket = null;
			try {
				byte[] data = DatatypeConverter.parseHexBinary("3801000000000000000000000000");
				socket = new DatagramSocket();
				DatagramPacket dp = new DatagramPacket(data, data.length, InetAddress.getByName(ip), udp);
				socket.send(dp);
				data = new byte[26];
				dp = new DatagramPacket(data, data.length);
				socket.setSoTimeout(1000);
				socket.receive(dp);
				socket.close();
				return true;
			} catch (Exception e) {
				if (!(e instanceof SocketTimeoutException || e instanceof PortUnreachableException)) {
					e.printStackTrace();
				}
			} finally {
				if (socket != null)
					socket.close();
			}
		}
		return false;
	}
	
	public static void webhook(DiscordWebHookEnum en, String contents) {
		pool.submit(new DiscordRunnable(en,contents));
	}
	
	public static class DiscordRunnable implements Runnable {

		private DiscordWebHookEnum en;
		private String contents;
		
		public DiscordRunnable(DiscordWebHookEnum en, String contents) {
			this.en = en;
			this.contents = contents;
		}
		@Override
		public void run() {
			try {
				@SuppressWarnings("deprecation")
				Request request = new Request.Builder().url(en.url).addHeader("User-Agent", "Rezxis")
						.post(RequestBody.create(MediaType.parse("application/JSON; charset=utf-8"), new Gson().toJson(new DiscordWebHookRequest(en.name,"https://i.gyazo.com/141e75149b5cfe462af38d922027043f.png","```"+contents+"```")))).build();
				Response response = client.newCall(request).execute();
				response.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public enum DiscordWebHookEnum {
		REPORT("https://discordapp.com/api/webhooks/743081693006528592/Jis3_zTTNlo65_Ch7JmqBkq5sdMXwmiIVQkO5hiIOXm00p-axPi_PULLtwanEnHHa2ya","rezxis-reports"),
		CONNECT("https://discordapp.com/api/webhooks/743081771540676618/AhVHOtG_NSGTrNdZrVFIWTKOfDzLKvU7Gq6d8hFw24RcRqIOVv0cVpKrTlmDgshpjD2S","rezxis-connections"),
		PUNISHMENT("https://discordapp.com/api/webhooks/743082110876909639/rB1JfXCjfcz5kPimxRdtmNNLowqper2P8GXAZaIHoRr2mr_VKbYvaCJvitgCK4T0fdpf","rezxis-punishments"),
		PRIVATE("https://discordapp.com/api/webhooks/744822613037154344/3NX_eb_H7sK9gXQTXK1qzO2Pe3pvzejloUTep7-QGCeIqb3dHBBXpDp2aMyq75eSuuog","rezxis-private-log"),
		SCRIPTS("https://discordapp.com/api/webhooks/744822745065586708/3cJyDaDYDlO_Nvke0i1T-zEzXbR1qfgcfSRLm-OKNTNI2J5xXCPIJdlLJlxRgwABD_hM","rezxis-scripts"),
		WATCHDOG("https://discordapp.com/api/webhooks/744831842238070894/baX0CJDzFLL2bWMi_ZXj1OTGR6Xfhao7nwTt5Qi5unY9G81AH8K0PeJGKEYQXCJVonFT","rezxis-watchdog");
		String url;
		String name;
		
		DiscordWebHookEnum(String url, String name) {
			this.url = url;
			this.name = name;
		}
	}
	
	public static class CheckIPResponse {
		private String ip;
		private String bad;
		private String type;
		private String country;
		
		public CheckIPResponse (String ip, String bad, String type, String country) {
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
