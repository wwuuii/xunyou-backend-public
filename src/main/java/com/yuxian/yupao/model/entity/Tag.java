package com.yuxian.yupao.model.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

/**
 * 标签表
 * @TableName tag
 */
@Data
public class Tag implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 父标签id
     */
    private Long parentId;

    /**
     * 是否父标签：0-不是，1-是
     */
    private Integer isParent;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}