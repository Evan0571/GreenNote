package com.evan.greennote.auth.model.vo.verificationcode;

import com.evan.framework.common.validator.EmailAddress;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendVerificationCodeReqVO {
    @NotBlank(message="邮箱不能为空")
    @EmailAddress
    private String email;
}
