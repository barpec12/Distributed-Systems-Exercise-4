import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class TimeServer {
	private static int PORT = 27780;
	private Random random;

	public TimeServer() {
		random = new Random();
		try (ServerSocket serverSocket = new ServerSocket(PORT)){
			System.out.println("Server started on port: " + PORT);

			while(true) {
				var client = serverSocket.accept();
				new Thread(new NTPRequestHandler(client)).start();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void threadSleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private long getTime() {
		return System.currentTimeMillis() + 1100L;
	}

	public static void main(String[] args) {
		new TimeServer();
	}

	private class NTPRequestHandler implements Runnable {
		private Socket client;

		public NTPRequestHandler(Socket client) {
			this.client = client;
		}

		@Override
		public void run() {
			try {
				var in = new ObjectInputStream(client.getInputStream());
				NTPRequest question = (NTPRequest) in.readObject();
				question.setT2(getTime());
				sendNTPAnswer(question);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					client.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

		}

		private void sendNTPAnswer(NTPRequest request) throws IOException {
			var out = new ObjectOutputStream(client.getOutputStream());
			var delay = random.nextInt(90) + 10;
			request.setT3(getTime());
			threadSleep(delay);
			out.writeObject(request);
		}

	}

}
