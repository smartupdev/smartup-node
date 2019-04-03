package global.smartup.node.mapper;

import global.smartup.node.po.Reply;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ReplyMapper extends Mapper<Reply> {


    @Select("select * from ")
    List<Reply> selectByFatherIds(List<Long> ids);

}
