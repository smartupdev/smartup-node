package global.smartup.node.mapper;

import global.smartup.node.po.User;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<User> {

    List<String> selectTopUserOnPostAndReply(@Param("marketId") String marketId, @Param("limit") Integer limit);

    List<String> selectTopUserOnReceivedLike(@Param("marketId") String marketId, @Param("limit") Integer limit);

}
