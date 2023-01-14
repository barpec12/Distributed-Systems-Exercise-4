import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import static java.util.Objects.nonNull;

public class TimeClient {
	private static String hostUrl = "127.0.0.1";
	private static int PORT = 27780;
	private Double minD = Double.MAX_VALUE;
	private NTPRequest minNTPrequest;
	private Socket socket;
	private Random random;

	public TimeClient() {
		random = new Random();
		try {

			for (int i = 0; i < 10; i++) {
				socket = new Socket(InetAddress.getByName(hostUrl), PORT);
				NTPRequest question = new NTPRequest();
				sendNTPRequest(question);
				var in = new ObjectInputStream(socket.getInputStream());
				NTPRequest answer = (NTPRequest) in.readObject();
				answer.setT4(System.currentTimeMillis());
				answer.calculateOandD();
				System.out.println("o: " + answer.o);
				System.out.println("d: " + answer.d);
				if(answer.d < minD) {
					minD = answer.d;
					minNTPrequest = answer;
				}
				socket.close();
				threadSleep(350);
			}
			System.out.println("Selected time difference: " + minNTPrequest.o);
			System.out.println("Accuracy: " + minNTPrequest.d / 2D);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if(nonNull(socket))
					socket.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void sendNTPRequest(NTPRequest request) {
		try {
			var out = new ObjectOutputStream(socket.getOutputStream());
			var delay = random.nextInt(90) + 10;
			request.setT1(System.currentTimeMillis());
			threadSleep(delay);
			out.writeObject(request);
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

	public static void main(String[] args) {
		new TimeClient();
	}

}
