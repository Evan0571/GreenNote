package com.evan.greennote.user.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserInfoReqVO {
    private MultipartFile avatar; //头像
    private String nickname; //昵称
    private String greennoteId; //小绿书id
    private Integer sex; //性别
    private LocalDate birthday; //生日
    private String introduction; //个人简介
    private MultipartFile backgroundImg; //背景图
}
