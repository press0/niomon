package niomon;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * NIO ServerSocketChannel: non-blocking multiplexed NIO request handler
 */
public class NIOServer {
	
	/** print comand line help */
	public static void printHelp() {
		System.err.println("Usage: java niomon.NIOServer port ");
	    System.err.println("Usage: java niomon.NIOServer ");
	    return;
	}

	/** 
	 * create NIO server for 2 different scenarios.
	 * 
	 * <pre>
	 * (1) specify port 
	 * (2) use default port
	 * </pre>
	 * */
	public static void main(String[] args) throws IOException {
		int port = 8000;
		if ( args.length == 0 ) { //use default port
		} else if ( args.length == 1 && Pattern.matches("[0-9]+", args[0]) ) { //set port
			port = Integer.parseInt(args[0]);
		} else { //help
			printHelp();
			return;
		}
	
	Charset charset = Charset.forName("ISO-8859-1");
	CharsetEncoder encoder = charset.newEncoder();
	CharsetDecoder decoder = charset.newDecoder();
	ByteBuffer buffer = ByteBuffer.allocate(523);
	Selector selector = Selector.open();
	
	ServerSocketChannel ssc = ServerSocketChannel.open();
	ssc.socket().bind(new InetSocketAddress( port ));
	ssc.configureBlocking(false);
	
	System.out.println("NIOServer running on port " + port + " ...");
	// registration of Channel with Selector
	SelectionKey serverkey = ssc.register(selector, SelectionKey.OP_ACCEPT);
	for (;;) {
		selector.select();	//block until a channel is ready for I/O
		Set<SelectionKey> keys = selector.selectedKeys();
		
		for( Iterator<SelectionKey> i = keys.iterator(); i.hasNext(); ) {
			SelectionKey key = (SelectionKey) i.next();
			i.remove();
			
			// if server socket channel activity, then a new client wants to connect to the server
			if (key == serverkey) {	
				if (key.isAcceptable()) {
					SocketChannel client = ssc.accept();
					client.configureBlocking(false);
					SelectionKey clientkey = client.register(selector, SelectionKey.OP_READ);
					clientkey.attach(new Integer(0));
				}
			// if not server socket channel activity, then this is an existing client connection
			} else {
				SocketChannel client = (SocketChannel) key.channel();
				if (!key.isReadable()) continue;
				
				// end-of-stream, client disconnect: deregister key, close channel
				int bytesread = 0;
				try {
					bytesread = client.read(buffer);
				} catch ( IOException ioe ) {
					System.out.println("SESSION CLOSED: " + client.toString());
					bytesread = -1;
				}
				if ( bytesread == -1 ) { 
					key.cancel();
					client.close();
					continue;
				}
				
				buffer.flip();
				String request = decoder.decode(buffer).toString();
				buffer.clear();
				
				if (request.trim().equals("quit")) {	// quit: deregister key, close channel
					client.write(encoder.encode(CharBuffer.wrap("Bye.")));
					key.cancel();
					client.close();
				} else {								// default: respond UC with sequence number 
					int num = ((Integer)key.attachment()).intValue();
					String response = "RESPONSE " + num + ": " + request.toUpperCase();
					client.write(encoder.encode(CharBuffer.wrap(response)));
					
					System.out.println( response );
	
					key.attach(new Integer(num+1));
				}
			}
		}
	}
	}
}
