package net.betaengine.authsite.mybatis.mapper;

import java.util.List;

import net.betaengine.authsite.mybatis.domain.User;

public interface UserMapper {

    public User getUserById(Integer userId);

    public List<User> getAllUsers();

    public void createUser(User user);
    
    public void createUserRole(Integer userId);

    public void deleteUserRole(Integer userId);

    public void modifyUser(User user);

    public void deleteUser(Integer userId);
}
