package httpserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import httpserver.config.Config;

public class Connection implements Runnable {
	
	private Socket socket;
	private BufferedReader br;
	private OutputStream os;
	private PrintWriter pw;
	
	private String requestLine;
	private String requestMethod;
	private String requestUrl;
	private String requestHttpVersion;
	private Map<String, String> headersMap = new HashMap<>();
	
	public Connection(Socket socket) {
		this.socket = socket;
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			os = socket.getOutputStream();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		try {
			requestLine = br.readLine();
			String[] temp = requestLine.split(" ");
			requestMethod = temp[0];
			requestUrl = temp[1];
			requestHttpVersion = temp[2];
			
			System.out.println(requestMethod + "," + requestUrl + "," + requestHttpVersion);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		new Thread() {
//			@Override
//			public void run() {
//				while(true) {
//					try {
//						String line = br.readLine();
//						if(line==null) break;
//						StringBuilder key = new StringBuilder();
//						StringBuilder value = new StringBuilder();
//						boolean isKey = true;
//						for(char c : line.toCharArray()) {
//							if(isKey) {
//								if(c!=':') {
//									key.append(c);
//								}
//								else {
//									isKey = false;
//								}
//							}
//							else {
//								value.append(c);
//							}
//						}
//						headersMap.put(key.toString(), value.toString());
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						return;
//					}
//				}
//			}
//		}.start();
		
		File file = new File(Config.ROOT + requestUrl);
		
		if(file.exists()&&file.isFile()&&file.canRead()) {
			
			StringBuilder sb = new StringBuilder();
			sb.append(requestHttpVersion + " 200 OK\r\n")
				.append("Date: " + new Date().toGMTString() + "\r\n")
				.append("Content-Length: " + file.length() + "\r\n")
				.append("\r\n");
			
			System.out.println("*****");
			System.out.println(sb.toString());
			
			try {
				InputStream fs = new FileInputStream(file);
				
				os.write(sb.toString().getBytes());
				
				byte[] temp = new byte[1024];
				while(fs.read(temp)!=-1) {
					os.write(temp);
				}
				
				pw = new PrintWriter(os);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			
			pw.print(sb.toString());
		}
		else {
			// 404
			StringBuilder sb = new StringBuilder();
			sb.append(requestHttpVersion + " 404 Not Found\r\n")
				.append("Date: " + new Date().toGMTString() + "\r\n")
				.append("Content-Length: 0\r\n")
				.append("\r\n");
			
			System.out.println("*****");
			System.out.println(sb.toString());
			
			try {
				os.write(sb.toString().getBytes());
				pw = new PrintWriter(os);
				pw.print(sb.toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			pw.flush();
			br.close();
			pw.close();
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
