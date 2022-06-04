package service;

import vo.UserName;

public interface UserService {
    void CreateUser(UserName userName);
    void UpdateUser(UserName userName);
}
