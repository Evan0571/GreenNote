package com.evan.greennote.user.biz.util;

import java.util.regex.Pattern;

public class ParamUtils {
    private ParamUtils(){
    }

    //校验昵称
    //定义昵称长度范围
    private static final int NICK_NAME_MIN_LENGTH = 2;
    private static final int NICK_NAME_MAX_LENGTH = 24;

    //定义特殊字符正则表达式
    private static final String NICK_NAME_REGEX = "[`~!@#$%^&*()+=|{}':;'\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";

    //校验昵称
    public static boolean checkNickName(String nickName){
        //检查长度
        if(nickName.length()<NICK_NAME_MIN_LENGTH||nickName.length()>NICK_NAME_MAX_LENGTH){
            return false;
        }

        //检查特殊字符
        Pattern pattern= Pattern.compile(NICK_NAME_REGEX);
        return !pattern.matcher(nickName).find();
    }

    //校验小绿书号id
    //定义ID长度范围
    private static final int ID_MIN_LENGTH = 6;
    private static final int ID_MAX_LENGTH = 15;

    //定义正则表达式
    private static final String ID_REGEX = "^[a-zA-Z0-9]+$";

    //校验id
    public static boolean checkGreenNoteId(String greennoteId){
        //检查长度
        if(greennoteId.length()<ID_MIN_LENGTH||greennoteId.length()>ID_MAX_LENGTH){
            return false;
        }
        //检查格式
        Pattern pattern= Pattern.compile(ID_REGEX);
        return pattern.matcher(greennoteId).matches();
    }

    //字符串长度校验
    public static boolean checkLength(String str,int length){
        //检查长度
        if(str.isEmpty()||str.length()>length){
            return false;
        }
        return true;
    }
}
