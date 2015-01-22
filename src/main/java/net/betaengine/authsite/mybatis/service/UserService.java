package net.betaengine.authsite.mybatis.service;

import java.util.List;

import org.mybatis.guice.transactional.Transactional;

import net.betaengine.authsite.mybatis.domain.User;
import net.betaengine.authsite.mybatis.mapper.UserMapper;

import com.google.inject.Inject;

public class UserService {
    private final UserMapper userMapper;
    
    @Inject
    UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    @Transactional
    public User getUserById(int userId) {
        return userMapper.getUserById(userId);
    }
    
    @Transactional
    public User getUserByUsername(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Transactional
    public List<User> getAllUsers() {
        return userMapper.getAllUsers();
    }

    @Transactional
    public void createUser(User user) {
        userMapper.createUser(user);
        userMapper.createUserRole(user.getId());
    }

    @Transactional
    public void modifyUser(User user) {
        userMapper.modifyUser(user);
    }

    @Transactional
    public void deleteUser(int userId) {
        userMapper.deleteUser(userId);
        userMapper.deleteUserRole(userId);
    }
}
