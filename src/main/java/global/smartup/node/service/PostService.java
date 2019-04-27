package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.PostDataMapper;
import global.smartup.node.mapper.PostMapper;
import global.smartup.node.po.Market;
import global.smartup.node.po.Post;
import global.smartup.node.po.PostData;
import global.smartup.node.util.Common;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PostDataMapper postDataMapper;

    @Autowired
    private MarketService marketService;

    @Autowired
    private LikeService likeService;


    public void create(Post post) {
        Long id = idGenerator.getId();
        post.setPostId(id);
        if (PoConstant.Post.Type.Market.equals(post.getType())) {
            Market market = marketService.queryById(post.getMarketId());
            post.setMarketAddress(market.getMarketAddress());
        }
        post.setCreateTime(new Date());
        postMapper.insert(post);

        PostData postData = new PostData();
        postData.setPostId(id);
        postData.setReplyCount(0);
        postData.setShareCount(0);
        postData.setCollectCount(0);
        postData.setLikeCount(0);
        postData.setDislikeCount(0);
        postDataMapper.insert(postData);

        // update market data
        if (StringUtils.isNotBlank(post.getMarketId())) {
            marketService.updatePostCountAddOne(post.getMarketAddress());
        }
    }

    public boolean isExist(Long postId) {
        return postMapper.selectByPrimaryKey(postId) != null;
    }

    public Post query(Long postId) {
        return postMapper.selectByPrimaryKey(postId);
    }

    public Pagination<Post> queryPage(String userAddress, String type, String marketId, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Post.class);
        if (StringUtils.isBlank(type) || PoConstant.Post.Type.Root.equalsIgnoreCase(type)) {
            // root
            example.createCriteria().andEqualTo("type", PoConstant.Post.Type.Root);
        } else {
            // market
            example.createCriteria()
                    .andEqualTo("type", PoConstant.Post.Type.Market)
                    .andEqualTo("marketId", marketId);
        }
        example.orderBy("createTime").asc();
        Page page = PageHelper.startPage(pageNumb, pageSize);
        postMapper.selectByExample(example);
        likeService.queryFillLike(userAddress, marketId, page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Integer queryLatelyReplyCount() {
        Example example = new Example(PostData.class);
        Date lately = Common.getSomeDaysAgo(new Date(), 30);
        example.createCriteria().andGreaterThanOrEqualTo("lastReplyTime", lately);
        return postDataMapper.selectCountByExample(example);
    }


}
