package org.wei.usercenterweb.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 *
 * @author WeiWei
 * @version V5.0.0
 * @date 2025/3/5
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 8575799328933029326L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String userId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账户
     */
    private String userCount;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 邮箱
     */
    private String userEmail;

    /**
     * 性别 (0-男，1-女)
     */
    private Integer sex;

    /**
     * 手机号
     */
    private String userPhone;

    /**
     * 用户头像
     */
    private String imageUrl;

    /**
     * 账户状态(0-正常，1-封禁)
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 是否为管理员（0-普通用户，1-管理员）
     */
    private Integer isAdmin;

    /**
     * 删除状态(0-未删除，1-删除)
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;
}