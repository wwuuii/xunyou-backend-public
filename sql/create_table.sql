-- 创建库
CREATE
DATABASE yupao CHARACTER SET utf8mb4 if not exists xunyou;
-- 切换库
use xunyou;

-- 用户表
CREATE TABLE `user`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userAccount`  varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
    `userPassword` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
    `userName`     varchar(256) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '用户昵称',
    `userAvatar`   varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg' COMMENT '用户头像',
    `userProfile`  varchar(512) COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '用户简介',
    `userRole`     varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user/admin/ban',
    `tags`         varchar(1024) COLLATE utf8mb4_unicode_ci         DEFAULT NULL COMMENT '标签',
    `createTime`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`   datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`     tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    `phone`        int(11) DEFAULT NULL COMMENT '手机号',
    `gender`       tinyint(4) DEFAULT NULL COMMENT '性别',
    `email`        varchar(48) COLLATE utf8mb4_unicode_ci           DEFAULT NULL COMMENT '邮箱',
    `latestLogin`  datetime                                         DEFAULT NULL COMMENT '最后登录时间',
    `status`       tinyint(4) DEFAULT '0' COMMENT '用户状态：0：可见，1：不可见',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_userAccount` (`userAccount`) USING BTREE COMMENT '用户账号唯一性索引'
) ENGINE=InnoDB AUTO_INCREMENT=1677876276312242824 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `private_message`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `senderId`       bigint(20) NOT NULL COMMENT '发送者Id',
    `receiverId`     bigint(20) NOT NULL COMMENT '接收者Id',
    `messageContent` text COLLATE utf8mb4_unicode_ci COMMENT '消息',
    `status`         tinyint(4) DEFAULT '0' COMMENT '0：未读，1：已读',
    `createTime`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
    `updateTime`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`       tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY              `idx_receiverId` (`receiverId`) USING BTREE,
    KEY              `idx_senderId` (`senderId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1681252698719965186 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';
-- 标签表
CREATE TABLE `tag`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `tagName`    varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名',
    `parentId`   bigint(20) DEFAULT NULL COMMENT '父标签id',
    `isParent`   tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否父标签：0-不是，1-是',
    `createTime` datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime                                NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`   tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_tagName` (`tagName`)
) ENGINE=InnoDB AUTO_INCREMENT=1676450437741359158 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE `team`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `name`        varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '队伍名称',
    `description` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
    `maxNum`      int(11) NOT NULL DEFAULT '1' COMMENT '最大人数',
    `expireTime`  datetime                                 DEFAULT NULL COMMENT '过期时间',
    `userId`      bigint(20) DEFAULT NULL COMMENT '用户id',
    `status`      int(11) NOT NULL DEFAULT '0' COMMENT '0 - 公开，1 - 私有，2 - 加密',
    `password`    varchar(512) COLLATE utf8mb4_unicode_ci  DEFAULT NULL COMMENT '密码',
    `createTime`  datetime                                 DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime`  datetime                                 DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete`    tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1681920795588083714 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='队伍';

CREATE TABLE `user_message`
(
    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `senderId`       bigint(20) NOT NULL COMMENT '发送者Id',
    `receiverId`     bigint(20) NOT NULL COMMENT '接收者Id',
    `messageContent` text COLLATE utf8mb4_unicode_ci COMMENT '消息',
    `createTime`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息发送时间',
    `updateTime`     datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `isDelete`       tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1681221678679363588 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户消息表';

CREATE TABLE `user_team`
(
    `id`         bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `userId`     bigint(20) DEFAULT NULL COMMENT '用户id',
    `teamId`     bigint(20) DEFAULT NULL COMMENT '队伍id',
    `joinTime`   datetime DEFAULT NULL COMMENT '加入时间',
    `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updateTime` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `isDelete`   tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
    PRIMARY KEY (`id`),
    KEY          `idx_userId` (`userId`) USING BTREE,
    KEY          `idx_teamId` (`teamId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1681920795676164099 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户队伍关系';