package global.smartup.node.mapper;

import global.smartup.node.po.CTAccount;
import global.smartup.node.vo.CTAccountWithMarket;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CTAccountMapper extends Mapper<CTAccount> {


    List<CTAccountWithMarket> selectWidthMarket(@Param("userAddress") String userAddress);

}
