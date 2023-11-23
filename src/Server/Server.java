package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.PrintStream;

public class Server {

	private static final int SERVER_PORT = 8080;
	public static soctet_stream_map<User, PrintStream> clients_string = new soctet_stream_map<User, PrintStream>();
	public void init() {
		try(
				ServerSocket ss = new ServerSocket(SERVER_PORT)){
			while(true) {
				Socket socket = ss.accept();
				System.out.println("�ѽ�������");
				ServerThread msg = new ServerThread(socket);
				msg.start();
			}
		}
		catch (IOException ex) {
			System.out.println("Server start failed!" + "SERVER_PORT" +
					"is already used?");
			
		}
	}
	
	public static void main(String[] args) {
		Server server = new Server();
		System.out.println("�ȴ��ͻ���������  . . .");
		server.init();
		
	}

}