package com.yuxian.yupao.service;

import javax.annotation.Resource;

import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 用户服务测试
 *

 */
@SpringBootTest
@Slf4j
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
//        String userAccount = "yuxian";
//        String userPassword = "";
//        String checkPassword = "123456";
//        try {
//            long result = userService.userRegister(userAccount, userPassword, checkPassword);
//            Assertions.assertEquals(-1, result);
//            userAccount = "yu";
//            result = userService.userRegister(userAccount, userPassword, checkPassword);
//            Assertions.assertEquals(-1, result);
//        } catch (Exception e) {
//
//        }
    }

    @Test
    void searchUserByTags() {
        List<UserVO> users = userService.searchUserByTags(Arrays.asList("Java", "Python"));
        if (CollectionUtils.isNotEmpty(users)) {
            System.out.println(users.size());
        }
    }


    @Test
    void batchAddUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                for (int j = 0; j < 10; j++) {
                    List<User> users = new ArrayList<>();
                    for (int k = 0; k < 1000; k++) {
                        User addUser = new User();
                        addUser.setUserAccount(UUID.randomUUID().toString());
                        addUser.setUserPassword("12345678");
                        addUser.setUserName("test");
                        addUser.setUserAvatar("https://ts1.cn.mm.bing.net/th/id/R-C.7ee2d80d0db2f787fed339d64eb6499b?rik=L%2bcPVmZBtmnGhw&riu=http%3a%2f%2fp6.zbjimg.com%2ftask%2f2012-08%2f21%2f1954425%2f503288c32ffe6.jpg&ehk=NLTxkBC%2frhd4Ok4N2sp8lQaFWZlMl1cHosGE1rKMaf0%3d&risl=&pid=ImgRaw&r=0");
                        addUser.setUserProfile("test");
                        addUser.setUserRole("test");
                        addUser.setTags("MySQL");
                        addUser.setEmail("test");
                        addUser.setGender(0);
                        addUser.setPhone(0);
                        users.add(addUser);
                    }
                    userService.saveBatch(users);
                }
            });
            completableFutures.add(voidCompletableFuture);
        }
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0])).join();
        stopWatch.stop();
        log.info("用时 {} s",stopWatch.getTotalTimeSeconds());
    }
}
