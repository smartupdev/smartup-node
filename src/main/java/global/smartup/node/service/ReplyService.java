package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.mapper.PostDataMapper;
import global.smartup.node.mapper.PostMapper;
import global.smartup.node.mapper.ReplyMapper;
import global.smartup.node.po.Post;
import global.smartup.node.po.PostData;
import global.smartup.node.po.Reply;
import global.smartup.node.util.Pagination;
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
    private IdGenerator idGenerator;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollectService collectService;


    public void add(Reply reply) {
        Long id = idGenerator.getId();
        if (reply.getFatherId() == null) {
            reply.setFatherId(0L);
        }
        reply.setReplyId(id);
        reply.setCreateTime(new Date());
        replyMapper.insert(reply);

        PostData data = postDataMapper.selectByPrimaryKey(reply.getPostId());
        data.setReplyCount(data.getReplyCount() + 1);
        data.setLastReplyTime(new Date());
        data.setLastReplyId(id);
        postDataMapper.updateByPrimaryKey(data);
    }

    public boolean isExist(Long replyId) {
        return replyMapper.selectByPrimaryKey(replyId) != null;
    }

    public Reply query(Long replyId) {
        return replyMapper.selectByPrimaryKey(replyId);
    }

    public Pagination<Reply> queryPage(String userAddress, Long postId, Integer pageNumb, Integer pageSize) {
        Post post = postMapper.selectByPrimaryKey(postId);
        Example example = new Example(Reply.class);
        example.createCriteria()
                .andEqualTo("postId", postId)
                .andEqualTo("fatherId", 0);
        example.orderBy("createTime").asc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);

        userService.fillUserForReply(page.getResult());
        likeService.queryFillLikeForReplies(userAddress, post.getMarketId(), page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());
        fillChildren(userAddress, page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Reply> queryChildren(String userAddress, Long fatherId, Integer pageNumb, Integer pageSize) {
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
        likeService.queryFillLikeForReplies(userAddress, post.getMarketId(), page.getResult());
        collectService.fillCollectForReplies(userAddress, page.getResult());

        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public void fillChildren(String userAddress, List<Reply> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        list.stream().forEach(i -> {
            i.setChildrenPage(queryChildren(userAddress, i.getReplyId(), 1, BuConstant.DefaultPageSize));
        });

    }

    public void fillLastReply(List<Post> posts) {
        if (posts == null || posts.size() <= 0) {
            return;
        }
        List<Long> replyIds = posts.stream().map(p -> p.getData().getLastReplyId()).collect(Collectors.toList());
        Example example = new Example(Reply.class);
        example.createCriteria().andIn("replyId", replyIds);
        List<Reply> replyList = replyMapper.selectByExample(example);
        userService.fillUserForReply(replyList);
        posts.forEach(p -> p.setLastReply(replyList.stream().filter(r -> r.getReplyId().equals(p.getData().getLastReplyId())).findFirst().orElse(null)));
    }

}
