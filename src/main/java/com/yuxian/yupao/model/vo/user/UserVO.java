package com.yuxian.yupao.model.vo.user;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 用户视图（脱敏）
 *

 */
@Data
public class UserVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
    private List<String> tags;
    /**
     * 创建时间
     */
    private Date createTime;
    private String email;
    private Integer gender;
    private Integer phone;
    private Long unReadNumSum;
    private String token;
    private static final long serialVersionUID = 1L;
}