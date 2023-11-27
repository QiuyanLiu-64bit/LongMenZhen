package Client;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.google.gson.Gson;

import Client.Base64Utils;
import Client.Transmission;

public class Client extends JFrame {
	/**
	 * 
	 */
	// 变量声明
	private static final long serialVersionUID = 6704231622520334518L;

	private JFrame frame;
	private JTextPane text_show;
	private JTextField txt_msg;
	private JLabel info_name;
	private JLabel info_ip;
	private JButton btn_send;
	private JButton btn_pic;
	private JButton btn_file;
	private JPanel northPanel;
	private JPanel southPanel;
	private JScrollPane rightScroll;
	private JScrollPane leftScroll;
	private JScrollPane listScroller;
	private JSplitPane centerSplit;
//	private JComboBox<String> comboBox;
	private SimpleAttributeSet attrset;

	private DefaultListModel<String> listModel;
	private DefaultListModel<String> model;
	private JList<String> userList;
	private JList<String> list;

	private Socket socket;
	private static PrintWriter writer; // 向server写消息
	private static BufferedReader reader; // 读server消息
	private static FileInputStream doc_read; // 读本地文件
	private static FileOutputStream fos; // 写本地文件
	private MessageThread messageThread;// 负责接收消息的线程
	private Map<String, User> onLineUsers = new HashMap<String, User>();// 所有在线用户
	private boolean isConnected = false;
	private int port = 8080;// 服务器端口
	private String ip = "127.0.0.1";// 140.210.---------212.154
	private String name;
	private String client_path = System.getProperty("user.dir") + "\\"; // 工作路径
	private String pic_path = null;
	private String file_path = null;
	private String UserValue = "";
	private int info_ip_ = 0;
	private int flag = 0;
	private Gson mGson;
	private boolean file_is_create = true;
	private Transmission trans;
	private ByteArrayInputStream bais = null;
	private ByteArrayOutputStream baos = null;
	private Boolean stopflag = false;

	private final static int THUMBNAIL_WIDTH = 240; // 缩略图的宽度
	private final static int THUMBNAIL_HEIGHT = 135; // 缩略图的高度
	int progressBarLength = 50; // 进度条的长度

	private HashSet<String> selectedItems;

	// 测试主函数
//	public static void main(String[] args) {
//		new Client("bbb");
//	}

	// 构造方法
	public Client(String n) {
		this.name = n;
		frame = new JFrame(name);
		frame.setVisible(true); // 可见
		frame.setBackground(Color.PINK);
		frame.setResizable(false); // 大小不可变

		info_name = new JLabel(name);
		text_show = new JTextPane();
		text_show.setEditable(false);
		// text_show.setSize(300, 300);
		// text_show.setForeground(Color.BLACK);
		// text_show.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		// text_show.setLayout(null);
		attrset = new SimpleAttributeSet();
		StyleConstants.setFontSize(attrset, 15);
		txt_msg = new JTextField();
		btn_send = new JButton("发送");
		btn_pic = new JButton("选择图片");
		btn_file = new JButton("选择文件");
//		comboBox = new JComboBox<>();
//		comboBox.addItem("ALL");

		// 初始化用户列表模型
		listModel = new DefaultListModel<>();
		userList = new JList<>(listModel);
		userList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		userList.setForeground(Color.DARK_GRAY); // 设置文字颜色

		model = new DefaultListModel<>();
		model.addElement("ALL");
		list = new JList<>(model);

		// 设置选择模式为多选
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		northPanel = new JPanel();
		northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		JLabel info_a = new JLabel("UserName : ");
		info_a.setForeground(Color.WHITE);
		info_a.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_a);
		info_name.setForeground(Color.WHITE);
		info_name.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.add(info_name);
		TitledBorder info_b = new TitledBorder("My Info");
		info_b.setTitleColor(Color.WHITE);
		info_b.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		northPanel.setBorder(info_b);

