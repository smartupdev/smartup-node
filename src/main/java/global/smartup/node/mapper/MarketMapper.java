package global.smartup.node.mapper;

import global.smartup.node.po.Market;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface MarketMapper extends Mapper<Market> {


    List<Market> selectNameLikeAndOrderBy(@Param("name") String name, @Param("inData") boolean inData,
                                          @Param("orderBy") String orderBy, @Param("asc") boolean asc);

}
