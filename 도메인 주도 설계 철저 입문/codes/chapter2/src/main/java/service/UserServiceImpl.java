package service;

import vo.UserName;

public class UserServiceImpl implements UserService {
    @Override
    public void CreateUser(UserName userName) {
        System.out.println(userName + "생성");
    }

    @Override
    public void UpdateUser(UserName userName) {
        System.out.println(userName + "수정");
    }
}
