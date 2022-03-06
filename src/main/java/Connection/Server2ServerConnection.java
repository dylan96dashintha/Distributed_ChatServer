package Connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ClientHandler.ClientHandler;
import Server.ServerState;

public class Server2ServerConnection extends Thread{

	private static final Logger logger = LogManager.getLogger(Server2ServerConnection.class);
	
	private Socket socket;
	private InputStream inputFromClient;
	private Scanner scanner;
	
	public Server2ServerConnection(Socket socket) {
		try {
			this.socket = socket;
			this.inputFromClient = this.socket.getInputStream();
			this.scanner = new Scanner(this.inputFromClient, String.valueOf(StandardCharsets.UTF_8));
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
		

	@Override
	public void run() {
		try {

			while (true) {
				String line = this.scanner.nextLine();
				logger.info(line);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
