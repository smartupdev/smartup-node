package global.smartup.node.mapper;

import global.smartup.node.po.TradeChild;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TradeChildMapper extends Mapper<TradeChild> {

    List<TradeChild> selectTop(@Param("marketId") String marketId, @Param("top") Integer top);

}
