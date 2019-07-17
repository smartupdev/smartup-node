package global.smartup.node.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.LangHandle;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.constant.RedisKey;
import global.smartup.node.mapper.NotificationMapper;
import global.smartup.node.po.Notification;
import global.smartup.node.util.MapBuilder;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.Ntfc;
import global.smartup.node.vo.UnreadNtfc;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Keys;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
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

    @Autowired
    private MessageSource messageSource;

    public void sendChargeSutFinish(String txHash, boolean isSuccess, String userAddress, BigDecimal sut) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Map<String, Object> content = MapBuilder.<String, Object>create()
                .put("txHash", txHash)
                .put("isSuccess", isSuccess)
                .put("sut", sut)
                .put("userAddress", userAddress)
                .build();
        send(userAddress, PoConstant.Notification.Style.Personal, PoConstant.Notification.Type.ChargeSutFinish, content);
    }

    public void sendChargeEthFinish(String txHash, boolean isSuccess, String userAddress, BigDecimal eth) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Map<String, Object> content = MapBuilder.<String, Object>create()
                .put("txHash", txHash)
                .put("isSuccess", isSuccess)
                .put("eth", eth)
                .put("userAddress", userAddress)
                .build();
        send(userAddress, PoConstant.Notification.Style.Personal, PoConstant.Notification.Type.ChargeEthFinish, content);
    }

    public void sendWithdrawSutFinish(String txHash, boolean isSuccess, String userAddress, BigDecimal sut) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Map<String, Object> content = MapBuilder.<String, Object>create()
                .put("txHash", txHash)
                .put("isSuccess", isSuccess)
                .put("sut", sut)
                .put("userAddress", userAddress)
                .build();
        send(userAddress, PoConstant.Notification.Style.Personal, PoConstant.Notification.Type.WithdrawSutFinish, content);
    }

    public void sendWithdrawEthFinish(String txHash, boolean isSuccess, String userAddress, BigDecimal eth) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Map<String, Object> content = MapBuilder.<String, Object>create()
                .put("txHash", txHash)
                .put("isSuccess", isSuccess)
                .put("eth", eth)
                .put("userAddress", userAddress)
                .build();
        send(userAddress, PoConstant.Notification.Style.Personal, PoConstant.Notification.Type.WithdrawEthFinish, content);
    }

    public void sendMarketCreateFinish(String txHash, boolean isSuccess, String userAddress, String marketId, String marketName, BigDecimal initSut) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Map<String, Object> content = MapBuilder.<String, Object>create()
                .put("txHash", txHash)
                .put("isSuccess", isSuccess)
                .put("marketId", marketId)
                .put("marketName", marketName)
                .put("initSut", initSut)
                .build();
        send(userAddress, PoConstant.Notification.Style.Personal, PoConstant.Notification.Type.MarketCreateFinish, content);
    }

    public void send(String userAddress, String style, String type, Map content) {
        Notification n = new Notification();
        n.setUserAddress(userAddress);
        n.setNotificationId(idGenerator.getStringId());
        n.setStyle(style);
        n.setType(type);
        n.setContent(JSON.toJSONString(content, SerializerFeature.WriteBigDecimalAsPlain));
        n.setIsRead(false);
        n.setCreateTime(new Date());
        notificationMapper.insert(n);

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

    public void modRead(String notificationId) {
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

    public void modRead(List<String> ids) {
        for (String id : ids) {
            modRead(id);
        }
    }

    public UnreadNtfc queryUnreadInCache(String userAddress, Locale locale) {
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
                unreadNtfc.setList(transferVo(list, locale));
                return unreadNtfc;
            }

            Object listObj = redisTemplate.opsForValue().get(listKey);
            if (listObj != null) {
                list = (List<Notification>) listObj;
                unreadNtfc.setList(transferVo(list, locale));
                return unreadNtfc;
            }
        }

        // find in db
        count = notificationMapper.selectUnreadCount(userAddress);
        list = notificationMapper.selectUnreadFew(userAddress, 10);

        // save in cache
        redisTemplate.opsForValue().set(countKey, count, RedisKey.NotificationExpire, TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(listKey, list, RedisKey.NotificationExpire, TimeUnit.MILLISECONDS);

        unreadNtfc.setCount(count);
        unreadNtfc.setList(transferVo(list, locale));
        return unreadNtfc;
    }

    public Pagination<Ntfc> queryPage(String userAddress, Boolean unread, Integer pageNumb, Integer pageSize, Locale locale) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Example example = new Example(Notification.class);
        Example.Criteria criteria = example.createCriteria()
                .andEqualTo("userAddress", userAddress);
        if (unread != null) {
            criteria.andEqualTo("isRead", !unread);
        }
        example.orderBy("createTime").desc();
        Page<Notification> page = PageHelper.startPage(pageNumb, pageSize);
        notificationMapper.selectByExample(example);
        List<Ntfc> list = transferVo(page.getResult(), locale);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), list);
    }

    public Pagination<Ntfc> querySearch(String userAddress, String query, Integer pageNumb, Integer pageSize, Locale locale) {
        userAddress = Keys.toChecksumAddress(userAddress);
        Example example = new Example(Notification.class);
        Example.Criteria criteria = example.createCriteria()
                .andEqualTo("userAddress", userAddress);

        if (StringUtils.isNotBlank(query)) {
            if (query.length() > 100) {
                query = query.substring(0, 100);
            }
            if (Locale.ENGLISH.equals(locale)) {
                criteria.andCondition(" (title_en like '%" + query + "%' or text_en like '%" + query + "%') ");
            } else if (Locale.SIMPLIFIED_CHINESE.equals(locale)) {
                criteria.andCondition(" (title_zh_cn like '%" + query + "%' or text_zh_cn like '%" + query + "%') ");
            } else if (Locale.TRADITIONAL_CHINESE.equals(locale)) {
                criteria.andCondition(" (title_zh_tw like '%" + query + "%' or text_zh_tw like '%" + query + "%') ");
            }
        }

        example.orderBy("createTime").desc();
        Page<Notification> page = PageHelper.startPage(pageNumb, pageSize);
        notificationMapper.selectByExample(example);
        List<Ntfc> list = transferVo(page.getResult(), locale);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), list);
    }

    private List<Ntfc> transferVo(List<Notification> list, Locale locale) {
        List<Ntfc> ret = new ArrayList<>();
        for (Notification notification : list) {
            fillI18n(notification, locale);
            Ntfc ntfc = new Ntfc();
            BeanUtils.copyProperties(notification, ntfc, "content");
            HashMap<String, Object> map = JSON.parseObject(notification.getContent(), HashMap.class, Feature.UseBigDecimal);
            ntfc.setContent(map);
            ret.add(ntfc);
        }
        return ret;
    }

    private void fillI18n(Notification not, Locale locale) {
        HashMap map = JSON.parseObject(not.getContent(), HashMap.class, Feature.UseBigDecimal);
        String title = null, text = null;

        if (PoConstant.Notification.Type.ChargeSutFinish.equals(not.getType())) {
            Boolean isS = (Boolean) map.get("isSuccess");
            String sut = ((BigDecimal) map.get("sut")).setScale(2, BigDecimal.ROUND_DOWN).toPlainString();
            if (isS) {
                title = messageSource.getMessage(LangHandle.NotificationTitleChargeSutSuccess, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextChargeSutSuccess, new String[]{sut}, locale);
            } else {
                title = messageSource.getMessage(LangHandle.NotificationTitleChargeSutFail, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextChargeSutFail, new String[]{sut}, locale);
            }
        }

        if (PoConstant.Notification.Type.ChargeEthFinish.equals(not.getType())) {
            Boolean isS = (Boolean) map.get("isSuccess");
            String eth = ((BigDecimal) map.get("eth")).setScale(6, BigDecimal.ROUND_DOWN).toPlainString();
            if (isS) {
                title = messageSource.getMessage(LangHandle.NotificationTitleChargeEthSuccess, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextChargeEthSuccess, new String[]{eth}, locale);
            } else {
                title = messageSource.getMessage(LangHandle.NotificationTitleChargeEthFail, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextChargeEthFail, new String[]{eth}, locale);
            }
        }

        if (PoConstant.Notification.Type.WithdrawSutFinish.equals(not.getType())) {
            Boolean isS = (Boolean) map.get("isSuccess");
            String sut = ((BigDecimal) map.get("sut")).setScale(2, BigDecimal.ROUND_DOWN).toPlainString();
            if (isS) {
                title = messageSource.getMessage(LangHandle.NotificationTitleWithdrawSutSuccess, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextWithdrawSutSuccess, new String[]{sut}, locale);
            } else {
                title = messageSource.getMessage(LangHandle.NotificationTitleWithdrawSutFail, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextWithdrawSutFail, new String[]{sut}, locale);
            }
        }

        if (PoConstant.Notification.Type.WithdrawEthFinish.equals(not.getType())) {
            Boolean isS = (Boolean) map.get("isSuccess");
            String eth = ((BigDecimal) map.get("eth")).setScale(6, BigDecimal.ROUND_DOWN).toPlainString();
            if (isS) {
                title = messageSource.getMessage(LangHandle.NotificationTitleWithdrawEthSuccess, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextWithdrawEthSuccess, new String[]{eth}, locale);
            } else {
                title = messageSource.getMessage(LangHandle.NotificationTitleWithdrawEthFail, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextWithdrawEthFail, new String[]{eth}, locale);
            }
        }

        if (PoConstant.Notification.Type.MarketCreateFinish.equals(not.getType())) {
            Boolean isS = (Boolean) map.get("isSuccess");
            String marketName = (String) map.get("marketName");
            String sut = ((BigDecimal) map.get("sut")).setScale(2, BigDecimal.ROUND_DOWN).toPlainString();
            if (isS) {
                title = messageSource.getMessage(LangHandle.NotificationTitleMarketCreateSuccess, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextMarketCreateSuccess, new String[]{sut, marketName}, locale);
            } else {
                title = messageSource.getMessage(LangHandle.NotificationTitleMarketCreateFail, null, locale);
                text = messageSource.getMessage(LangHandle.NotificationTextMarketCreateFail, new String[]{sut, marketName}, locale);
            }
        }

        not.setTitle(title);
        not.setText(text);
    }

}

