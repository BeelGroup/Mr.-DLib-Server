package org.mrdlib.translator;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.io.*;

public class SendPacket implements Callable<String> {
	private static final Charset FILE_ENCODING = Charset.forName("UTF-8");
	private String message;
	private String translatedText;

	public SendPacket(String message) {
		super();
		this.message = message;
	}

	public String send(String msg) throws Exception {

		Socket socket = null;

		socket = new Socket("localhost", 5674);
		// System.out.println("Got socket");
		OutputStream os = socket.getOutputStream();

		// System.out.println("Got output stream");
		os.write((msg + "\n").getBytes());
		os.flush();

		// System.out.println("Flushed");
		// System.out.println(msg + "\n");

		// System.out.println("Got input stream");
		if (socket.isClosed())
			System.out.println("socket is closed");
		BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), FILE_ENCODING));

		// System.out.println("Got object input stream");
		// read the server response message
		String message = (String) reader.readLine();

		// close resources
		os.close();
		reader.close();
		socket.close();// !!!!
		return message;
	}

	private static String[] readText() throws IOException {
		if (System.console() != null) {
			int lines = Integer.parseInt(System.console().readLine("Enter Number of strings: "));
			String[] text = new String[lines];
			int i = 0;
			while (i < lines) {
				String germanText = System.console().readLine("Input line:");
				text[i] = germanText;
				i++;
			}
			return text;
		}
		System.out.print("Enter Number of Strings: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		int lines = Integer.parseInt(reader.readLine());
		String[] text = new String[lines];
		int i = 0;
		while (i < lines) {
			System.out.print("Input line:");
			String germanText = reader.readLine();
			text[i] = germanText;
			i++;
		}
		return text;
	}

	public static void main(String[] args) {

		try {
			String[] germanText = SendPacket.readText();

			long time = System.currentTimeMillis();
			ExecutorService pool = Executors.newFixedThreadPool(4);
			List<Future<String>> futures = new ArrayList<Future<String>>(10);

			for (int i = 0; i < germanText.length; i++) {
				futures.add(pool.submit(new SendPacket(germanText[i])));
			}

			for (Future<String> future : futures) {
				String result = future.get();
				System.out.println(result);
				// Compute the result
			}

			pool.shutdown();
			
			System.out.println(System.currentTimeMillis() - time);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Did not work");
		}
	}

	public String call() {

		try {
			return send(message);
		} catch (Exception e) {

			e.printStackTrace();
			return "";
		}

	}

	public String getTranslatedText() {
		return translatedText;
	}

	public void setTranslatedText(String translatedText) {
		this.translatedText = translatedText;
	}
}