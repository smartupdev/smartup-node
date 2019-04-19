package global.smartup.node.mapper;

import global.smartup.node.po.Notification;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface NotificationMapper extends Mapper<Notification> {

    List<Notification> selectUnreadFew(@Param("userAddress") String userAddress, @Param("limit") Integer limit);

    Integer selectUnreadCount(@Param("userAddress") String userAddress);

}
