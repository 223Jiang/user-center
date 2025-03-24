package org.wei.usercenterweb.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.wei.usercenterweb.common.ResponseResult;
import org.wei.usercenterweb.common.StatusCodeEnum;
import org.wei.usercenterweb.domain.User;
import org.wei.usercenterweb.domain.request.UserRegisterRequest;
import org.wei.usercenterweb.domain.request.SearchUsersRequest;
import org.wei.usercenterweb.domain.response.UserInformation;
import org.wei.usercenterweb.exception.CustomRuntimeExceptions;
import org.wei.usercenterweb.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.wei.usercenterweb.contains.UserConstants.USER_INFORMATION;
import static org.wei.usercenterweb.utile.AccountValidatorUtile.validateAccount;
import static org.wei.usercenterweb.utile.AccountValidatorUtile.validateAccountAndPassword;

/**
 * 用户接口
 *
 * @author WeiWei
 * @version V5.0.0
 * @date 2025/3/2
 */
@RequestMapping("/user")
@RestController
@Slf4j
@Api(tags = "用户管理")
public class UserController {

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.baseUrl}")
    private String baseUrl;

    private final UserService userService;

    private final MinioClient minioClient;

    public UserController(UserService userService, MinioClient minioClient) {
        this.userService = userService;
        this.minioClient = minioClient;
    }

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public ResponseResult<String> userRegister(@RequestBody UserRegisterRequest request) {
        String userCount = request.getUserCount();
        String userPassword = request.getUserPassword();
        String userCheckPassword = request.getUserCheckPassword();

        if (StringUtils.isAnyEmpty(userCount, userPassword, userCheckPassword)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "存在为空的数据");
        }

        if (!userPassword.equals(userCheckPassword)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "两次密码不一致！");
        }

        if (!validateAccount(userCount)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "账号格式不正确！长度在 6 到 20 个字符之间，只能包含字母和数字");
        }

        if (!validateAccountAndPassword(userPassword)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "密码格式不正确！长度在 8 到 20 个字符之间，必须包含至少一个大写字母、一个小写字母和一个数字");
        }

        boolean bool = userService.userRegister(userCount, userPassword, userCheckPassword);
        if (bool) {
            return ResponseResult.success("用户注册成功");
        } else {
            return ResponseResult.fail("用户注册失败！");
        }
    }

    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public ResponseResult<UserInformation> userLogin(@RequestBody UserRegisterRequest userRegister, HttpServletRequest request) {
        String userCount = userRegister.getUserCount();
        String userPassword = userRegister.getUserPassword();

        if (StringUtils.isAnyEmpty(userCount, userPassword)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "存在为空的数据");
        }

        if (!validateAccount(userCount)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "账号格式不正确！");
        }

        if (!validateAccountAndPassword(userPassword)) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "密码格式不正确！");
        }

        UserInformation userInformation = userService.userLogin(userCount, userPassword, request);
        if (userInformation == null) {
            return ResponseResult.fail("用户登录失败！");
        }
        return ResponseResult.success(userInformation);
    }

    @ApiOperation(value = "用户注销")
    @PostMapping("/logout")
    public ResponseResult<String> userLogout(HttpServletRequest request) {
        Object user = request.getSession().getAttribute(USER_INFORMATION);
        if (user == null) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.UNAUTHORIZED, "用户未登录！");
        }
        request.getSession().removeAttribute(USER_INFORMATION);
        return ResponseResult.success("注销成功");
    }

    @ApiOperation(value = "获取登录用户信息")
    @GetMapping("/currentUser")
    public ResponseResult<UserInformation> userRegister(HttpServletRequest request) {
        return ResponseResult.success(permissionVerification(request));
    }

    @ApiOperation(value = "修改头像")
    @PostMapping("/uploadAvatar")
    public ResponseResult<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        UserInformation userInformation = permissionVerification(request);

        String fileName = null;
        try (InputStream inputStream = file.getInputStream()) {
            fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            String url = baseUrl + "/" + String.format("%s/objects/download?preview=true&prefix=%s",
                    bucket, fileName);

            userInformation.setImageUrl(url);
            User user = new User();
            BeanUtils.copyProperties(userInformation, user);
            userService.updateById(user);

            return ResponseResult.success(url);
        } catch (Exception e) {
            log.error("头像上传失败: {}", fileName, e);
            return ResponseResult.fail("上传失败！");
        }
    }

    @ApiOperation(value = "更新用户信息")
    @PostMapping("/updateUserProfile")
    public ResponseResult<String> updateUserProfile(@RequestBody UserInformation userInformation, HttpServletRequest request) {
        permissionVerification(request);

        User user = new User();
        BeanUtils.copyProperties(userInformation, user);
        boolean bool = userService.updateById(user);
        return ResponseResult.success(bool ? "更新成功" : "更新失败");
    }

    @ApiOperation(value = "查询用户列表数据")
    @PostMapping("/searchUsers")
    public ResponseResult<Page<UserInformation>> searchUsers(
            @RequestBody SearchUsersRequest request,
            HttpServletRequest servletRequest) {
        // 判断用户是否登录
        isAdmin(servletRequest);

        return ResponseResult.success(userService.searchUsers(request));
    }

    @ApiOperation(value = "删除非管理员用户")
    @PostMapping("/deleteUser/{id}")
    public ResponseResult<String> deleteUser(HttpServletRequest request, @PathVariable String id) {
        // 判断用户是否登录
        isAdmin(request);

        boolean bool = userService.deleteUser(id);

        if (!bool) {
            return ResponseResult.fail("删除失败！");
        } else {
            return ResponseResult.success("删除成功");
        }
    }

    @ApiOperation(value = "禁用/启用用户")
    @PostMapping("/updateUserStatus")
    public ResponseResult<String> updateUserStatus(
            HttpServletRequest request,
            @RequestBody UserInformation userInformation
    ) {
        // 判断用户是否登录
        isAdmin(request);

        User user = new User();
        user.setUserId(userInformation.getUserId());
        user.setUserStatus(userInformation.getUserStatus());
        boolean bool = userService.updateById(user);

        if (!bool) {
            return ResponseResult.fail("删除失败！");
        } else {
            return ResponseResult.success("删除成功");
        }
    }

    /**
     * 权限校验
     * @param request   请求体
     */
    private UserInformation permissionVerification(HttpServletRequest request) {
        UserInformation user = (UserInformation) request.getSession().getAttribute(USER_INFORMATION);
        if (user == null) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.UNAUTHORIZED, "用户未登录！");
        }
        // 跟新为最新的数据
        User searchUser = userService.getById(user.getUserId());
        BeanUtils.copyProperties(searchUser, user);
        request.getSession().setAttribute(USER_INFORMATION, user);

        return user;
    }

    private void isAdmin(HttpServletRequest request) {
        UserInformation user = permissionVerification(request);

        // 判断是否为管理员
        if (user.getIsAdmin() == 0) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.METHOD_NOT_ALLOWED, "用户权限不足！");
        }
    }
}
