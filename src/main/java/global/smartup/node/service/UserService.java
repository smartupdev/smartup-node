package global.smartup.node.service;

import global.smartup.node.mapper.UserMapper;
import global.smartup.node.po.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public void add(User user) {
        user.setCreateTime(new Date());
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateByPrimaryKey(user);
    }

    public List<User> queryPage(Integer pageNumb, Integer pageSize) {
        return userMapper.selectAll();
    }

    public User query(String address) {
        return userMapper.selectByPrimaryKey(address);
    }

    public boolean isExist(String address) {
        return userMapper.selectByPrimaryKey(address) != null;
    }

}
