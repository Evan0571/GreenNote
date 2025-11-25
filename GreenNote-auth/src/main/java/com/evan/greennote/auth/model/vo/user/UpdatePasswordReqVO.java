package com.evan.greennote.auth.model.vo.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//用户修改密码
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatePasswordReqVO {
    @NotBlank(message="新密码不能为空")
    private String newPassword;
}
