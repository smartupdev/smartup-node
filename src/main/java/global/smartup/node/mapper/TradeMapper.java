package global.smartup.node.mapper;

import global.smartup.node.po.Trade;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TradeMapper extends Mapper<Trade> {


    List<Trade> selectOrderBy(@Param("marketAddress") String marketAddress, @Param("type") String type, @Param("asc") Boolean asc);

}
