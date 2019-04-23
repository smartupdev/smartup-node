package global.smartup.node.mapper;

import global.smartup.node.po.Market;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MarketMapper extends Mapper<Market> {


    List<Market> selectOrderBy(@Param("name") String name, @Param("orderBy") String orderBy, @Param("asc") boolean asc);

}
