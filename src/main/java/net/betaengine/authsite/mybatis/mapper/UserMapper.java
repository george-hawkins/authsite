package net.betaengine.authsite.mybatis.mapper;

import java.util.List;

import net.betaengine.authsite.mybatis.domain.User;

public interface UserMapper {

    public User getUserById(int userId);

    public User getUserByUsername(String username);

    public List<User> getAllUsers();

    public void createUser(User user);
    
    public void createUserRole(int userId);

    public void deleteUserRole(int userId);

    public void modifyUser(User user);

    public void deleteUser(int userId);
}
