package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.mapper.UserMapper;
import global.smartup.node.mapper.UserMarketDataMapper;
import global.smartup.node.po.*;
import global.smartup.node.util.Common;
import global.smartup.node.util.Pagination;
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

    @Autowired
    private UserMarketDataMapper userMarketDataMapper;

    @Autowired
    private UserAccountService userAccountService;

    public User add(String address) {
        address = Keys.toChecksumAddress(address);
        User user = new User();
        user.setUserAddress(address);
        user.setCreateTime(new Date());
        user.setCode(generateCode());
        userMapper.insert(user);
        userAccountService.addAccount(address);
        return user;
    }

    public UserMarketData addMarketData(UserMarketData marketData) {
        if (!StringUtils.isAnyBlank(marketData.getMarketId(), marketData.getUserAddress())) {
            if (marketData.getPostCount() == null) {
                marketData.setPostCount(0);
            }
            if (marketData.getReplyCount() == null) {
                marketData.setReplyCount(0);
            }
            if (marketData.getReceivedLikeCount() == null) {
                marketData.setReceivedLikeCount(0);
            }
            userMarketDataMapper.insert(marketData);
        }
        return marketData;
    }

    public void update(User user) {
        User db = userMapper.selectByPrimaryKey(user.getUserAddress());
        if (db != null) {
            if (db.getName() == null && user.getName() != null) {
                db.setName(user.getName() );
            }
            if (user.getAvatarIpfsHash() != null) {
                db.setAvatarIpfsHash(user.getAvatarIpfsHash());
            }
            userMapper.updateByPrimaryKey(db);
        }
    }

    public void updateCode(String address) {
        address = Keys.toChecksumAddress(address);
        User user = userMapper.selectByPrimaryKey(address);
        user.setCode(generateCode());
        userMapper.updateByPrimaryKey(user);
    }

    public void updatePostCount(String userAddress, String marketId) {
        UserMarketData marketData = queryMarketDataById(userAddress, marketId);
        if (marketData == null) {
            marketData = new UserMarketData();
            marketData.setMarketId(marketId);
            marketData.setUserAddress(userAddress);
            marketData.setPostCount(1);
            addMarketData(marketData);
        } else {
            marketData.setPostCount(marketData.getPostCount() + 1);
            userMarketDataMapper.updateByPrimaryKey(marketData);
        }
    }

    public void updateReplyCount(String userAddress, String marketId) {
        UserMarketData marketData = queryMarketDataById(userAddress, marketId);
        if (marketData == null) {
            marketData = new UserMarketData();
            marketData.setMarketId(marketId);
            marketData.setUserAddress(userAddress);
            marketData.setReplyCount(1);
            addMarketData(marketData);
        } else {
            marketData.setReplyCount(marketData.getReplyCount() + 1);
            userMarketDataMapper.updateByPrimaryKey(marketData);
        }
    }

    public void updateReceivedLikeCount(String userAddress, String marketId, Integer addend) {
        UserMarketData marketData = queryMarketDataById(userAddress, marketId);
        if (marketData == null) {
            if (addend > 0) {
                marketData = new UserMarketData();
                marketData.setMarketId(marketId);
                marketData.setUserAddress(userAddress);
                marketData.setReceivedLikeCount(addend);
                addMarketData(marketData);
            }
        } else {
            Integer likeCount = marketData.getReceivedLikeCount() + addend;
            likeCount = likeCount > 0 ? likeCount : 0;
            marketData.setReceivedLikeCount(likeCount);
            userMarketDataMapper.updateByPrimaryKey(marketData);
        }
    }

    public UserMarketData queryMarketDataById(String userAddress, String marketId) {
        UserMarketData dataId = new UserMarketData();
        dataId.setMarketId(marketId);
        dataId.setUserAddress(userAddress);
        return userMarketDataMapper.selectByPrimaryKey(dataId);
    }

    public User query(String address) {
        address = Keys.toChecksumAddress(address);
        return userMapper.selectByPrimaryKey(address);
    }

    public boolean isNameExist(String name) {
        User cdt = new User();
        cdt.setName(name);
        return userMapper.selectOne(cdt) != null;
    }

    public boolean isNotExist(String address) {
        return !isExist(address);
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

    public void fillUserForAccount(List<UserAccount> list) {
        List<String> addressList = list.stream().map(UserAccount::getUserAddress).collect(Collectors.toList());
        List<User> users = queryByAddressList(addressList);
        list.forEach(a -> a.setUser(users.stream().filter(u -> u.getUserAddress().equals(a.getUserAddress())).findFirst().orElse(null)));
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

    public List<User> queryUserList(List<String> addressList) {
        Example userExample = new Example(User.class);
        userExample.createCriteria().andIn("userAddress", addressList);
        userExample.excludeProperties("code");
        return userMapper.selectByExample(userExample);
    }

    public List<User> queryTopUserOnPostCount(String marketId, Integer limit) {
        List<User> ret = new ArrayList<>();
        if (StringUtils.isBlank(marketId) || limit <= 0) {
            return ret;
        }
        List<String> addresses = userMapper.selectTopUserOnPostAndReply(marketId, limit);
        if (addresses == null || addresses.size() <= 0) {
            return ret;
        }
        Example example = new Example(User.class);
        example.createCriteria().andIn("userAddress", addresses);
        List<User> userList = userMapper.selectByExample(example);
        addresses.forEach(a -> {
            ret.add(userList.stream().filter(u -> a.equals(u.getUserAddress())).findFirst().orElse(null));
        });
        return ret;
    }

    public List<User> queryTopUserOnReceviedLike(String marketId, Integer limit) {
        List<User> ret = new ArrayList<>();
        if (StringUtils.isBlank(marketId) || limit <= 0) {
            return ret;
        }
        List<String> addresses = userMapper.selectTopUserOnReceivedLike(marketId, limit);
        if (addresses == null || addresses.size() <= 0) {
            return ret;
        }
        Example example = new Example(User.class);
        example.createCriteria().andIn("userAddress", addresses);
        List<User> userList = userMapper.selectByExample(example);
        addresses.forEach(a -> {
            ret.add(userList.stream().filter(u -> a.equals(u.getUserAddress())).findFirst().orElse(null));
        });
        return ret;
    }

    public Pagination<User> queryPage(Integer pageNumb, Integer pageSize) {
        Page<User> page = PageHelper.startPage(pageNumb, pageSize);
        userMapper.selectAll();
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
