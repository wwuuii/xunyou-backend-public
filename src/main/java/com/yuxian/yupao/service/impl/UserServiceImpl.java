package com.yuxian.yupao.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Pair;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.constant.CommonConstant;
import com.yuxian.yupao.constant.RedisConstant;
import com.yuxian.yupao.enums.UserStatusEnum;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.mapper.UserMapper;
import com.yuxian.yupao.model.dto.user.UserQueryRequest;
import com.yuxian.yupao.model.entity.Tag;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.enums.UserRoleEnum;
import com.yuxian.yupao.model.vo.user.UserVO;
import com.yuxian.yupao.service.PrivateMessageService;
import com.yuxian.yupao.service.TagService;
import com.yuxian.yupao.service.UserService;
import com.yuxian.yupao.utils.JWTUtils;
import com.yuxian.yupao.utils.RecommendAlgorithmUtils;
import com.yuxian.yupao.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yuxian.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	/**
	 * 盐值，混淆密码
	 */
	private static final String SALT = "yuxian";

	@Resource
	private UserMapper userMapper;
	@Resource
	private RedisTemplate<String, Object> redisTemplate;
	@Resource
	private ObjectMapper objectMapper;
	@Resource
	private TagService tagService;
	@Resource
	private PrivateMessageService privateMessageService;

	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
		}
		if (userName.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户名过短");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		}
		synchronized (userAccount.intern()) {
			// 账户不能重复
			QueryWrapper<User> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("userAccount", userAccount);
			long count = this.baseMapper.selectCount(queryWrapper);
			if (count > 0) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
			}
			// 2. 加密
			String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
			// 3. 插入数据
			User user = new User();
			user.setUserAccount(userAccount);
			user.setUserPassword(encryptPassword);
			user.setUserName(userName);
			boolean saveResult = this.save(user);
			if (!saveResult) {
				throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
			}
			return user.getId();
		}
	}

	@Override
	public UserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 1. 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不能小于4");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不能小于8");
		}
		// 2. 加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 查询用户是否存在
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("userAccount", userAccount);
		User user = this.baseMapper.selectOne(queryWrapper);
		// 用户不存在
		if (user == null || !encryptPassword.equals(user.getUserPassword())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
		}
		// 3. 生成token
		String token = JWTUtils.getToken(new HashMap<>());
		user.setToken(token);
		redisTemplate.opsForValue().set(RedisConstant.LOGIN_KEY + token, user.getId(), 1, TimeUnit.DAYS);
		// 4. 修改用户最近登陆时间
		renewLatestLoginTime(user.getId());
		return this.getLoginUserVO(user);
	}

	private void renewLatestLoginTime(Long id) {
		User updateUser = new User();
		updateUser.setId(id);
		updateUser.setLatestLogin(new Date());
		boolean updateResult = this.updateById(updateUser);
		if (!updateResult) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败，数据处理失败");
		}
	}

	/**
	 * 获取当前登录用户
	 *
	 * @param request
	 * @return
	 */
	@Override
	public User getLoginUser(HttpServletRequest request) {
		String token = request.getHeader("Authorization");
		try {
			Long userId = (Long) redisTemplate.opsForValue().get(RedisConstant.LOGIN_KEY + token);
			ThrowUtils.throwIf(userId <= 0, ErrorCode.NOT_LOGIN_ERROR);
			User currentUser = this.getById(userId);
			if (currentUser == null) {
				throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
			}
			//更新最近一次登陆时间
			renewLatestLoginTime(userId);
			return currentUser;
		} catch (Exception e) {
			throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
		}
	}

	/**
	 * 获取当前登录用户（允许未登录）
	 *
	 * @param request
	 * @return
	 */
	@Override
	public User getLoginUserPermitNull(HttpServletRequest request) {
		// 先判断是否已登录
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User currentUser = (User) userObj;
		if (currentUser == null || currentUser.getId() == null) {
			return null;
		}
		// 从数据库查询（追求性能的话可以注释，直接走缓存）
		long userId = currentUser.getId();
		return this.getById(userId);
	}

	/**
	 * 是否为管理员
	 *
	 * @param request
	 * @return
	 */
	@Override
	public boolean isAdmin(HttpServletRequest request) {
		// 仅管理员可查询
		Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
		User user = (User) userObj;
		return isAdmin(user);
	}

	@Override
	public boolean isAdmin(User user) {
		return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
	}

	/**
	 * 用户注销
	 *
	 * @param request
	 */
	@Override
	public boolean userLogout(HttpServletRequest request) {
		//校验是否已登录
		String token = request.getHeader("Authorization");
		//移除登录态
		redisTemplate.delete(RedisConstant.LOGIN_KEY + token);
		return true;
	}

	@Override
	public UserVO getLoginUserVO(User user) {
		if (user == null) {
			return null;
		}
		return userToUserVo(user);
	}

	@Override
	public UserVO getUserVO(User user) {
		if (user == null) {
			return null;
		}
		UserVO userVO = new UserVO();
		BeanUtils.copyProperties(user, userVO);
		return userVO;
	}

	@Override
	public List<UserVO> getUserVO(List<User> userList) {
		if (CollectionUtils.isEmpty(userList)) {
			return new ArrayList<>();
		}
		return userList.stream().map(this::getUserVO).collect(Collectors.toList());
	}

	@Override
	public List<User> batchQueryByIds(List<Long> ids) {
		if (CollectionUtils.isEmpty(ids)) {
			return new ArrayList<>();
		}
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "userName", "userAvatar");
		queryWrapper.in("id", ids);
		return list(queryWrapper);
	}

	@Override
	public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
		if (userQueryRequest == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
		}
		Long id = userQueryRequest.getId();
		String unionId = userQueryRequest.getUnionId();
		String mpOpenId = userQueryRequest.getMpOpenId();
		String userName = userQueryRequest.getUserName();
		String userProfile = userQueryRequest.getUserProfile();
		String userRole = userQueryRequest.getUserRole();
		String sortField = userQueryRequest.getSortField();
		String sortOrder = userQueryRequest.getSortOrder();
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq(id != null, "id", id);
		queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
		queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
		queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
		queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
		queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
		queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
				sortField);
		return queryWrapper;
	}

	@Override
	public List<UserVO> searchUserByTags(List<String> tags) {
		if (CollectionUtils.isEmpty(tags)) {
			return new ArrayList<>();
		}
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
		tags.forEach(tag -> queryWrapper.like(User::getTags, tag));
		queryWrapper.eq(User::getStatus, UserStatusEnum.VISIBLE.getCode());
		List<User> users = userMapper.selectList(queryWrapper);
		return userListToUserVoList(users);
	}

	@Nullable
	public List<UserVO> userListToUserVoList(List<User> users) {
		List<UserVO> result = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(users)) {
			result = users.stream().map(user ->
					{
						UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
						Gson gson = new Gson();
						userVO.setTags(gson.fromJson(user.getTags(), new TypeToken<List<String>>() {
						}.getType()));
						return userVO;
					}
			).collect(Collectors.toList());
		}
		return result;
	}

	@Override
	public List<UserVO> matchUsers(Integer pageNum, User loginUser) {
		ThrowUtils.throwIf(pageNum <= 0, ErrorCode.PARAMS_ERROR);
		String tags = loginUser.getTags();
		if (StringUtils.isEmpty(tags)) {
			return recommend(loginUser, pageNum);
		}
		// 先从缓存拿
		Object cache = redisTemplate.opsForValue().get(RedisConstant.MATCH_KEY + loginUser.getId());
		if (cache != null) {
			List<UserVO> result = (List<UserVO>)cache;
			if (CollectionUtils.isNotEmpty(result) && result.size() >= pageNum * 10) {
				return result.stream().skip((pageNum - 1) * 10L).limit(10).collect(Collectors.toList());
			}
		}
		// 查询所有用户
		QueryWrapper<User> queryWrapper = new QueryWrapper<>();
		queryWrapper.select("id", "tags");
		queryWrapper.isNotNull("tags");
		queryWrapper.eq("status", UserStatusEnum.VISIBLE.getCode());
		List<User> userList = this.list(queryWrapper);
		Gson gson = new Gson();
		List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
		}.getType());
		// 用户列表的下标 => 相似度
		List<Pair<User, Long>> list = new ArrayList<>();
		// 依次计算所有用户和当前用户的相似度
		for (User user : userList) {
			String userTags = user.getTags();
			// 无标签或者为当前用户自己
			if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
				continue;
			}
			List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
			}.getType());
			// 计算分数
			long distance = RecommendAlgorithmUtils.recommendAlgorithmList(tagList, userTagList);
			list.add(new Pair<>(user, distance));
		}
		// 排序后的userIds
		List<Long> userIds = list.stream()
				.sorted((a, b) -> (int) (a.getValue() - b.getValue()))
				.map(pair -> pair.getKey().getId())
				.limit(Math.max(100, pageNum * 10))
				.collect(Collectors.toList());
		List<UserVO> result = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(userIds)) {
			Map<Long, User> usersMap = listByIds(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
			userIds.forEach(id -> result.add(userToUserVo(usersMap.getOrDefault(id, null))));
		}
		//存缓存
		redisTemplate.opsForValue().set(RedisConstant.MATCH_KEY + loginUser.getId(), result);
		return result;
	}

	private UserVO userToUserVo(User user) {
		if (user == null) {
			return null;
		}
		UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
		Gson gson = new Gson();
		userVO.setTags(gson.fromJson(user.getTags(), new TypeToken<List<String>>() {
		}.getType()));
		userVO.setUnReadNumSum(privateMessageService.getUnReadNumAll(user.getId()));
		return userVO;
	}

	@Override
	public boolean updateUserTags(Long userId, Set<Long> tagIds) {
		ThrowUtils.throwIf(userId == null, ErrorCode.PARAMS_ERROR);
		User user = this.getById(userId);
		ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, String.format("根据id=%d,查询不到对应的用户", userId));
		User updateUser = new User();
		updateUser.setId(userId);
		List<String> tags = tagService.queryByIds(tagIds).stream().map(Tag::getTagName).collect(Collectors.toList());
		Gson gson = new Gson();
		updateUser.setTags(gson.toJson(tags));
		return this.updateById(updateUser);
	}

	@Override
	public List<UserVO> recommend(User user, Integer pageNum) {
		try {
//			if (pageNum == 1) {
//				//查缓存
//				Object cache = redisTemplate.opsForValue().get(RedisConstant.RECOMMEND_KEY + user.getId());
//				if (cache != null) {
//					return (List<UserVO>) cache;
//				}
//			}
			QueryWrapper<User> wrapper = new QueryWrapper<>();
			wrapper.eq("status", UserStatusEnum.VISIBLE.getCode());
			wrapper.ne("id", user.getId());
			Page<User> page = page(new Page<>(pageNum, 10), wrapper);
			List<UserVO> result = userListToUserVoList(page.getRecords());
//			if (pageNum == 1) {
//				redisTemplate.opsForValue().set(RedisConstant.RECOMMEND_KEY + user.getId(), result, 1, TimeUnit.DAYS);
//			}
			return result;
		} catch (Exception e) {
			log.error("recommend error", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "系统异常");
		}

	}

	public List<User> getCachedUserList(String key) throws JsonProcessingException {
		List<User> userList = new ArrayList<>();
		List<LinkedHashMap<String, Object>> resultMapList = (List<LinkedHashMap<String, Object>>) redisTemplate.opsForValue().get(key);

		for (LinkedHashMap<String, Object> resultMap : resultMapList) {

			User user = objectMapper.convertValue(resultMap, User.class);
			userList.add(user);
		}

		return userList;
	}

}
