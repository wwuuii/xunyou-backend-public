package com.yuxian.yupao.model.entity;


import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 私聊消息表
 * @TableName private_message
 */
@Data
public class PrivateMessage implements Serializable {
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
     * 0：未读，1：已读
     */
    private Integer status;

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