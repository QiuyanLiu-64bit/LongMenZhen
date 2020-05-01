package DB;

public class TestUserDB {
    public static void main(String[] args) {
        // 创建一个UserDB对象，用于测试
        UserDB userDB = new UserDB("测试用户名", "测试密码");

        // 尝试添加用户
        boolean addResult = userDB.addsql();
        if (addResult) {
            System.out.println("用户添加成功！");
        } else {
            System.out.println("用户添加失败！");
        }

        // 尝试验证用户
        boolean selectResult = userDB.selectsql();
        if (selectResult) {
            System.out.println("用户验证成功！");
        } else {
            System.out.println("用户验证失败！");
        }
    }
}

