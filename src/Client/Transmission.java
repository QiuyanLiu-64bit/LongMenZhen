package Client;

public class Transmission {
	 //文件名称
   public String fileName;

   //文件长度
   public long fileLength;

   //传输类型
   public int transmissionType;//0 文本  1  图片  2  文件  3  语音

   //传输内容
   public String content;

   //传输的长度
   public long transLength;

   //0 文本  1  图片
   public int showType;

   public Transmission() {
   }
}
