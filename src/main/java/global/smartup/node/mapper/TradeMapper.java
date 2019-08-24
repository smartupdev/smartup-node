package global.smartup.node.mapper;

import global.smartup.node.po.Trade;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TradeMapper extends Mapper<Trade> {

    List<Trade> selectPage(@Param("marketId") String marketId, @Param("state") String state, @Param("offset") Integer offset, @Param("size") Integer size);

}
