package DB;

public class TestUserDB {
    public static void main(String[] args) {
        // ����һ��UserDB�������ڲ���
        UserDB userDB = new UserDB("�����û���", "��������");

        // ��������û�
        boolean addResult = userDB.addsql();
        if (addResult) {
            System.out.println("�û���ӳɹ���");
        } else {
            System.out.println("�û����ʧ�ܣ�");
        }

        // ������֤�û�
        boolean selectResult = userDB.selectsql();
        if (selectResult) {
            System.out.println("�û���֤�ɹ���");
        } else {
            System.out.println("�û���֤ʧ�ܣ�");
        }
    }
}

