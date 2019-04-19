package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.constant.RedisKey;
import global.smartup.node.mapper.NotificationMapper;
import global.smartup.node.po.Notification;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.UnreadNtfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class NotificationService {

    /**
     * 通知缓存策略
     * 1. 首先查询redis中的通知，如果查询不到，再查询MySQL中的通知
     * 2. 查询到新的通知后在redis中cache，如果查询到空，也在redis中放置一个空的标识
     * 3. 如果产生了新的通知，就删除redis中的cache
     */

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdGenerator idGenerator;


    public void sendMarketCreateFinish(String txHash, boolean isSuccess, String userAddress, String marketAddress) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Notification ntfc = new Notification();
        HashMap<String, Object> content = new HashMap<>();
        content.put("userAddress", userAddress);
        content.put("isSuccess", isSuccess);
        content.put("marketAddress", marketAddress);
        content.put("txHash", txHash);
        ntfc.setUserAddress(userAddress);
        ntfc.setNotificationId(idGenerator.getId());
        ntfc.setType(PoConstant.Notification.Type.MarketCreateFinish);
        ntfc.setContent(JSON.toJSONString(content, SerializerFeature.WriteBigDecimalAsPlain));
        ntfc.setIsRead(false);
        ntfc.setCreateTime(new Date());
        notificationMapper.insert(ntfc);

        //clear cache
        delNotificationCache(userAddress);
    }

    public void sendTradeFinish(String txHash, boolean isSuccess, String userAddress, String type, String marketAddress, BigDecimal sut, BigDecimal ct) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Notification ntfc = new Notification();
        HashMap<String, Object> content = new HashMap<>();
        content.put("txHash", txHash);
        content.put("isSuccess", isSuccess);
        content.put("userAddress", userAddress);
        content.put("type", type);
        content.put("marketAddress", marketAddress);
        content.put("sut", sut);
        content.put("ct", ct);
        ntfc.setUserAddress(userAddress);
        ntfc.setNotificationId(idGenerator.getId());
        ntfc.setType(PoConstant.Notification.Type.TradeFinish);
        ntfc.setContent(JSON.toJSONString(content, SerializerFeature.WriteBigDecimalAsPlain));
        ntfc.setIsRead(false);
        ntfc.setCreateTime(new Date());
        notificationMapper.insert(ntfc);

        //clear cache
        delNotificationCache(userAddress);
    }

    public void delNotificationCache(String userAddress) {
        userAddress = Keys.toChecksumAddress(userAddress);
        String countKey = RedisKey.NotificationPrefix + userAddress + RedisKey.NotificationCountPrefix;
        String listKey = RedisKey.NotificationPrefix + userAddress + RedisKey.NotificationListPrefix;
        redisTemplate.delete(countKey);
        redisTemplate.delete(listKey);
    }

    public void modRead(Long notificationId) {
        if (notificationId == null) {
            return;
        }
        Notification ntfc = notificationMapper.selectByPrimaryKey(notificationId);
        if (ntfc == null) {
            return;
        }
        ntfc.setIsRead(true);
        notificationMapper.updateByPrimaryKey(ntfc);

        // clear cache
        delNotificationCache(ntfc.getUserAddress());
    }

    public UnreadNtfc queryUnreadInCache(String userAddress) {
        userAddress = Keys.toChecksumAddress(userAddress);
        UnreadNtfc unreadNtfc = new UnreadNtfc();
        Integer count = 0;
        List<Notification> list = new ArrayList<>();

        // find in cache
        String countKey = RedisKey.NotificationPrefix + userAddress + RedisKey.NotificationCountPrefix;
        String listKey = RedisKey.NotificationPrefix + userAddress + RedisKey.NotificationListPrefix;
        Object countObj = redisTemplate.opsForValue().get(countKey);
        if (countObj != null) {
            count = Integer.valueOf(countObj.toString());
            unreadNtfc.setCount(count);
            if (count.compareTo(0) == 0) {
                unreadNtfc.setList(list);
                return unreadNtfc;
            }

            Object listObj = redisTemplate.opsForValue().get(listKey);
            if (listObj != null) {
                list = JSON.parseObject(listObj.toString(), List.class);
                unreadNtfc.setList(list);
                return unreadNtfc;
            }
        }

        // find in db
        count = notificationMapper.selectUnreadCount(userAddress);
        list = notificationMapper.selectUnreadFew(userAddress, 10);
        unreadNtfc.setCount(count);
        unreadNtfc.setList(list);

        // save in cache
        redisTemplate.opsForValue().set(countKey, count, RedisKey.NotificationExpire, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(listKey, list, RedisKey.NotificationExpire, TimeUnit.MILLISECONDS);

        return unreadNtfc;
    }

    public Pagination<Notification> queryPage(String userAddress, Integer pageNumb, Integer pageSize) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Example example = new Example(Notification.class);
        example.createCriteria().andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Notification> page = PageHelper.startPage(pageNumb, pageSize);
        notificationMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }



}
