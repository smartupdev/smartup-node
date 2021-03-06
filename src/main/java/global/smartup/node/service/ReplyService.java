package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.constant.PoConstant;
import global.smartup.node.mapper.PostDataMapper;
import global.smartup.node.mapper.PostMapper;
import global.smartup.node.mapper.ReplyDataMapper;
import global.smartup.node.mapper.ReplyMapper;
import global.smartup.node.po.*;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyService.class);

    @Autowired
    private PostDataMapper postDataMapper;

    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private ReplyDataMapper replyDataMapper;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectService collectService;

    @Autowired
    private PostService postService;


    public void add(Reply reply) {
        String id = idGenerator.getStringId();
        if (reply.getFatherId() == null) {
            reply.setFatherId("0");
        }
        reply.setReplyId(id);
        reply.setCreateTime(new Date());
        replyMapper.insert(reply);

        ReplyData replyData = new ReplyData();
        replyData.setReplyId(id);
        replyData.setLikeCount(0);
        replyData.setDislikeCount(0);
        replyDataMapper.insert(replyData);

        PostData data = postDataMapper.selectByPrimaryKey(reply.getPostId());
        data.setReplyCount(data.getReplyCount() + 1);
        data.setLastReplyTime(new Date());
        data.setLastReplyId(id);
        postDataMapper.updateByPrimaryKey(data);

        Post post = postMapper.selectByPrimaryKey(reply.getPostId());
        if (post != null) {
            userService.updateReplyCount(reply.getUserAddress(), post.getMarketId());
        }
    }

    public void modLike(String userAddress, String replyId, boolean isMark, boolean isLike) {
        Reply reply = replyMapper.selectByPrimaryKey(replyId);
        if (reply == null) {
            return;
        }
        Post post = postMapper.selectByPrimaryKey(reply.getPostId());
        Liked like = likeService.queryLiked(userAddress, post.getMarketId(), PoConstant.Liked.Type.Reply, String.valueOf(replyId));
        if (isMark) {
            if (like == null) {
                likeService.addMark(userAddress, post.getMarketId(), PoConstant.Liked.Type.Reply, isLike, String.valueOf(replyId));
                modLikeCount(replyId, isLike, true);
            } else {
                // 判断重复
                if (like.getIsLike() != isLike) {
                    // 修改like, 修改post data
                    like.setIsLike(isLike);
                    likeService.mod(like);
                    modLikeCount(replyId, isLike);
                }
            }
        } else {
            if (like != null) {
                likeService.delMark(userAddress, post.getMarketId(), PoConstant.Liked.Type.Reply, String.valueOf(replyId));
                modLikeCount(replyId, isLike, false);
            }
        }
    }

    private void modLikeCount(String replyId, boolean newIsLike) {
        ReplyData data = replyDataMapper.selectByPrimaryKey(replyId);
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
        replyDataMapper.updateByPrimaryKey(data);
    }

    private void modLikeCount(String replyId, boolean isLike, boolean addOrSubtract) {
        ReplyData data = replyDataMapper.selectByPrimaryKey(replyId);
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
        replyDataMapper.updateByPrimaryKey(data);
    }

    public boolean isExist(String replyId) {
        return replyMapper.selectByPrimaryKey(replyId) != null;
    }

    public Reply query(String replyId) {
        return replyMapper.selectByPrimaryKey(replyId);
    }

    public Pagination<Reply> queryPage(String query, String userAddress, String postId, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Reply.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("postId", postId);
        if (StringUtils.isBlank(query)) {
            criteria.andEqualTo("fatherId", 0);
        }
        if (StringUtils.isNotBlank(query)) {
            query = query.trim();
            query = query.length() > BuConstant.QueryMaxLength ? query.substring(0, BuConstant.QueryMaxLength) : query;
            criteria.andLike("content", "%" + query + "%");
        }
        example.orderBy("createTime").desc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);

        userService.fillUserForReply(page.getResult());
        likeService.queryFillLikeForReplies(userAddress, page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());
        fillData(page.getResult());
        fillChildren(userAddress, page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Reply> queryChildren(String userAddress, String fatherId, Integer pageNumb, Integer pageSize) {
        Reply reply = replyMapper.selectByPrimaryKey(fatherId);
        if (reply == null) {
            return null;
        }
        Post post = postMapper.selectByPrimaryKey(reply.getPostId());
        if (post == null) {
            return null;
        }

        Example example = new Example(Reply.class);
        example.createCriteria().andEqualTo("fatherId", fatherId);
        example.orderBy("createTime").asc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);

        userService.fillUserForReply(page.getResult());
        likeService.queryFillLikeForReplies(userAddress, page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());
        fillData(page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Reply> queryUserCreated(String userAddress, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Reply.class);
        example.createCriteria().andEqualTo("userAddress", userAddress);
        example.orderBy("createTime").desc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);

        postService.fillPostForReply(page.getResult());
        userService.fillUserForReply(page.getResult());
        likeService.queryFillLikeForReplies(userAddress, page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());
        fillMarketId(page.getResult());
        fillData(page.getResult());
        fillChildren(userAddress, page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Reply> queryUserCollected(String userAddress, Integer pageNumb, Integer pageSize) {
        Pagination<Collect> cPage = collectService.queryPage(userAddress, PoConstant.Collect.Type.Reply, pageNumb, pageSize);
        if (cPage.getRowCount() <= 0) {
            return Pagination.blank();
        }
        List<String> replyIds = cPage.getList().stream().map(Collect::getObjectMark).collect(Collectors.toList());

        Example example = new Example(Reply.class);
        example.createCriteria().andIn("replyId", replyIds);
        example.orderBy("createTime").desc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);

        postService.fillPostForReply(page.getResult());
        userService.fillUserForReply(page.getResult());
        likeService.queryFillLikeForReplies(userAddress, page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());
        fillMarketId(page.getResult());
        fillData(page.getResult());
        fillChildren(userAddress, page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    private void fillChildren(String userAddress, List<Reply> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        list.stream().forEach(i -> {
            if ("0".equals(i.getFatherId())) {
                i.setChildrenPage(queryChildren(userAddress, i.getReplyId(), 1, BuConstant.DefaultPageSize));
            }
        });
    }

    public void fillLastReply(List<Post> posts) {
        if (posts == null || posts.size() <= 0) {
            return;
        }
        List<String> replyIds = posts.stream().map(p -> p.getData().getLastReplyId()).collect(Collectors.toList());
        Example example = new Example(Reply.class);
        example.createCriteria().andIn("replyId", replyIds);
        List<Reply> replyList = replyMapper.selectByExample(example);
        userService.fillUserForReply(replyList);
        posts.forEach(p -> p.setLastReply(replyList.stream().filter(r -> r.getReplyId().equals(p.getData().getLastReplyId())).findFirst().orElse(null)));
    }

    private void fillData(List<Reply> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        List<String> ids = list.stream().map(Reply::getReplyId).collect(Collectors.toList());
        Example example = new Example(ReplyData.class);
        example.createCriteria().andIn("replyId", ids);
        List<ReplyData> dataList = replyDataMapper.selectByExample(example);
        list.forEach(r -> r.setData(dataList.stream().filter(d -> r.getReplyId().equals(d.getReplyId())).findFirst().orElse(null)));
    }

    private void fillMarketId(List<Reply> list) {
        if (list == null || list.size() <= 0) {
            return;
        }
        List<String> postIds = list.stream().map(Reply::getPostId).distinct().collect(Collectors.toList());
        Example example = new Example(Post.class);
        example.createCriteria().andIn("postId", postIds);
        List<Post> postList = postMapper.selectByExample(example);
        list.forEach(r -> postList.stream().filter(p -> p.getPostId().equals(r.getPostId())).findFirst().ifPresent(p -> r.setMarketId(p.getMarketId())));
    }

}