		rightScroll = new JScrollPane(text_show);
		TitledBorder info_c = new TitledBorder("消息");
		info_c.setTitleColor(Color.DARK_GRAY);
		info_c.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		rightScroll.setBorder(info_c);
		leftScroll = new JScrollPane(userList);
		listScroller = new JScrollPane(list);

		TitledBorder info_d = new TitledBorder("在线用户");
		info_d.setTitleColor(Color.DARK_GRAY);
		info_d.setTitleFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		leftScroll.setBorder(info_d);

		southPanel = new JPanel();
		southPanel.setLayout(null);
		// 创建滚动面板包含JList
		listScroller.setBounds(30, 110, 100, 35); // 和comboBox一样的位置和大小
		txt_msg.setBounds(0, 0, 1100, 100);
		txt_msg.setBackground(Color.pink);
		btn_send.setBounds(1101, 0, 80, 100);
		btn_send.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
		btn_send.setForeground(Color.DARK_GRAY);
//		comboBox.setBounds(30, 110, 100, 35);
//		comboBox.setForeground(Color.DARK_GRAY);

		btn_pic.setBounds(360, 110, 100, 35);
		btn_pic.setForeground(Color.DARK_GRAY);
		btn_pic.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
		btn_file.setBounds(490, 110, 100, 35);
		btn_file.setForeground(Color.DARK_GRAY);
		btn_file.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 15));
