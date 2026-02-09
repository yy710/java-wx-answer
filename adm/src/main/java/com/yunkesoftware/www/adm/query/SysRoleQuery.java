package com.yunkesoftware.www.adm.query;

import com.yunkesoftware.www.query.PageCurrency;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SysRoleQuery extends PageCurrency {
    private String name;

    private String userId;
}
