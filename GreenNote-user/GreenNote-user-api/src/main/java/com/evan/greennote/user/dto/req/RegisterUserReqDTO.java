package com.evan.greennote.user.dto.req;

import com.evan.framework.common.validator.EmailAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//用户注册
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterUserReqDTO {
    //邮箱号
    @NotBlank(message = "邮箱号不能为空")
    @EmailAddress
    private String email;
}
