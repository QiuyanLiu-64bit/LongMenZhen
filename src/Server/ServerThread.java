
package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import com.google.gson.Gson;

public class ServerThread extends Thread {
	Socket s = null;
	BufferedReader br = null;
	PrintStream ps = null;
	User user = null;
	FileOutputStream fos = null;
	DataOutputStream doc_write = null; // ��clientд�ļ�
	FileInputStream doc_read = null; // �������ļ�
	private String down_path = System.getProperty("user.dir") + "\\files\\"; // �ļ�����·��
	// ��־�ļ�·��
	private static final String LOG_FILE_PATH = System.getProperty("user.dir") + "\\Chatlog.txt";// ��־�ļ�·��
	Gson mGson;
	Transmission trans;
	int flag = 0;// 0:���� 1:�ļ�
	int file_length;
	String file_name_just = null;
	boolean file_is_create = true;
	boolean client_rec_first = true;

	int progressBarLength = 50; // �������ĳ���
	
	public ServerThread(Socket s) throws IOException {
		this.s = s;
	}
	//��Ŀ¼�ļ���ʱ������
	public static List<File> getFileSort(String path) {
	        List<File> list = getFiles(path, new ArrayList<File>());
	        if (list != null && list.size() > 0) {
	            Collections.sort(list, new Comparator<File>() {
	                public int compare(File file, File newFile) {
	                    if (file.lastModified() < newFile.lastModified()) {
	                        return 1;
	                    } else if (file.lastModified() == newFile.lastModified()) {
	                        return 0;
	                    } else {
	                        return -1;
	                    }
	                }
	            });
	        }
	        return list;
	}
	 //��ȡ����Ŀ¼�ļ�
	public static List<File> getFiles(String realpath, List<File> files) {
	        File realFile = new File(realpath);
	        if (realFile.isDirectory()) {
	            File[] subfiles = realFile.listFiles();
	            for (File file : subfiles) {
	                if (file.isDirectory()) {
	                    getFiles(file.getAbsolutePath(), files);
	                } else {
	                    files.add(file);
	                }
	            }
	        }
	        return files;
	}

