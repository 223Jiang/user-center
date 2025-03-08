package org.wei.usercenterweb.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.stereotype.Service;
import org.wei.usercenterweb.domain.User;
import org.wei.usercenterweb.domain.request.SearchUsersRequest;
import org.wei.usercenterweb.domain.response.UserInformation;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author WeiWei
 * @version V5.0.0
 * @date 2025/3/5
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userCount         账号
     * @param userPassword      密码
     * @param userCheckPassword 校验密码
     * @return                  注册状态
     */
    boolean userRegister(String userCount, String userPassword, String userCheckPassword);

    /**
     * 用户登录
     *
     * @param userCount     账号
     * @param userPassword  密码
     * @param request       请求
     * @return              用户信息（脱密）
     */
    UserInformation userLogin(String userCount, String userPassword, HttpServletRequest request);

    /**
     * 查询用户列表
     *
     * @return 非管理员用户列表
     */
    Page<UserInformation> searchUsers(SearchUsersRequest request);

    /**
     * 进行用户的删除，不能删除管理员用户
     *
     * @param id    用户id
     * @return      删除状态，true为删除成功，false为删除失败
     */
    boolean deleteUser(String id);
}
