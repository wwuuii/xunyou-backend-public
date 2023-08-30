package com.yuxian.yupao.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yuxian.yupao.common.PageRequest;
import com.yuxian.yupao.model.dto.team.TeamQuery;
import com.yuxian.yupao.model.dto.user.UserQueryRequest;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.vo.team.TeamUserVO;
import com.yuxian.yupao.model.vo.user.LoginUserVO;
import com.yuxian.yupao.model.vo.user.UserVO;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *

 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param userName 用户名
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String userName);

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    User getLoginUserPermitNull(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    UserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);

    /**
     * 批量查询用户名与t头像
     * @param ids
     * @return
     */
    List<User> batchQueryByIds(List<Long> ids);
    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 根据标签查询用户
     *
     * @param tags
     * @return
     */
    List<UserVO> searchUserByTags(List<String> tags);

    /**
     *
     * @param userId
     * @param tagIds
     * @return
     */
    boolean updateUserTags(Long userId, Set<Long> tagIds);

    /**
     * 查询用户推荐列表
     * @return
     */
    List<UserVO> recommend(User user, Integer pageNum);

    List<UserVO> userListToUserVoList(List<User> users);

    /**
     * 根据标签使用编辑举例算法匹配用户
     * @param pageNum
     * @param user
     * @return
     */
    List<UserVO> matchUsers(Integer pageNum, User user);
}
