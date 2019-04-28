package global.smartup.node.service;

import global.smartup.node.mapper.UserMapper;
import global.smartup.node.po.Post;
import global.smartup.node.po.Reply;
import global.smartup.node.po.User;
import global.smartup.node.util.Common;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User add(User user) {
        String address = Keys.toChecksumAddress(user.getUserAddress());
        user.setUserAddress(address);
        if (StringUtils.isBlank(user.getName())) {
            user.setName(address);
        }
        user.setCreateTime(new Date());
        user.setCode(generateCode());
        userMapper.insert(user);
        return user;
    }

    public User add(String address) {
        address = Keys.toChecksumAddress(address);
        User user = new User();
        if (StringUtils.isBlank(user.getName())) {
            user.setName(address);
        }
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
        address = Keys.toChecksumAddress(address);
        User user = userMapper.selectByPrimaryKey(address);
        user.setCode(generateCode());
        userMapper.updateByPrimaryKey(user);
    }

    public List<User> queryPage(Integer pageNumb, Integer pageSize) {
        return userMapper.selectAll();
    }

    public User query(String address) {
        address = Keys.toChecksumAddress(address);
        return userMapper.selectByPrimaryKey(address);
    }

    public boolean isExist(String address) {
        address = Keys.toChecksumAddress(address);
        return userMapper.selectByPrimaryKey(address) != null;
    }

    public String generateCode() {
        return Common.getRandomString(16);
    }

    public void fillUserForPost(List<Post> posts) {
        if (posts == null || posts.size() <= 0) {
            return;
        }
        List<String> addressList = posts.stream().map(Post::getUserAddress).collect(Collectors.toList());
        List<User> users = queryByAddressList(addressList);
        posts.forEach(p -> p.setUser(users.stream().filter(u -> u.getUserAddress().equals(p.getUserAddress())).findFirst().orElse(null)));
    }

    public void fillUserForReply(List<Reply> replies) {
        List<String> addressList = replies.stream().map(Reply::getUserAddress).collect(Collectors.toList());
        List<User> users = queryByAddressList(addressList);
        replies.forEach(p -> p.setUser(users.stream().filter(u -> u.getUserAddress().equals(p.getUserAddress())).findFirst().orElse(null)));
    }

    public List<User> queryByAddressList(List<String> addressList) {
        if (addressList == null || addressList.size() <= 0) {
            return new ArrayList<>();
        }
        Example example = new Example(User.class);
        example.createCriteria().andIn("userAddress", addressList);
        example.excludeProperties("code");
        return userMapper.selectByExample(example);
    }

}
