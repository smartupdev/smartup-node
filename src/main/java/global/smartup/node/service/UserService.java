package global.smartup.node.service;

import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.mapper.UserMapper;
import global.smartup.node.po.User;
import global.smartup.node.util.Common;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IdGenerator idGenerator;

    public User add(User user) {
        user.setCreateTime(new Date());
        user.setCode(generateCode());
        userMapper.insert(user);
        return user;
    }

    public User add(String address) {
        User user = new User();
        user.setUserAddress(address);
        user.setCreateTime(new Date());
        user.setCode(generateCode());
        userMapper.insert(user);
        return user;
    }

    public void update(User user) {
        User db = userMapper.selectByPrimaryKey(user.getUserAddress());
        if (db != null) {
            db.setName(user.getName());
            db.setAvatarIpfsHash(user.getAvatarIpfsHash());
            userMapper.updateByPrimaryKey(db);
        }
    }

    public void updateCode(String address) {
        User user = userMapper.selectByPrimaryKey(address);
        user.setCode(generateCode());
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

    public String generateCode() {
        return Common.getRandomString(16);
    }

}
