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


    public void add(Reply reply) {
        if (reply.getFatherId() == null) {
            reply.setFatherId(0L);
        }
        reply.setReplyId(idGenerator.getId());
        reply.setCreateTime(new Date());
        replyMapper.insert(reply);

        PostData data = postDataMapper.selectByPrimaryKey(reply.getPostId());
        data.setReplyCount(data.getReplyCount() + 1);
        data.setLastReplyTime(new Date());
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
        likeService.queryFillLikeForReply(userAddress, post.getMarketAddress(), page.getResult());
        fillChildren(userAddress, post.getMarketAddress(), page.getResult());
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public Pagination<Reply> queryChildren(Long fatherId, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Reply.class);
        example.createCriteria().andEqualTo("fatherId", fatherId);
        example.orderBy("createTime").asc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);
        return Pagination.init(page.getTotal(), page.getPageNum(), page.getPageSize(), page.getResult());
    }

    public void fillChildren(String userAddress, String marketAddress, List<Reply> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        list.stream().forEach(i -> {
            i.setChildrenPage(queryChildren(i.getReplyId(), 1, BuConstant.DefaultPageSize));
            likeService.queryFillLikeForReply(userAddress, marketAddress, i.getChildrenPage().getList());
        });

    }

}
