package global.smartup.node.mapper;

import global.smartup.node.po.MarketData;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.math.BigDecimal;
import java.util.List;

public interface MarketDataMapper extends Mapper<MarketData> {

    BigDecimal selectAllMarketAmount(@Param("statuses") List<String> statuses);

}
