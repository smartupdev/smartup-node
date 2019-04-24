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

import java.math.BigDecimal;
import java.util.Date;

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
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

}
