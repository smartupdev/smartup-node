package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.PostDataMapper;
import global.smartup.node.mapper.PostMapper;
import global.smartup.node.po.*;
import global.smartup.node.util.Common;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    private ReplyService replyService;

    @Autowired
    private MarketService marketService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectService collectService;

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
        postData.setLikeCount(0);
        postData.setDislikeCount(0);
        postData.setLastReplyId(null);
        postData.setLastReplyTime(new Date());
        postDataMapper.insert(postData);

        if (StringUtils.isNotBlank(post.getMarketId())) {
            // update market data
            marketService.updatePostCountAddOne(post.getMarketAddress());

            // update user market data
            userService.updatePostCount(post.getUserAddress(), post.getMarketId());
        }
    }

    public void modLike(String userAddress, Long postId, boolean isMark, boolean isLike) {
        Post post = postMapper.selectByPrimaryKey(postId);
        if (post == null) {
            return;
        }
        Liked like = likeService.queryLiked(userAddress, post.getMarketId(), PoConstant.Liked.Type.Post, String.valueOf(postId));
        if (isMark) {
            if (like == null) {
                likeService.addMark(userAddress, post.getMarketId(), PoConstant.Liked.Type.Post, isLike, String.valueOf(postId));
                modLikeCount(postId, isLike, true);
                if (isLike) {
                    userService.updateReceivedLikeCount(post.getUserAddress(), post.getMarketId(), 1);
                }
            } else {
                // 判断重复
                if (like.getIsLike() != isLike) {
                    // 修改like, 修改post data
                    like.setIsLike(isLike);
                    likeService.mod(like);
                    modLikeCount(postId, isLike);
                    if (isLike) {
                        userService.updateReceivedLikeCount(post.getUserAddress(), post.getMarketId(), 1);
                    }
                }
            }
        } else {
            if (like != null) {
                likeService.delMark(userAddress, post.getMarketId(), PoConstant.Liked.Type.Post, String.valueOf(postId));
                modLikeCount(postId, isLike, false);
                if (isLike) {
                    userService.updateReceivedLikeCount(post.getUserAddress(), post.getMarketId(), -1);
                }
            }
        }
    }

    private void modLikeCount(Long postId, boolean newIsLike) {
        PostData data = postDataMapper.selectByPrimaryKey(postId);
        if (data == null) {
            return;
        }
        if (newIsLike) {
            data.setLikeCount(data.getLikeCount() + 1);
            Integer count = data.getDislikeCount() - 1;
            data.setDislikeCount(count >= 0 ? count : 0);
        } else {
            data.setDislikeCount(data.getDislikeCount() + 1);
            Integer count = data.getLikeCount() - 1;
            data.setLikeCount(count >= 0 ? count : 0);
        }
        postDataMapper.updateByPrimaryKey(data);
    }

    private void modLikeCount(Long postId, boolean isLike, boolean addOrSubtract) {
        PostData data = postDataMapper.selectByPrimaryKey(postId);
        if (data == null) {
            return;
        }
        if (isLike) {
            Integer count = data.getLikeCount() + (addOrSubtract ? 1 : -1);
            if (count.compareTo(0) < 0) {
                count = 0;
            }
            data.setLikeCount(count);
        } else {
            Integer count = data.getDislikeCount() + (addOrSubtract ? 1 : -1);
            if (count.compareTo(0) < 0) {
                count = 0;
            }
            data.setDislikeCount(count);
        }
        postDataMapper.updateByPrimaryKey(data);
    }

    public boolean isExist(Long postId) {
        return postMapper.selectByPrimaryKey(postId) != null;
    }

    public Post query(Long postId) {
        return postMapper.selectByPrimaryKey(postId);
    }

    public Post queryWithData(String userAddress, Long postId) {
        Post post = postMapper.selectByPrimaryKey(postId);
        if (post != null) {
            post.setData(postDataMapper.selectByPrimaryKey(postId));
        }
        post.setUser(userService.query(post.getUserAddress()));
        likeService.queryFillLikeForPost(userAddress, post.getMarketId(), post);
        collectService.fillCollectForPosts(userAddress, Arrays.asList(post));
        return post;
    }

    public Pagination<Post> queryPage(String query, String userAddress, String type, String marketId, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Post.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isBlank(type) || PoConstant.Post.Type.Root.equalsIgnoreCase(type)) {
            // root
            criteria.andEqualTo("type", PoConstant.Post.Type.Root);
        } else {
            // market
            criteria.andEqualTo("type", PoConstant.Post.Type.Market)
                    .andEqualTo("marketId", marketId);
        }
        if (StringUtils.isNotBlank(query)) {
            query = query.trim();
            query = query.length() > BuConstant.QueryMaxLength ? query.substring(0, BuConstant.QueryMaxLength) : query;
            criteria.andLike("title", "%" + query + "%");
        }
        example.orderBy("createTime").desc();
        Page<Post> page = PageHelper.startPage(pageNumb, pageSize);
        postMapper.selectByExample(example);

        fillPostData(page.getResult());
        userService.fillUserForPost(page.getResult());
        collectService.fillCollectForPosts(userAddress, page.getResult());
        likeService.queryFillLikeForPosts(userAddress, page.getResult());
        replyService.fillLastReply(page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Post> queryUserCreated(String userAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Post.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Post> page = PageHelper.startPage(pageNumb, pageSize);
        postMapper.selectByExample(example);

        fillPostData(page.getResult());
        userService.fillUserForPost(page.getResult());
        collectService.fillCollectForPosts(userAddress, page.getResult());
        likeService.queryFillLikeForPosts(userAddress, page.getResult());
        replyService.fillLastReply(page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Post> queryUserCollected(String userAddress, Integer pageNumb, Integer pageSize) {
        Pagination<Collect> page = collectService.queryPage(userAddress, PoConstant.Collect.Type.Post, pageNumb, pageSize);
        if (page.getRowCount() <= 0) {
            return Pagination.blank();
        }
        List<String> postIds = page.getList().stream().map(Collect::getObjectMark).collect(Collectors.toList());

        Example postExample = new Example(Post.class);
        Example.Criteria criteria = postExample.createCriteria();
        criteria.andIn("postId", postIds);
        List<Post> list = postMapper.selectByExample(postExample);
        list.forEach(post -> post.setIsCollected(true));

        List<Post> ret = new ArrayList<>();
        postIds.forEach(id -> {
            ret.add(list.stream().filter(p -> id.equals(String.valueOf(p.getPostId()))).findFirst().orElse(null));
        });

        fillPostData(ret);
        userService.fillUserForPost(ret);
        likeService.queryFillLikeForPosts(userAddress, ret);
        replyService.fillLastReply(ret);

        return Pagination.init(page.getRowCount(), page.getPageNumb(), page.getPageSize(), ret);
    }

    public Integer queryLatelyReplyCount() {
        Example example = new Example(PostData.class);
        Date lately = Common.getSomeDaysAgo(new Date(), 30);
        example.createCriteria().andGreaterThanOrEqualTo("lastReplyTime", lately);
        return postDataMapper.selectCountByExample(example);
    }

    public void fillPostForReply(List<Reply> replies) {
        if (replies == null || replies.size() <= 0) {
            return;
        }
        List<Long> postIds = replies.stream().map(Reply::getPostId).collect(Collectors.toList());
        Example example = new Example(Post.class);
        example.createCriteria().andIn("postId", postIds);
        List<Post> postList = postMapper.selectByExample(example);
        replies.forEach(r -> r.setPost(postList.stream().filter(p -> p.getPostId().equals(r.getPostId())).findFirst().orElse(null)));
    }

    private void fillPostData(List<Post> posts) {
        if (posts == null || posts.size() <= 0) {
            return;
        }
        List<Long> postIds = posts.stream().map(Post::getPostId).collect(Collectors.toList());
        Example example = new Example(PostData.class);
        example.createCriteria().andIn("postId", postIds);
        List<PostData> dataList = postDataMapper.selectByExample(example);
        posts.forEach(p -> p.setData(dataList.stream().filter(d -> d.getPostId().equals(p.getPostId())).findFirst().orElse(null)));
    }

}