//		southPanel.add(comboBox);
		southPanel.add(listScroller);
		southPanel.add(txt_msg);
		southPanel.add(btn_send);
		southPanel.add(btn_pic);
		southPanel.add(btn_file);

		centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroll, rightScroll);
		centerSplit.setDividerLocation(200);

		frame.setLayout(null);
		northPanel.setBounds(0, 0, 1200, 80);
		northPanel.setBackground(Color.pink);
		centerSplit.setBounds(0, 90, 1200, 500);
		southPanel.setBounds(0, 600, 1200, 200);
		frame.add(northPanel);
		frame.add(centerSplit);
		frame.add(southPanel);
		frame.setBounds(0, 0, 1200, 800);
		int screen_width = Toolkit.getDefaultToolkit().getScreenSize().width;
		int screen_height = Toolkit.getDefaultToolkit().getScreenSize().height;
		frame.setLocation((screen_width - frame.getWidth()) / 2, (screen_height - frame.getHeight()) / 2);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		ConnectServer();// 连接服务器

		// 存储选中状态的集合
		selectedItems = new HashSet<>();

		// 添加鼠标监听器来处理点击事件
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int index = list.locationToIndex(e.getPoint());
				if (index != -1) {
					String item = model.getElementAt(index);
					if ("ALL".equals(item)) {
						// 如果点击的是 "ALL"，清除所有选中项，并且只选择 "ALL"
						selectedItems.clear();
						selectedItems.add("ALL");
					} else {
						// 如果点击的是其他项，切换其选中状态
						if (selectedItems.contains(item)) {
							selectedItems.remove(item);
						} else {
							selectedItems.add(item);
						}
					}
					list.repaint(); // 请求重新绘制列表，以更新显示
				}
			}
		});

		list.setCellRenderer(new ListCellRenderer<String>() {
			@Override
			public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
				if ("ALL".equals(value)) {
					JLabel label = new JLabel(value);
					if (isSelected) {
						// 设置选中状态的背景和前景色
						label.setBackground(list.getSelectionBackground());
						label.setForeground(list.getSelectionForeground());
						label.setOpaque(true); // 使背景色可见
					} else {
						// 设置非选中状态的背景和前景色
						label.setBackground(list.getBackground());
						label.setForeground(list.getForeground());
						label.setOpaque(false);
					}
					return label;
				} else {
					// 对于其他项，显示复选框
					JCheckBox checkBox = new JCheckBox(value);
					checkBox.setSelected(selectedItems.contains(value));
					checkBox.setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
					checkBox.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
					return checkBox;
				}
			}
		});

		// txt_msg回车键时事件
		txt_msg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ComboBoxValue();
			}
		});

		// btn_send单击发送按钮时事件
		btn_send.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ComboBoxValue();
			}

		});

		// btn_pic发送图片事件
		btn_pic.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Filechose();
				try {
					File file = new File(pic_path);
					if (pic_path != null) {
						doc_read = new FileInputStream(pic_path);
						sendMessage(name + "@" + "FILE_up" + "@" + file.getName() + "@" + name); // 上传图片指令
					}
					mGson = new Gson();
					Transmission trans = new Transmission();
					trans.sender = name;
					trans.transmissionType = 3;
					trans.fileName = file.getName();
					trans.fileLength = file.length();
					trans.transLength = 0;
					byte[] sendByte = new byte[1024];
					int length = 0;
					while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
						trans.transLength += length;
						trans.content = Base64Utils.encode(sendByte);
						writer.write(mGson.toJson(trans) + "\r\n");
						// 计算进度百分比
						int progressPercentage = (int) (100 * trans.transLength / trans.fileLength);

						// 创建进度条字符串
						int fill = (progressPercentage * progressBarLength) / 100;
						String progressBar = new String(new char[fill]).replace('\0', '#') +
								new String(new char[progressBarLength - fill]).replace('\0', ' ');

						// 显示进度条
						System.out.print("上传文件进度: [" + progressBar + "] " + progressPercentage + "%\r");
						// 检查是否完成文件接收
						if (trans.transLength == trans.fileLength) {
							// 打印最终的进度条状态
							System.out.println("上传文件进度: [" + progressBar + "] " + progressPercentage + "%");
						}
						writer.flush();
					}
					System.out.println("文件上传完毕");
				} catch (FileNotFoundException e1) {
					System.out.println("文件不存在！");
				} catch (IOException e2) {
					System.out.println("文件写入异常");
				} finally {
					try {
						doc_read.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// btn_file发送文件事件
		btn_file.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Filechose();
				try{
					File file = new File(file_path);
					if (file_path != null) {
						doc_read = new FileInputStream(file_path);
						sendMessage(name + "@" + "FILE_up" + "@" + file.getName() + "@" + name); // 上传文件指令
					}
					mGson = new Gson();
					Transmission trans = new Transmission();
					trans.sender = name;
					trans.transmissionType = 3;
					trans.fileName = file.getName();
					trans.fileLength = file.length();
					trans.transLength = 0;
					byte[] sendByte = new byte[1024];
					int length = 0;
					while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
						trans.transLength += length;
						trans.content = Base64Utils.encode(sendByte);
						writer.write(mGson.toJson(trans) + "\r\n");
						// 计算进度百分比
						int progressPercentage = (int) (100 * trans.transLength / trans.fileLength);

						// 创建进度条字符串
						int fill = (progressPercentage * progressBarLength) / 100;
						String progressBar = new String(new char[fill]).replace('\0', '#') +
								new String(new char[progressBarLength - fill]).replace('\0', ' ');

						// 显示进度条
						System.out.print("上传文件进度: [" + progressBar + "] " + progressPercentage + "%\r");
						// 检查是否完成文件接收
						if (trans.transLength == trans.fileLength) {
							// 打印最终的进度条状态
							System.out.println("上传文件进度: [" + progressBar + "] " + progressPercentage + "%");
						}
						writer.flush();
					}
					System.out.println("文件上传完毕");
				} catch (FileNotFoundException e1) {
					System.out.println("文件不存在！");
				} catch (IOException e2) {
					System.out.println("文件写入异常");
				} finally {
					try {
						doc_read.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		// 关闭窗口时事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (isConnected) {
					try {
						// 断开连接
						boolean flag = ConnectClose();
						if (!flag) {
							throw new Exception("断开连接发生异常！");
						} else {
							JOptionPane.showMessageDialog(frame, "成功断开!");
							txt_msg.setEnabled(false);
							btn_send.setEnabled(false);
						}
					} catch (Exception e4) {
						JOptionPane.showMessageDialog(frame, "断开连接服务器异常：" + e4.getMessage(), "错误",
								JOptionPane.ERROR_MESSAGE);
					}
				} else {
					ConnectServer();
					txt_msg.setEnabled(true);
					btn_send.setEnabled(true);
				}

			}
		});

