package com.yuxian.yupao.controller;

import cn.hutool.core.io.FileUtil;
import com.yuxian.yupao.common.BaseResponse;
import com.yuxian.yupao.common.ErrorCode;
import com.yuxian.yupao.common.ResultUtils;
import com.yuxian.yupao.constant.FileConstant;
import com.yuxian.yupao.exception.BusinessException;
import com.yuxian.yupao.exception.ThrowUtils;
import com.yuxian.yupao.manager.CosManager;
import com.yuxian.yupao.model.dto.file.UploadFileRequest;
import com.yuxian.yupao.model.entity.User;
import com.yuxian.yupao.model.enums.FileUploadBizEnum;
import com.yuxian.yupao.service.UserService;
import java.io.File;
import java.util.Arrays;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *

 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param biz
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestBody MultipartFile multipartFile,
                                           @RequestParam String biz, HttpServletRequest request) {
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        validFile(multipartFile, fileUploadBizEnum);
        User loginUser = userService.getLoginUser(request);
        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        File file = null;
        try {
            // 上传文件
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            String result = FileConstant.COS_HOST + filepath;
            if ("user_avatar".equals(fileUploadBizEnum.getValue())) {
                //保存信息
                User updateUser = new User();
                updateUser.setId(loginUser.getId());
                updateUser.setUserAvatar(result);
                if (!userService.updateById(updateUser)) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
                }
            }
            // 返回可访问地址
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            if (file != null) {
                // 删除临时文件
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
