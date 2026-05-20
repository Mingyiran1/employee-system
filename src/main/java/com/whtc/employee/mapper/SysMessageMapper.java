package com.whtc.employee.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whtc.employee.entity.SysMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 系统消息Mapper
 */
@Mapper
public interface SysMessageMapper extends BaseMapper<SysMessage> {

    /**
     * 标记消息为已读
     */
    @Update("UPDATE sys_message SET is_read = 1 WHERE id = #{messageId}")
    int markAsRead(@Param("messageId") Long messageId);

    /**
     * 标记用户所有消息为已读
     */
    @Update("UPDATE sys_message SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
