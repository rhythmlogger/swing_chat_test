package chat_test;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.List;

public class ChatHandler extends Thread implements  Serializable {
	private static final long serialVersionUID = 2L;
	private ObjectInputStream reader;
	private ObjectOutputStream writer;

	List<ChatHandler> list;
	private Socket socket;

	public ChatHandler(Socket socket, List<ChatHandler> list) throws IOException {
		this.socket = socket;
		this.list = list;
		writer = new ObjectOutputStream(socket.getOutputStream());
		reader = new ObjectInputStream(socket.getInputStream());
	}

	@Override
	public void run() {
		InfoDTO dto = null;
		String nickName = null;
		try {
			while (true) {
				dto = (InfoDTO) reader.readObject();
				nickName = dto.getNickName();
				if (dto.getCommand() == Info.EXIT) {
					InfoDTO sendDto = new InfoDTO();
					sendDto.setCommand(Info.EXIT);
					writer.writeObject(sendDto);
					writer.flush();
					reader.close();
					writer.close();
					socket.close();
					list.remove(this);
					if (list.size() < 1) {
						System.exit(0);
					}
					sendDto.setCommand(Info.SEND);
					sendDto.setMessage(nickName + "님 퇴장하였습니다.");
					broadcast(sendDto);
					break;
				} else if (dto.getCommand() == Info.JOIN) {
					nickName = dto.getNickName();
					InfoDTO sendDto = new InfoDTO();
					sendDto.setCommand(Info.SEND);
					sendDto.setMessage(nickName + "님 입장하였습니다.");
					broadcast(sendDto);
				} else if (dto.getCommand() == Info.SEND) {
					InfoDTO sendDto = new InfoDTO();
					sendDto.setCommand(Info.SEND);
					sendDto.setMessage("[" + nickName + "]" + dto.getMessage());
					broadcast(sendDto);
				}
			} // while
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void broadcast(InfoDTO sendDto) throws IOException {
		for (ChatHandler handler : list) {
			handler.writer.writeObject(sendDto);
			handler.writer.flush();
		}
	}

}
