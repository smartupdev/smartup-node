package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.eth.ExchangeClient;
import global.smartup.node.mapper.UserAccountMapper;
import global.smartup.node.po.Market;
import global.smartup.node.po.UserAccount;
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
    private ExchangeClient exchangeClient;


    public void addAccount(String userAddress) {
        UserAccount account = new UserAccount();
        account.setUserAddress(userAddress);
        account.setSut(BigDecimal.ZERO);
        account.setEth(BigDecimal.ZERO);
        account.setSutAmount(BigDecimal.ZERO);
        account.setUpdateTime(new Date());
        userAccountMapper.insert(account);
    }

    public void updateSut(String userAddress, BigDecimal newBalance) {
        UserAccount account = queryByAddress(userAddress);
        account.setSut(newBalance);
        account.setUpdateTime(new Date());
        userAccountMapper.updateByPrimaryKey(account);
    }

    public void updateEth(String userAddress, BigDecimal newBalance) {
        UserAccount account = queryByAddress(userAddress);
        account.setEth(newBalance);
        account.setUpdateTime(new Date());
        userAccountMapper.updateByPrimaryKey(account);
    }

    public void updateSutAndEth(String userAddress, BigDecimal sut, BigDecimal eth) {
        UserAccount account = queryByAddress(userAddress);
        account.setSut(sut);
        account.setEth(eth);
        account.setUpdateTime(new Date());
        userAccountMapper.updateByPrimaryKey(account);
    }

    public Boolean hasEnoughSut(String userAddress, BigDecimal sut) {
        // query form contract
        BigDecimal balance = exchangeClient.querySutBalance(userAddress);
        if (balance == null) {
            return null;
        }

        // update account
        updateSut(userAddress, balance);

        if (balance.compareTo(sut) >= 0) {
            return true;
        }
        return false;
    }

    public Boolean hasEnoughEth(String userAddress, BigDecimal eth) {
        // query form contract
        BigDecimal balance = exchangeClient.queryEthBalance(userAddress);
        if (balance == null) {
            return null;
        }

        // update account
        updateSut(userAddress, balance);

        if (balance.compareTo(eth) >= 0) {
            return true;
        }
        return false;
    }

    public UserAccount queryByAddress(String userAddress) {
        return userAccountMapper.selectByPrimaryKey(userAddress);
    }

    public List<UserAccount> queryTop(Integer top) {
        Example example = new Example(UserAccount.class);
        example.orderBy("sutAmount").desc();
        Page<UserAccount> page = PageHelper.startPage(1, top, false);
        userAccountMapper.selectByExample(example);
        userService.fillUserForAccount(page.getResult());
        return page.getResult();
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
