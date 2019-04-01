package global.smartup.node.service;

import global.smartup.node.mapper.UserMapper;
import global.smartup.node.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void add(User user) {
        userMapper.insert(user);
    }


    public List<User> queryPage(Integer pageNumb, Integer pageSize) {
        return userMapper.selectAll();
    }

    public User query(String userAddress) {
        return userMapper.selectByPrimaryKey(userAddress);
    }
}
