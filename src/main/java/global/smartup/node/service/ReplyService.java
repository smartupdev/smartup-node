package global.smartup.node.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import global.smartup.node.compoment.IdGenerator;
import global.smartup.node.constant.BuConstant;
import global.smartup.node.mapper.ReplyMapper;
import global.smartup.node.po.Reply;
import global.smartup.node.util.Pagination;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReplyService {

    private static final Logger log = LoggerFactory.getLogger(ReplyService.class);

    @Autowired
    private ReplyMapper replyMapper;

    @Autowired
    private IdGenerator idGenerator;

    public void add(Reply reply) {
        if (reply.getFatherId() == null) {
            reply.setFatherId(0L);
        }
        reply.setReplyId(idGenerator.getId());
        reply.setCreateTime(new Date());
        replyMapper.insert(reply);
    }

    public boolean isExist(Long replyId) {
        return replyMapper.selectByPrimaryKey(replyId) != null;
    }

    public Reply query(Long replyId) {
        return replyMapper.selectByPrimaryKey(replyId);
    }

    public Pagination<Reply> queryPage(Long postId, Integer pageNumb, Integer pageSize) {
        Example example = new Example(Reply.class);
        example.createCriteria()
                .andEqualTo("postId", postId)
                .andEqualTo("fatherId", 0);
        example.orderBy("createTime").asc();
        Page<Reply> page = PageHelper.startPage(pageNumb, pageSize);
        replyMapper.selectByExample(example);
        fillChildren(page.getResult());
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

    public void fillChildren(List<Reply> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        list.stream().forEach(i -> {
            i.setChildrenPage(queryChildren(i.getReplyId(), 1, BuConstant.DefaultPageSize));
        });
    }

}
