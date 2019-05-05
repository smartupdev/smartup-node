package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.eth.SmartupClient;
import global.smartup.node.mapper.CTAccountMapper;
import global.smartup.node.po.CTAccount;
import global.smartup.node.util.Pagination;
import global.smartup.node.vo.CTAccountWithMarket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CTAccountService {

    private static final Logger log = LoggerFactory.getLogger(CTAccountService.class);

    @Autowired
    private CTAccountMapper ctAccountMapper;

    @Autowired
    private SmartupClient smartupClient;

    @Autowired
    private MarketService marketService;


    public void create(String marketAddress, String userAddress, BigDecimal init) {
        CTAccount account = new CTAccount();
        account.setMarketAddress(marketAddress);
        account.setUserAddress(userAddress);
        account.setLastUpdateTime(new Date());
        if (init.compareTo(BigDecimal.ZERO) > 0) {
            account.setAmount(init);
        } else {
            account.setAmount(BigDecimal.ZERO);
        }
        ctAccountMapper.insert(account);
    }

    public void update(String marketAddress, String userAddress, BigDecimal add) {
        CTAccount account = queryAccount(marketAddress, userAddress);
        if (account == null) {
            create(marketAddress, userAddress, add);
        } else {
            account.setAmount(account.getAmount().add(add));
            account.setLastUpdateTime(new Date());
            ctAccountMapper.updateByPrimaryKey(account);
        }
    }

    public void updateFromChain(String marketAddress, String userAddress) {
        BigDecimal balance = smartupClient.getCtBalance(marketAddress, userAddress);
        if (balance == null) {
            return;
        }
        CTAccount account = queryAccount(marketAddress, userAddress);
        if (account == null) {
            create(marketAddress, userAddress, balance);
            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                marketService.updateUserCount(marketAddress, 1);
            }
        } else {
            account.setAmount(balance);
            account.setLastUpdateTime(new Date());
            ctAccountMapper.updateByPrimaryKey(account);
            if (account.getAmount().compareTo(BigDecimal.ZERO) <= 0 && balance.compareTo(BigDecimal.ZERO) > 0) {
                marketService.updateUserCount(marketAddress, 1);
            }
            if (account.getAmount().compareTo(BigDecimal.ZERO) > 0 && balance.compareTo(BigDecimal.ZERO) <= 0) {
                marketService.updateUserCount(marketAddress, -1);
            }
        }
    }

    public CTAccount queryAccount(String marketAddress, String userAddress) {
        CTAccount key = new CTAccount();
        key.setMarketAddress(marketAddress);
        key.setUserAddress(userAddress);
        return ctAccountMapper.selectOne(key);
    }

    public Pagination<CTAccountWithMarket> queryCTAccountsWithMarket(String userAddress, Integer pageNumb, Integer pageSize) {
        Page<CTAccountWithMarket> page =  PageHelper.startPage(pageNumb, pageSize);
        ctAccountMapper.selectWidthMarket(userAddress);
        fillMarketName(page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<String> queryUserTradeMarket(String userAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(CTAccount.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress)
                .andGreaterThan("amount", BigDecimal.ZERO);
        example.orderBy("lastUpdateTime").desc();
        Page<CTAccount> page = PageHelper.startPage(pageNumb, pageSize);
        ctAccountMapper.selectByExample(example);
        List<String> ret = page.getResult().stream().map(CTAccount::getMarketAddress).collect(Collectors.toList());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), ret);
    }

    public List<String> queryTopUserAddress(String marketAddress, Integer limit) {
        Example ctExample = new Example(CTAccount.class);
        ctExample.createCriteria().andEqualTo("marketAddress", marketAddress);
        ctExample.orderBy("amount").desc();
        PageHelper.startPage(1, limit, false);
        List<CTAccount> ctAccountList = ctAccountMapper.selectByExample(ctExample);
        return ctAccountList.stream().map(CTAccount::getUserAddress).collect(Collectors.toList());
    }

    public Integer queryUserCountInMarket(String marketAddress) {
        return ctAccountMapper.selectCountUserInMarket(marketAddress);
    }

    private void fillMarketName(List<CTAccountWithMarket> list) {
        if (list == null && list.size() <= 0) {
            return;
        }
        List<String> ids = list.stream().map(a -> a.getMarketId()).collect(Collectors.toList());
        List<String> marketNameList = marketService.queryNames(ids);
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setMarketName(marketNameList.get(i));
        }
    }

}
