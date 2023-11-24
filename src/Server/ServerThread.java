
package Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import com.google.gson.Gson;

public class ServerThread extends Thread {
	Socket s = null;
	BufferedReader br = null;
	PrintStream ps = null;
	User user = null;
	FileOutputStream fos = null;
	DataOutputStream doc_write = null; // ��clientд�ļ�
	FileInputStream doc_read = null; // �������ļ�
	Gson mGson;
	Transmission trans;
	int flag = 0;
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
	
	public void run() {
		try {
			String content = null;
			br = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
			ps = new PrintStream(s.getOutputStream());

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
						String command = str_msg[1];// �ź�

						if (command.equals("IP")) {
							System.out.println(str_msg[0] + " " + str_msg[1] + " " + str_msg[2] + " ********* ");
							user = new User(str_msg[0], str_msg[2]);
						} else if (command.equals("ALL")) {
							for (PrintStream ps_ : Server.clients_string.valueSet()) {
								ps_.println(content);
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
							String userlist = "";
							for (User user_ : Server.clients_string.map.keySet()) {
								String user_name = user_.getName();
								String user_ip = user_.getIp();
								userlist += ("@" + user_name + "@" + user_ip);
							}
							PrintStream ps_ = new PrintStream(s.getOutputStream());
							ps_.println("Server" + "@" + "USERLIST" + userlist);
						} else if (user_list > 6) {
							PrintStream ps_ = new PrintStream(s.getOutputStream());
							ps_.println("Server" + "@" + "MAX");
						} else if (command.equals("PIC_up")) {
							flag = 1;
							break;
						} else if (command.equals("PIC_down")) {
							System.out.println("�������յ��ͻ��˵��ļ��ϴ��ɹ����׼�������ļ�����");
							try {
								file_name_just = getFileSort("D:\\code\\LongMenZhen\\LongMenZhen\\img\\").get(0).getName();
								String doc_path = new String("D:\\code\\LongMenZhen\\LongMenZhen\\img\\" + file_name_just);
								doc_read = new FileInputStream(doc_path);
								File file = new File(doc_path);
								mGson = new Gson();
								Transmission trans = new Transmission();
								trans.transmissionType = 3;
								trans.fileName = file.getName();
								trans.fileLength = file.length();
								trans.transLength = 0;
//								for (PrintStream ps_ : Server.clients_string.valueSet()) {
									byte[] sendByte = new byte[1024];
									int length = 0;
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
						} else {
							User user_ss = null;
							for (User user_ : Server.clients_string.map.keySet())
								if (user_.getName().equals(command)) {
									user_ss = user_;
									break;
								}
							System.out.println("The whisper msg!");
							Server.clients_string.map.get(user_ss)
									.println(Server.clients_string.getKeyByValue(ps).getName() + " whispers to you : "
											+ str_msg[2] + "@" + "ONLY");
						}
					} // while
				} // if
				else if (flag == 1) {
						mGson = new Gson();
						while ((content = readFromClient()) != null) {
							trans = mGson.fromJson(content, Transmission.class);
							long fileLength = trans.fileLength;
							long transLength = trans.transLength;
							file_name_just = trans.fileName;
							if(file_is_create) {
								fos = new FileOutputStream(
										new File("D:\\code\\LongMenZhen\\LongMenZhen\\img\\" + trans.fileName));
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
						System.out.println("�ϴ��ļ�����");
						for (PrintStream ps_ : Server.clients_string.valueSet()) {
							ps_.println("Server" + "@" + "PIC_up_ok");
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
