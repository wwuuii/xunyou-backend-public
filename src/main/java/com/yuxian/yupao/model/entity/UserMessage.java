package com.yuxian.yupao.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户消息表
 * @TableName user_message
 */
@Data
public class UserMessage implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 发送者Id
     */
    private Long senderId;

    /**
     * 接收者Id
     */
    private Long receiverId;

    /**
     * 消息
     */
    private String messageContent;

    /**
     * 消息发送时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}