package com.evan.greennote.user.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//根据邮箱号查询用户信息
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindUserByEmailRspDTO {
    private Long id;
    private String password;
}
