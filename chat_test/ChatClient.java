package chat_test;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends JFrame implements ActionListener, Runnable, Serializable {
	private static final long serialVersionUID = 3L;
	private JTextArea output;
	private JTextField input;
	private JButton sendBtn;
	private Socket socket;
	private ObjectInputStream reader = null;
	private ObjectOutputStream writer = null;
	private String nickName;

	public ChatClient() {
		output = new JTextArea();
		output.setFont(new Font("맑은 고딕", Font.BOLD, 15));
		output.setEditable(false);
		JScrollPane scroll = new JScrollPane(output);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		input = new JTextField();
		sendBtn = new JButton("보내기");
		bottom.add("Center", input);
		bottom.add("East", sendBtn);
		Container c = this.getContentPane();
		c.add("Center", scroll);
		c.add("South", bottom);
		setBounds(300, 300, 300, 300);
		this.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					// InfoDTO dto = new InfoDTO(nickName, Info.EXIT);
					InfoDTO dto = new InfoDTO();
					dto.setNickName(nickName);
					dto.setCommand(Info.EXIT);
					writer.writeObject(dto);
					writer.flush();
				} catch (IOException io) {
					io.printStackTrace();
				}
			}

		});
		setVisible(true);

	}

	public JTextArea getOutput() {
		return output;
	}

	public void setOutput(JTextArea output) {
		this.output = output;
	}

	public JTextField getInput() {
		return input;
	}

	public void setInput(JTextField input) {
		this.input = input;
	}

	public JButton getSendBtn() {
		return sendBtn;
	}

	public void setSendBtn(JButton sendBtn) {
		this.sendBtn = sendBtn;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public ObjectInputStream getReader() {
		return reader;
	}

	public void setReader(ObjectInputStream reader) {
		this.reader = reader;
	}

	public ObjectOutputStream getWriter() {
		return writer;
	}

	public void setWriter(ObjectOutputStream writer) {
		this.writer = writer;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public static void main(String[] args) {
		new ChatClient().service();
	}

	private void service() {
		String serverIP = JOptionPane.showInputDialog(this, "서버 IP를 입력하세요", "127.0.0.1");
		if (serverIP == null || serverIP.length() == 0) {
			System.out.println("서버 IP가 입력되지 않았습니다.");
			System.exit(0);
		}
		nickName = JOptionPane.showInputDialog(this, "닉네임을 입력하세요", "닉네임");
		if (nickName == null || nickName.length() == 0) {
			nickName = "guest";
		}
		try {
			socket = new Socket(serverIP, 9500);
			reader = new ObjectInputStream(socket.getInputStream());
			writer = new ObjectOutputStream(socket.getOutputStream());
			System.out.println("전송 준비 완료");
		} catch (UnknownHostException e) {
			System.out.println("서버를 찾을 수 없습니다.");
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			System.out.println("서버와 연결이 안되었습니다.");
			e.printStackTrace();
			System.exit(0);
		}
		try {
			InfoDTO dto = new InfoDTO();
			dto.setCommand(Info.JOIN);
			dto.setNickName(nickName);
			writer.writeObject(dto);
			System.out.println("Join");
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Thread t = new Thread(this);
		t.start();
		input.addActionListener(this);
		sendBtn.addActionListener(this);
	}

	@Override
	public void run() {
		InfoDTO dto = null;
		while (true) {
			try {
				dto = (InfoDTO) reader.readObject();
				if (dto.getCommand() == Info.EXIT) {
					reader.close();
					writer.close();
					socket.close();
					System.exit(0);
				} else if (dto.getCommand() == Info.SEND) {
					output.append(dto.getMessage() + "\n");
					int pos = output.getText().length();
					output.setCaretPosition(pos);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {

				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			String msg = input.getText();
			InfoDTO dto = new InfoDTO();
			dto.setNickName(nickName);
			if (msg.equals("exit")) {
				dto.setCommand(Info.EXIT);
			} else {
				dto.setCommand(Info.SEND);
				dto.setMessage(msg);
			}
			writer.writeObject(dto);
			writer.flush();
			input.setText("");
		} catch (IOException io) {
			io.printStackTrace();
		}
	}
}