//		comboBox.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent evt) {
//				try {
//					if (ItemEvent.SELECTED == evt.getStateChange()) {
//						// 这个判断是选择只会得到一个结果，如果没有判断，会得到两个相同的值，从而获取不到所要选中的值。。
//						String value = comboBox.getSelectedItem().toString();
//						System.out.println(value);
//						UserValue = value;
//					}
//				} catch (Exception e) {
//					System.out.println("GGGFFF");
//				}
//
//			}
//		});

	}

	// 连接服务器
	private void ConnectServer() {
		try {
			socket = new Socket(ip, port);// 根据端口号和服务器IP建立连接
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			info_ip = new JLabel(socket.getLocalAddress().toString());
			info_ip.setForeground(Color.WHITE);
			info_ip.setFont(new Font("Microsoft JhengHei Light", Font.PLAIN, 20));
			if (info_ip_ == 0) {
				northPanel.add(info_ip);
				JOptionPane.showMessageDialog(frame, name + " 连接服务器成功!");
			}
			info_ip_++;
			// 发送客户端基本信息(用户名和IP地址)
			sendMessage(name + "@" + "IP" + "@" + socket.getLocalAddress().toString());
			// for(int i=0; i<100; i++);
			sendMessage(name + "@" + "ADD");

			// 开启不断接收消息的线程
			messageThread = new MessageThread(reader);
			messageThread.start();
			isConnected = true;// 已经连接上了

			frame.setVisible(true);

		} catch (Exception e) {
			isConnected = false;// 未连接上
			JOptionPane.showMessageDialog(frame, "连接服务器异常：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	// 断开连接
	@SuppressWarnings("deprecation")
	public synchronized boolean ConnectClose() {
		try {

			sendMessage(name + "@" + "DELETE");// 发送断开连接命令给服务器
			messageThread.stop();// 停止接受消息线程
			// 释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;
			return true;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, name + " 断开连接服务器成功!");
			isConnected = true;
			return false;
		}
	}

	// 群聊、私聊选择，打包消息，更新列表
	public void ComboBoxValue() {
		sendMessage(name + "@" + "USERLIST");
		String message = txt_msg.getText();
		if (message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 获取选中的用户列表
		List<String> selectedUsers = new ArrayList<>(selectedItems);

		if ((selectedUsers.size() == 1 && "ALL".equals(selectedUsers.get(0))) || selectedUsers.size() == 0) {
			sendMessage(frame.getTitle() + "@" + "ALL" + "@" + message + "@" + "not");

		} else {
			// 向选中的每个用户发送消息
			for (String user : selectedUsers) {
                sendMessage(frame.getTitle() + "@" + "NALL" + "@" + message + "@" + user + "@" + "not");
			}
		}
		txt_msg.setText(null);
	}

	// 文件选择，输出绝对路径
	public void Filechose() {
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(new File(""));
		jfc.addChoosableFileFilter(new MyFileFilter());
		// jfc.
		JFrame pic_chose = new JFrame();
		pic_chose.setVisible(false);
		pic_chose.setBounds(100, 100, 800, 600);
		if (jfc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
			File selectedFile = jfc.getSelectedFile();
			String filePath = selectedFile.getAbsolutePath();
			if (filePath.endsWith(".png") || filePath.endsWith(".jpg")) {
				pic_path = filePath;  // 如果文件是 PNG 或 JPG，赋值给 pic_path 和 file_path
				file_path = filePath;
				System.out.println("Image file path: " + pic_path);
			} else {
				file_path = filePath;  // 如果文件不是 PNG 或 JPG，赋值给 file_path
				System.out.println("Other file path: " + file_path);
			}
		}
	}

	// 文件类型过滤
	static class MyFileFilter extends FileFilter {
		public boolean accept(File pathname) {
			if (pathname.getAbsolutePath().endsWith(".gif") || pathname.isDirectory()
					|| pathname.getAbsolutePath().endsWith(".png") || pathname.getAbsolutePath().endsWith(".jpg"))
				return true;
			return false;
		}

		public String getDescription() {
			return "图像文件";
		}
	}

	// 发送消息
	public static void sendMessage(String message) {
		writer.println(message);
		writer.flush();
	}

	//创建downloads和thumbnail_imgs文件夹
	public void createDir() {
		File downloads = new File(client_path + "downloads");
		File thumbnail_imgs = new File(client_path + "thumbnail_imgs");
		if (!downloads.exists()) {
			downloads.mkdir();
		}
		if (!thumbnail_imgs.exists()) {
			thumbnail_imgs.mkdir();
		}
	}

	// 消息处理线程

	// ------------------------------------------------------------------------------------
	// 不断接收消息的线程
	class MessageThread extends Thread {
		private final BufferedReader reader;

		// 接收消息线程的构造方法
		public MessageThread(BufferedReader reader) {
			this.reader = reader;
		}

		public void LogReciver () {// 之后可修改整合为文件的接收方法
			System.out.println("客户端接收日志");
			String message = "";
			try {
				int lineCount = 0;
				while (!message.equals("LOG_END")) {
					sendMessage(name + "@" + "LOG" + "@" + lineCount);
					message = reader.readLine();
					if (message.startsWith("LOG@")){// 需要被扩展！！
						lineCount++;
					}
					else return;
					// 日志格式：时间 发送者ID@命令@内容@附加信息
					/*示例:
					 	20:01:04 ~ 1@ALL@hello@not
						20:01:04 ~ 10000@ALL@ok@not
						20:06:16 ~ 1@NALL@private message@10000@not
						20:06:42 ~ 10000@FILE_up@1.png@10000
						20:06:42 ~ 1@FILE_up@testText.md@1
						02:34:49 ~ 10000@ALL@test later@not
						02:37:01 ~ 10000@ALL@later now on@not
						<注意需要保留此空行>
						*/

					// 移除字符串头部的 "LOG@"
					message = message.replace("LOG@", "");
					String[] parts = message.split(" ~ ");
					String timestamp = parts[0]; // 时间
					String[] messageParts = parts[1].split("@");

					String senderId = messageParts[0];
					String command = messageParts[1];
					String content = messageParts[2];

					// 根据指令类型处理消息
					Document docs;
					switch (command) {
						case "ALL": // 群发消息
							docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(),
										"[" + timestamp + "]\r\n" + senderId + " 说 : " + content + "\r\n\n", attrset);// 对文本进行追加
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							break;
						case "NALL": // 私发消息
							if (messageParts[3].equals(name)) {
								docs = text_show.getDocument();
								try {
									docs.insertString(docs.getLength(),
											"[" + timestamp + "]\r\n" + senderId + " 对你说 : " + content + "\r\n\n", attrset);// 对文本进行追加
								} catch (BadLocationException e) {
									e.printStackTrace();
								}
							}
							break;
						case "FILE_up": // 文件下载
							sendMessage(name + "@" + "FILE_down" + "@" + messageParts[2] + "@" + messageParts[3]);
							System.out.println("客户端准备消息接受 。 。 。 ");

							mGson = new Gson();
							while ((message = reader.readLine()) != null) {
								trans = mGson.fromJson(message, Transmission.class);
								long fileLength = trans.fileLength;
								long transLength = trans.transLength;
								if (file_is_create) {
									fos = new FileOutputStream(new File(client_path + "downloads\\" + trans.fileName));
									file_is_create = false;
								}
								byte[] b = Base64Utils.decode(trans.content.getBytes());
								fos.write(b, 0, b.length);

								// 计算进度百分比
								int progressPercentage = (int) (100 * transLength / fileLength);

								// 创建进度条字符串
								int fill = (progressPercentage * progressBarLength) / 100;
								String progressBar = new String(new char[fill]).replace('\0', '#') +
										new String(new char[progressBarLength - fill]).replace('\0', ' ');

								// 显示进度条
								System.out.print("接收文件进度: [" + progressBar + "] " + progressPercentage + "%\r");

								if (transLength == fileLength) {
									System.out.println("接收文件进度: [" + progressBar + "] " + progressPercentage + "%");
									file_is_create = true;
									fos.flush();
									fos.close();
									if (trans.fileName.endsWith(".jpg")||trans.fileName.endsWith(".png")) {
										// 创建并保存缩略图
										try {
											//创建缩略图
											BufferedImage originalImage = ImageIO.read(new File(client_path + "downloads\\" + trans.fileName));
											BufferedImage thumbnailImage = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
											Graphics2D graphics2D = thumbnailImage.createGraphics();
											graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
											graphics2D.drawImage(originalImage, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
											graphics2D.dispose();
											// 保存缩略图
											File thumbnailFile = new File(client_path + "thumbnail_imgs\\" + trans.fileName);
											ImageIO.write(thumbnailImage, "jpg", thumbnailFile);
										} catch (IOException e) {
											e.printStackTrace();
											System.out.println("生成缩略图时发生错误!");
										}

										ImageIcon icon = new ImageIcon(
												client_path + "thumbnail_imgs\\" + trans.fileName);
										// icon.
										StyledDocument doc = text_show.getStyledDocument();
										docs = text_show.getDocument();
										try {
											docs.insertString(docs.getLength(),
													"[" + timestamp + "]\r\n" + trans.sender + " 发送了一张图片: " + "\r\n", attrset);// 对文本进行追加
											text_show.setCaretPosition(doc.getLength());
											text_show.insertIcon(icon);
											docs = text_show.getDocument();
											docs.insertString(docs.getLength(), "\r\n", attrset);
										} catch (BadLocationException e) {
											e.printStackTrace();
										}
									} else{
										URL imageUrl = getClass().getClassLoader().getResource("file_icon.png");
										ImageIcon icon = new ImageIcon(imageUrl);
										StyledDocument doc = text_show.getStyledDocument();
										docs = text_show.getDocument();
										try {
											docs.insertString(docs.getLength(),
													"[" + timestamp + "]\r\n" + trans.sender + " 发送了一份文件\r\n" + "下载路径为: " + client_path + "downloads\\" + trans.fileName + "\r\n\n", attrset);// 对文本进行追加
											text_show.setCaretPosition(doc.getLength());
											text_show.insertIcon(icon);
											docs = text_show.getDocument();
											docs.insertString(docs.getLength(), "\r\n", attrset);
										} catch (BadLocationException e) {
											e.printStackTrace();
										}
									}
									break;
								}
							}
							System.out.println("文件下载执行完毕");
							break;
					}
				}
				System.out.println("日志重建执行完毕");
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("日志重建时发生错误!");
			} catch (Exception e2) {
				e2.printStackTrace();
				System.out.println("日志重建时发生错误!");
			}
		}

		@SuppressWarnings("unlikely-arg-type")
		public void run() {
			String message = "";
			createDir();
			LogReciver();
			sendMessage(name + "@" + "USERLIST");

			while (true) {
				try {
					if (flag == 0) {
						message = reader.readLine();
						StringTokenizer stringTokenizer = new StringTokenizer(message, "/@");
						// 服务器消息处理
						String[] str_msg = new String[10];
						int j_ = 0;
						while (stringTokenizer.hasMoreTokens()) {
							str_msg[j_++] = stringTokenizer.nextToken();
						}
						String command = str_msg[1];// 信号
						// 服务器已关闭信号
						if (command.equals("SERVERClOSE")) {
							Document docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(), "服务器已关闭!\\r\\n", attrset);// 对文本进行追加
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							// text_show.add("服务器已关闭!\r\n", null);
							closeCon();// 关闭连接
							return;// 结束线程
						}
						// 上线更新列表信号
						else if (command.equals("ADD")) {
							String username = "";
							String userIp = "";
							username = str_msg[0];
							userIp = socket.getLocalAddress().toString();
							User user = new User(username, userIp);
							onLineUsers.put(username, user);
							listModel.addElement(username);
							model.addElement(username);
//							comboBox.addItem(username);
						}
						// 下线更新列表信号
						else if (command.equals("DELETE")) {
							String username = str_msg[0];
							User user = (User) onLineUsers.get(username);
							onLineUsers.remove(user);
							listModel.removeElement(username);
							model.removeElement(username);
//							comboBox.removeItem(username);
						}
						// 加载用户列表
						else if (command.equals("USERLIST")) {
							String username = null;
							String userIp = null;
							for (int i = 2; i < str_msg.length; i += 2) {
								if (str_msg[i] == null)
									break;
								username = str_msg[i];
								userIp = str_msg[i + 1];
								User user = new User(username, userIp);
								onLineUsers.put(username, user);
								if (listModel.contains(username))
									;
								else {
									listModel.addElement(username);
									model.addElement(username);
								}
//								int len = comboBox.getItemCount();
//								int _i = 0;
//								for (; _i < len; _i++) {
//									if (comboBox.getItemAt(_i).toString().equals(username))
//										break;
//								}
//								if (_i == len)
//									comboBox.addItem(username);
							}
						}
						// 人数已达上限信号
						else if (command.equals("MAX")) {
							closeCon();// 关闭连接
							JOptionPane.showMessageDialog(frame, "服务器人数已满,请稍后再试！", "提示", JOptionPane.CANCEL_OPTION);
							return;// 结束线程
						}
						// 群发消息
						else if (command.equals("ALL")) {
							SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
							String time = df.format(new java.util.Date());
							Document docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(),
										"[" + time + "]\r\n" + str_msg[0] + " 说 : " + str_msg[2] + "\r\n\n", attrset);// 对文本进行追加
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							// text_show.add(, null);// 普通消息
						}
						// 私聊消息
						else if (command.equals("ONLY")) {
							SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
							String time = df.format(new java.util.Date());
							Document docs = text_show.getDocument();
							try {
								docs.insertString(docs.getLength(), "[" + time + "]\r\n" + str_msg[0] + "\r\n\n",
										attrset);// 对文本进行追加
							} catch (BadLocationException e) {
								e.printStackTrace();
							}
							// text_show.add(, null);// 普通消息
						}
						// 下载图片
						else if (command.equals("FILE_up_ok")) {
							sendMessage(name + "@" + "FILE_down" + "@" + str_msg[2] + "@" + str_msg[3]);
							flag = 1;
							// break;
						}
						str_msg = null; // 消息数组置空
					} // if(flag == 0)
					else if (flag == 1) {
						System.out.println("客户端准备消息接受 。 。 。 ");

						mGson = new Gson();
						while ((message = reader.readLine()) != null) {
							trans = mGson.fromJson(message, Transmission.class);
							long fileLength = trans.fileLength;
							long transLength = trans.transLength;
							if (file_is_create) {
								fos = new FileOutputStream(new File(client_path + "downloads\\" + trans.fileName));
								file_is_create = false;
							}
							byte[] b = Base64Utils.decode(trans.content.getBytes());
							fos.write(b, 0, b.length);

							// 计算进度百分比
							int progressPercentage = (int) (100 * transLength / fileLength);

							// 创建进度条字符串
							int fill = (progressPercentage * progressBarLength) / 100;
							String progressBar = new String(new char[fill]).replace('\0', '#') +
									new String(new char[progressBarLength - fill]).replace('\0', ' ');

							// 显示进度条
							System.out.print("接收文件进度: [" + progressBar + "] " + progressPercentage + "%\r");

							if (transLength == fileLength) {
								System.out.println("接收文件进度: [" + progressBar + "] " + progressPercentage + "%");
								file_is_create = true;
								fos.flush();
								fos.close();
								if (trans.fileName.endsWith(".jpg")||trans.fileName.endsWith(".png")) {
									// 创建并保存缩略图
									try {
										//创建缩略图
										BufferedImage originalImage = ImageIO.read(new File(client_path + "downloads\\" + trans.fileName));
										BufferedImage thumbnailImage = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_RGB);
										Graphics2D graphics2D = thumbnailImage.createGraphics();
										graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
										graphics2D.drawImage(originalImage, 0, 0, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, null);
										graphics2D.dispose();
										// 保存缩略图
										File thumbnailFile = new File(client_path + "thumbnail_imgs\\" + trans.fileName);
										ImageIO.write(thumbnailImage, "jpg", thumbnailFile);
									} catch (IOException e) {
										e.printStackTrace();
										System.out.println("生成缩略图时发生错误!");
									}

									ImageIcon icon = new ImageIcon(
											client_path + "thumbnail_imgs\\" + trans.fileName);
									// icon.
									SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
									String time = df.format(new java.util.Date());
									StyledDocument doc = text_show.getStyledDocument();
									Document docs = text_show.getDocument();
									try {
										docs.insertString(docs.getLength(),
												"[" + time + "]\r\n" + trans.sender + " 发送了一张图片: " + "\r\n", attrset);// 对文本进行追加
										text_show.setCaretPosition(doc.getLength());
										text_show.insertIcon(icon);
										docs = text_show.getDocument();
										docs.insertString(docs.getLength(), "\r\n", attrset);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								} else{
//									URL classpathRoot = getClass().getClassLoader().getResource("");
//									System.out.println("Classpath root: " + classpathRoot);
									URL imageUrl = getClass().getClassLoader().getResource("file_icon.png");
									ImageIcon icon = new ImageIcon(imageUrl);
									SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");// 设置日期格式
									String time = df.format(new java.util.Date());
									StyledDocument doc = text_show.getStyledDocument();
									Document docs = text_show.getDocument();
									try {
										docs.insertString(docs.getLength(),
												"[" + time + "]\r\n" + trans.sender + " 发送了一份文件\r\n" + "下载路径为: " + client_path + "downloads\\" + trans.fileName + "\r\n\n", attrset);// 对文本进行追加
										text_show.setCaretPosition(doc.getLength());
										text_show.insertIcon(icon);
										docs = text_show.getDocument();
										docs.insertString(docs.getLength(), "\r\n", attrset);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								}
								break;
							}
						}
						System.out.println("文件下载执行完毕");
						flag = 0;
					} /// else if
				} // try
				catch (IOException e1) {
					// ConnectServer();
					e1.printStackTrace();
					System.out.println("客户端接受 消息 线程 run() e1:" + e1.getMessage());
					break;
				} catch (Exception e2) {
					// ConnectServer();
					e2.printStackTrace();
					System.out.println("客户端接收 消息 线程 run() e2:" + e2.getMessage());
					break;
				}
			} // while
		} // run

		// 服务器停止后，客户端关闭连接。
		// synchronized用来修饰一个方法或者一个代码块的时候，能够保证在同一时刻只有一个线程执行该段代码。
		public synchronized void closeCon() throws Exception {
			listModel.removeAllElements();// 清空用户列表
			// 被动的关闭连接释放资源
			if (reader != null) {
				reader.close();
			}
			if (writer != null) {
				writer.close();
			}
			if (socket != null) {
				socket.close();
			}
			isConnected = false;// 修改状态为断开
		}
	}
}