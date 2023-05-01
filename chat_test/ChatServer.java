package chat_test;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements  Serializable{
	private static final long serialVersionUID = 1L;
	private ServerSocket serverSocket;
	private List<ChatHandler> list;

	public ChatServer() {
		try {
			serverSocket = new ServerSocket(9500);
			System.out.println("서버 준비 완료");
			list = new ArrayList<ChatHandler>();
			while (true) {
				Socket socket = serverSocket.accept();
				ChatHandler handler = new ChatHandler(socket, list);
				handler.start();
				list.add(handler);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new ChatServer();
	}
}
