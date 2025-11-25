package com.evan.greennote.auth.model.vo.user;

import com.evan.framework.common.validator.EmailAddress;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserLoginReqVO {
    @NotBlank(message = "邮箱号不能为空")
    @EmailAddress
    private String email;
    private String code;
    private String password;
    @NotNull(message = "登录类型不能为空")
    private Integer type;
}
