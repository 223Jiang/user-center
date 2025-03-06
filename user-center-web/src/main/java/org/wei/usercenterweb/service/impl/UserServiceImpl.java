package org.wei.usercenterweb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Service;
import org.wei.usercenterweb.common.StatusCodeEnum;
import org.wei.usercenterweb.domain.User;
import org.wei.usercenterweb.domain.response.UserInformation;
import org.wei.usercenterweb.exception.CustomRuntimeExceptions;
import org.wei.usercenterweb.mapper.UserMapper;
import org.wei.usercenterweb.service.UserService;
import org.wei.usercenterweb.utile.BCryptEncryption;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static org.wei.usercenterweb.contains.UserConstants.USER_INFORMATION;
import static org.wei.usercenterweb.utile.BCryptEncryption.verifyPassword;

/**
* @author WeiWei
* @version V5.0.0
* @date 2025/3/5
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Override
    public boolean userRegister(String userCount, String userPassword, String userCheckPassword) {
        // 用户密码加密
        String encryptPassword = BCryptEncryption.encryptPassword(userPassword);
        User user = new User();
        user.setUserCount(userCount);
        user.setUserPassword(encryptPassword);

        // 判断账号是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserCount, userCount);
        long count = this.count(wrapper);

        if (count > 0) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "账号已存在！");
        }

        return this.save(user);
    }

    @Override
    public UserInformation userLogin(String userCount, String userPassword, HttpServletRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserCount, userCount);
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "用户不存在！");
        }

        if (!verifyPassword(userPassword, user.getUserPassword())) {
            throw new CustomRuntimeExceptions(StatusCodeEnum.BAD_REQUEST, "密码错误！");
        }

        // 生成脱敏后的数据
        UserInformation userInformation = new UserInformation();
        BeanUtils.copyProperties(user, userInformation);

        // 在session中存储用户信息
        request.getSession().setAttribute(USER_INFORMATION, userInformation);

        return userInformation;
    }

    @Override
    public List<UserInformation> searchUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsAdmin, 0);
        List<User> list = this.list(wrapper);

        // 进行数据的脱敏操作
        return list.stream().map(e -> {
            UserInformation userInformation = new UserInformation();
            BeanUtils.copyProperties(e, userInformation);
            return userInformation;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean deleteUser(String id) {
        // 判断用户是否是管理员，管理员不能删除
        User user = this.getById(id);
        if (user.getIsAdmin() == 1) {
            return false;
        }

        return this.removeById(id);
    }
}




