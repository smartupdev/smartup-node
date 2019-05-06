package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.CollectMapper;
import global.smartup.node.po.Collect;
import global.smartup.node.po.Post;
import global.smartup.node.po.Reply;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
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

    @Autowired
    private ReplyService replyService;

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

    public boolean isObjectExist(String type, String objectMark) {
        if (PoConstant.Collect.Type.Market.equals(type)) {
            if (marketService.isMarketIdExist(objectMark)) {
                return true;
            }
        } else if (PoConstant.Collect.Type.Post.equals(type)) {
            Long id = Long.valueOf(objectMark);
            if (postService.isExist(id)) {
                return true;
            }
        } else if (PoConstant.Collect.Type.Reply.equals(type)) {
            Long id = Long.valueOf(objectMark);
            if (replyService.isExist(id)) {
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

    public Pagination queryPageWithObj(String userAddress, String type, Boolean asc, Integer pageNumb, Integer pageSize) {
        Page page = null;
        if (asc == null) {
            asc = false;
        }
        if (PoConstant.Collect.Type.Market.equals(type)) {
            page = PageHelper.startPage(pageNumb, pageSize);
            collectMapper.selectCollectedMarket(userAddress, asc);
        } else if (PoConstant.Collect.Type.Post.equals(type)) {
            page = PageHelper.startPage(pageNumb, pageSize);
            collectMapper.selectCollectedPost(userAddress, asc);
        }
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Collect> queryPage(String userAddress, String type, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Collect.class);
        example.createCriteria().andEqualTo("type", type)
                .andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Collect> page = PageHelper.startPage(pageNumb, pageSize);
        collectMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public void fillCollectForPosts(String userAddress, List<Post> posts) {
        if (posts == null || posts.size() <= 0) {
            return;
        }
        List<String> postIds = posts.stream().map(p -> String.valueOf(p.getPostId())).collect(Collectors.toList());
        Example example = new Example(Collect.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress)
                .andEqualTo("type", PoConstant.Collect.Type.Post)
                .andIn("objectMark", postIds);
        List<String> cIds = collectMapper.selectByExample(example)
                .stream().map(Collect::getObjectMark).collect(Collectors.toList());
        posts.forEach(p -> {
            if (cIds.contains(String.valueOf(p.getPostId()))) {
                p.setIsCollected(true);
            } else {
                p.setIsCollected(false);
            }
        });
    }

    public void fillCollectForReplies(String userAddress, List<Reply> replies) {
        if (StringUtils.isBlank(userAddress) || replies == null || replies.size() <= 0) {
            return;
        }
        List<String> replayIds = replies.stream().map(p -> String.valueOf(p.getReplyId())).collect(Collectors.toList());
        Example example = new Example(Collect.class);
        example.createCriteria()
                .andEqualTo("userAddress", userAddress)
                .andEqualTo("type", PoConstant.Collect.Type.Reply)
                .andIn("objectMark", replayIds);
        List<String> cIds = collectMapper.selectByExample(example)
                .stream().map(Collect::getObjectMark).collect(Collectors.toList());
        replies.forEach(r -> {
            if (cIds.contains(String.valueOf(r.getReplyId()))) {
                r.setIsCollected(true);
            } else {
                r.setIsCollected(false);
            }
        });
    }

}
