package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.CollectMapper;
import global.smartup.node.po.Collect;
import global.smartup.node.util.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollectService {

    private static final Logger log = LoggerFactory.getLogger(CollectService.class);

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private MarketService marketService;

    @Autowired
    private PostService postService;

    public void add(String userAddress, String type, String objectMark) {
        if (isCollected(userAddress, type, objectMark)) {
            return;
        }
        Collect collect = new Collect();
        collect.setUserAddress(userAddress);
        collect.setType(type);
        collect.setObjectMark(objectMark);
        collect.setCreateTime(new Date());
        collectMapper.insert(collect);
    }

    public void del(String userAddress, String type, String objectMark) {
        Collect collect = new Collect();
        collect.setUserAddress(userAddress);
        collect.setType(type);
        collect.setObjectMark(objectMark);
        collectMapper.deleteByPrimaryKey(collect);
    }

    public boolean isType(String type) {
        List<String> types = Arrays.asList(PoConstant.Collect.Type.All);
        if (!types.contains(type)) {
            return false;
        }
        return true;
    }

    public boolean isObjectMarkExist(String type, String objectMark) {
        if (PoConstant.Collect.Type.Market.equals(type)) {
            if (marketService.isMarketIdExist(objectMark)) {
                return true;
            }
        } else if (PoConstant.Collect.Type.Market.equals(type)) {
            Long id = Long.valueOf(objectMark);
            if (postService.isExist(id)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCollected(String userAddress, String type, String objectMark) {
        return query(userAddress, type, objectMark) != null;
    }

    public List<String> isCollected(String userAddress, String type, List<Object> objectMark) {
        Example example = new Example(Collect.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress)
                .andEqualTo("type", type)
                .andIn("objectMark", objectMark);
        List<Collect> collects = collectMapper.selectByExample(example);
        return collects.stream().map(Collect::getObjectMark).collect(Collectors.toList());
    }

    public Collect query(String userAddress, String type, String objectMark) {
        Collect collect = new Collect();
        collect.setUserAddress(userAddress);
        collect.setType(type);
        collect.setObjectMark(objectMark);
        return collectMapper.selectByPrimaryKey(collect);
    }

    public Pagination queryPage(String userAddress, String type, Integer pageNumb, Integer pageSize) {
        Page page = null;
        if (PoConstant.Collect.Type.Market.equals(type)) {
            page = PageHelper.startPage(pageNumb, pageSize);
            collectMapper.selectCollectedMarket(userAddress);
        } else if (PoConstant.Collect.Type.Post.equals(type)) {
            page = PageHelper.startPage(pageNumb, pageSize);
            collectMapper.selectCollectedPost(userAddress);
        }
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());

    }

}
