package global.smartup.node.mapper;

import global.smartup.node.po.Collect;
import global.smartup.node.po.Market;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CollectMapper extends Mapper<Collect> {

    List<Market> selectCollectedMarket(@Param("userAddress") String userAddress, @Param("asc") Boolean asc);

    List<Market> selectCollectedPost(@Param("userAddress") String userAddress, @Param("asc") Boolean asc);

}