	//���������ļ���Ŀ¼
	public void createDir() {
		File file = new File(down_path);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	//������־��¼
	public void startLog() {
		// ������־�ļ�
		File logFile = new File(LOG_FILE_PATH);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				System.out.println("������־�ļ�ʧ��");
				e.printStackTrace();
			}
		}
	}

	// �̰߳�ȫ����־��¼����
	private synchronized void writeToLog(String message) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
			String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
			writer.write(timeStamp + " ~ " + message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			String sender = null;
			String content = null;
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			ps = new PrintStream(s.getOutputStream());
			createDir();
			startLog();

			while (true) {
				if (flag == 0) {
					while ((content = readFromClient()) != null) {
						flag = 0;
						int user_list = Server.clients_string.valueSet().size();
						System.out.println("Msg_from_Client : " + content);

						StringTokenizer stringTokenizer = new StringTokenizer(content, "/@");
						String[] str_msg = new String[10];
						int j_ = 0;
						while (stringTokenizer.hasMoreTokens()) {
							str_msg[j_++] = stringTokenizer.nextToken();
						}

						sender = str_msg[0];// ������
						String command = str_msg[1];// �ź�

						if (command.equals("IP")) {
							System.out.println(str_msg[0] + " " + str_msg[1] + " " + str_msg[2] + " ********* ");
							user = new User(str_msg[0], str_msg[2]);
						} else if (command.equals("ALL")) {
							for (PrintStream ps_ : Server.clients_string.valueSet()) {
								ps_.println(content);
							}
							writeToLog(content + "\n");
						} else if (command.equals("LOG")){
							try {
								// ���ݲ�����ȡ��־�ļ���Ӧ��
								int line = Integer.parseInt(str_msg[2]);
								List<String> lines = Files.readAllLines(Paths.get(LOG_FILE_PATH));
								// �������������־�ļ��������򷵻ؿ�
								if (line >= lines.size()) {
									ps.println("LOG_END");
									break;
								}
								// ��ȡ��־�ļ���Ӧ��
								String log = lines.get(line);
								// ����־�ļ���Ӧ�з��͸��ͻ���
								ps.println("LOG@" + log);
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else if (command.equals("DELETE")) {
							System.out.println("CLOSE!");
							Server.clients_string.removeByValue(ps);
							try {
								if (br != null) {
									br.close();
								}
								if (ps != null) {
									ps.close();
								}
								if (s != null) {
									s.close();
								}
							} catch (IOException ex) {
								ex.printStackTrace();
							}
						} else if (command.equals("ADD")) {
							Server.clients_string.put(user, ps);
						} else if (command.equals("USERLIST")) {
							// �����û��б��ַ���
							String userlist = "";
							for (User user_ : Server.clients_string.map.keySet()) {
								String user_name = user_.getName();
								String user_ip = user_.getIp();
								userlist += ("@" + user_name + "@" + user_ip);
							}

							// �����пͻ��˹㲥�û��б�
							for (PrintStream ps_ : Server.clients_string.valueSet()) {
								ps_.println("Server" + "@" + "USERLIST" + userlist);
							}
						} else if (user_list > 6) {
							PrintStream ps_ = new PrintStream(s.getOutputStream());
							ps_.println("Server" + "@" + "MAX");
						} else if (command.equals("FILE_up")) {
							flag = 1;
							writeToLog(content + "\n");
							break;
						} else if (command.equals("FILE_down")) {
							System.out.println("�������յ��ͻ��˵��ļ��ϴ��ɹ����׼�������ļ�����");
							try {
//								file_name_just = getFileSort(down_path).get(0).getName();
								file_name_just = str_msg[2];
								String doc_path = new String(down_path + file_name_just);
								doc_read = new FileInputStream(doc_path);
								File file = new File(doc_path);
								mGson = new Gson();
								Transmission trans = new Transmission();
								trans.sender = str_msg[3];
								trans.transmissionType = 3;
								trans.fileName = file.getName();
								trans.fileLength = file.length();

//								for (PrintStream ps_ : Server.clients_string.valueSet()) {
									byte[] sendByte = new byte[1024];
									int length = 0;
									trans.transLength = 0;

									while ((length = doc_read.read(sendByte, 0, sendByte.length)) != -1) {
										trans.transLength += length;
										trans.content = Base64Utils.encode(sendByte);
										ps.println(mGson.toJson(trans));
										// ������Ȱٷֱ�
										int progressPercentage = (int) (100 * trans.transLength / trans.fileLength);

										// �����������ַ���
										int fill = (progressPercentage * progressBarLength) / 100;
										String progressBar = new String(new char[fill]).replace('\0', '#') +
												new String(new char[progressBarLength - fill]).replace('\0', ' ');

										// ��ʾ������
										System.out.print("�����ļ�����: [" + progressBar + "] " + progressPercentage + "%\r");
										// ����Ƿ�����ļ�����
										if (trans.transLength == trans.fileLength) {
											// ��ӡ���յĽ�����״̬
											System.out.println("�����ļ�����: [" + progressBar + "] " + progressPercentage + "%");
										}
										ps.flush();
									}
//									System.out.println("һ�����ؽ���");
//								}
								System.out.println("Server����ִ�н���");
							}
							catch (FileNotFoundException e1){
								System.out.println("�ļ������ڣ�");
							}
							catch (IOException e2) {
								System.out.println("�ļ�д���쳣");
							} 
							finally {
								try {
									doc_read.close();
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
							// break;
						} else if (command.equals("NALL")) {
							User user_s1 = null;
							User user_s2 = null;
							for (User user_ : Server.clients_string.map.keySet()){
								if (user_.getName().equals(sender)) {
									user_s1 = user_;
								}
								if (user_.getName().equals(str_msg[3])) {
									user_s2 = user_;
								}
								if(user_s1 != null && user_s2 != null) {
									break;
								}
							}
							System.out.println("The whisper msg!");
							Server.clients_string.map.get(user_s1)
									.println("��� " + str_msg[3] + " ˵ : " + str_msg[2] + "@" + "ONLY");
							Server.clients_string.map.get(user_s2)
									.println(Server.clients_string.getKeyByValue(ps).getName() + " ����˵ : "
											+ str_msg[2] + "@" + "ONLY");
							writeToLog(content + "\n");
						}
					} // while
				} // if
				else if (flag == 1) {
						mGson = new Gson();
						String file_name = null;
						while ((content = readFromClient()) != null) {
							trans = mGson.fromJson(content, Transmission.class);
							long fileLength = trans.fileLength;
							long transLength = trans.transLength;
							file_name_just = trans.fileName;
							file_name = trans.fileName;
							if(file_is_create) {
								fos = new FileOutputStream(
										new File(down_path + trans.fileName));
								file_is_create = false;
							}
							byte[] b = Base64Utils.decode(trans.content.getBytes());
							fos.write(b, 0, b.length);
							// ������Ȱٷֱ�
							int progressPercentage = (int) (100 * transLength / fileLength);

							// �����������ַ���
							int fill = (progressPercentage * progressBarLength) / 100;
							String progressBar = new String(new char[fill]).replace('\0', '#') +
									new String(new char[progressBarLength - fill]).replace('\0', ' ');

							// ��ʾ������
							System.out.print("�����ļ�����: [" + progressBar + "] " + progressPercentage + "%\r");
							if (transLength == fileLength) {
								System.out.println("�����ļ�����: [" + progressBar + "] " + progressPercentage + "%");
								file_is_create = true;
								fos.flush();
								fos.close();
								break;
							}
						}
						System.out.println("�����ļ�����");
						for (PrintStream ps_ : Server.clients_string.valueSet()) {
							ps_.println("Server" + "@" + "FILE_up_ok" + "@" + file_name + "@" + sender);
						}
						flag = 0;
					} // else if
					
				} // while
		} // try
		catch (IOException e1) {
			System.out.println("�ļ�д���쳣 : ServerThread�߳� run() e:" + e1.getMessage());
			Server.clients_string.removeByValue(ps);
			try {
				if (br != null) {
					br.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (s != null) {
					s.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} catch (Exception e3) {
			try {
				System.out.println(e3.getMessage());
				if (s != null) {
					s.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public String readFromClient() {
		try {
			return br.readLine();
		} catch (IOException e) {
			Server.clients_string.removeByValue(ps);
		}
		return null;
	}
}
