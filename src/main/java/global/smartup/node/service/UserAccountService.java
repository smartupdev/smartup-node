package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageException;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.SmartupClient;
import global.smartup.node.mapper.UserAccountMapper;
import global.smartup.node.po.CTAccount;
import global.smartup.node.po.Market;
import global.smartup.node.po.User;
import global.smartup.node.po.UserAccount;
import global.smartup.node.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAccountService {

    private static final Logger log = LoggerFactory.getLogger(UserAccountService.class);

    private static final Map<String, BigDecimal> lastPriceCache = new HashMap<>();

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private CTAccountService ctAccountService;

    @Autowired
    private KlineNodeService klineNodeService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private SmartupClient smartupClient;

    public void updateAllAccount() {
        loadLastPrice();
        Integer pageNumb = 0;
        Integer pageSize = 200;
        Pagination<User> page;
        do {
            pageNumb += 1;
            page = userService.queryPage(pageNumb, pageSize);

            for (User user : page.getList()) {
                updateAccount(user.getUserAddress());
            }
        } while (page.hasNextPage());
        lastPriceCache.clear();
    }

    public List<UserAccount> queryTop(Integer top) {
        Example example = new Example(UserAccount.class);
        example.orderBy("sutAmount").desc();
        Page<UserAccount> page = PageHelper.startPage(1, top, false);
        userAccountMapper.selectByExample(example);
        userService.fillUserForAccount(page.getResult());
        return page.getResult();
    }

    private void updateAccount(String userAddress) {
        BigDecimal sut = smartupClient.getSutBalance(userAddress);
        if (sut == null) {
            return;
        }
        BigDecimal sutAmount = BigDecimal.ZERO.add(sut);
        List<CTAccount> ctAccountList = ctAccountService.queryUserAll(userAddress);
        for (CTAccount ctAccount : ctAccountList) {
            BigDecimal last = lastPriceCache.get(ctAccount.getMarketAddress());
            if (last != null) {
                sutAmount = sutAmount.add(ctAccount.getAmount().multiply(last));
            }
        }
        UserAccount account = userAccountMapper.selectByPrimaryKey(userAddress);
        if (account == null) {
            account = new UserAccount();
            account.setUserAddress(userAddress);
            account.setSut(sut);
            account.setSutAmount(sutAmount);
            account.setUpdateTime(new Date());
            userAccountMapper.insert(account);
        } else {
            account.setSut(sut);
            account.setSutAmount(sutAmount);
            account.setUpdateTime(new Date());
            userAccountMapper.updateByPrimaryKey(account);
        }
    }

    private void loadLastPrice() {
        List<Market> marketList = marketService.queryAll();
        for (Market market : marketList) {
            BigDecimal last = klineNodeService.queryCurrentPrice(market.getMarketAddress(), PoConstant.KLineNode.Segment.Day, new Date());
            if (last != null) {
                lastPriceCache.put(market.getMarketAddress(), last);
            }
        }
    }



}
