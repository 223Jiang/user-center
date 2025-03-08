package org.wei.usercenterweb.domain.request;

import lombok.Data;

/**
 * user-center-web
 *
 * @author WeiWei
 * @version V5.0.0
 * @date 2025/3/7
 */
@Data
public class SearchUsersRequest {
    private Integer current = 1;

    private Integer pageSize = 10;

    private Integer userStatus;

    private String userName;

    private String isAdmin;

    private String startTime;

    private String endTime;
}
