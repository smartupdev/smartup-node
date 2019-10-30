package global.smartup.node.mapper;

import global.smartup.node.po.TakePlan;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface TakePlanMapper extends Mapper<TakePlan> {

    @Select("select * from take_plan where is_over = 0 order by create_time asc limit 1")
    TakePlan queryTop();

}
