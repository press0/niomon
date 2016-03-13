package niomon;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * NIO client: burst message producer and statistics calculator
 */
public class NIOClient {

	/** reserved for future - session tracking */
	private NIOSession session;
	/** NIO server host default, may be over-ridden on command line */
	private String host = "localhost";
	/** NIO server port default, may be over-ridden on command line */
	private int port = 8000;
	/**
	 * messages to send to NIO server by default, may be over-ridden on command
	 * line
	 */
	private int count = 5;

	/** connect to NIO server, create TCP session info */
	private void runTest() {
		try {
			session = new NIOSession();
			Charset charset = Charset.forName("ISO-8859-1");
			CharsetEncoder encoder = charset.newEncoder();
			CharsetDecoder decoder = charset.newDecoder();
			ByteBuffer buffer = ByteBuffer.allocate(523);

			SocketChannel socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(true);
			Socket socket = socketChannel.socket();
			socket.setSoTimeout(2000);
			socketChannel.connect(new InetSocketAddress(host, port));

			System.out.println("time units is in milli-seconds, throughput is messages/second");

			String message = "SESSION: " + session.getSessionID();
			String printStatus;
			String response;
			long nsecCurrent;
			float msecTotal;
			float msecRoundTripOne;
			float msecRoundTripAverage = 0;
			float messagesPerSecond = 0;
			long nsecStartOne;
			long nsecStart = System.nanoTime();
			for (int i = 1; i <= this.count; i++) {
				nsecStartOne = System.nanoTime();
				socketChannel.write(encoder.encode(CharBuffer.wrap(message)));
				socketChannel.read(buffer);
				buffer.flip();
				response = decoder.decode(buffer).toString().trim();
				buffer.clear();
				nsecCurrent = System.nanoTime();
				msecTotal = (float) (nsecCurrent - nsecStart) / 1000000;
				msecRoundTripOne = (float) (nsecCurrent - nsecStartOne) / 1000000;
				msecRoundTripAverage = (float) (msecRoundTripAverage * (i - 1) + msecRoundTripOne) / i;
				messagesPerSecond = (int) (i * 1000) / msecTotal;
				printStatus = response + " RT (ms): " + msecRoundTripOne + " AVG RT (ms): " + msecRoundTripAverage
						+ " TOTAL (ms): " + msecTotal + " throughput: " + messagesPerSecond;
				System.out.println(printStatus);
				// System.out.println( (float) (System.nanoTime() - nsecCurrent)
				// / 1000000 );
			}
			session.setEndTime();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** print comand line help */
	private static void printHelp() {
		System.err.println("Usage: java niomon.NIOClient host port count");
		System.err.println("Usage: java niomon.NIOClient count");
		System.err.println("Usage: java niomon.NIOClient NIOClient ");
		return;
	}

	/**
	 * create NIO client for 3 different scenarios.
	 * 
	 * <pre>
	 * (1) specify remote server, port and message count
	 * (2) use default local server and port, specify message count
	 * (3) use default local server, port and message count
	 * </pre>
	 */
	public static void main(String[] args) {
		if (args.length > 1 && Pattern.matches("help", args[0])) {
			printHelp();
		} else if (args.length == 3 && Pattern.matches("[0-9]+", args[1]) && Pattern.matches("[0-9]+", args[2])) {
			new NIOClient(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		} else if (args.length == 1 && Pattern.matches("[0-9]+", args[0])) {
			new NIOClient(Integer.parseInt(args[0]));
		} else if (args.length == 0) {
			new NIOClient();
		} else {
			printHelp();
		}
	}

	/** use default server, port and message count */
	public NIOClient() {
		runTest();
	}

	/** use default server and port, specify message count */
	public NIOClient(int _c) {
		count = _c;
		runTest();
	}

	/** specify server, port and message count */
	public NIOClient(String _host, int _port, int _count) {
		host = _host;
		port = _port;
		count = _count;
		runTest();
	}
}
