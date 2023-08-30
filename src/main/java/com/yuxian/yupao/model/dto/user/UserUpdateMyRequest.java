package com.yuxian.yupao.model.dto.user;

import java.io.Serializable;
import lombok.Data;

/**
 * 用户更新个人信息请求
 *

 */
@Data
public class UserUpdateMyRequest implements Serializable {

    private Long id;
    private String email;
    private Integer gender;
    private Integer phone;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
    private String tags;
    /**
     * 简介
     */
    private String userProfile;

    private static final long serialVersionUID = 1L;
}