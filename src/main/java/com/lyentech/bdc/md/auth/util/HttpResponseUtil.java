package com.lyentech.bdc.md.auth.util;

import com.alibaba.fastjson.JSONObject;
import com.lyentech.bdc.http.response.ResultEntity;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author guolanren
 */
public class HttpResponseUtil {

    public static void setResultEntityAsContent(HttpServletResponse response, ResultEntity resultEntity) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/javascript;charset=utf-8");
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(JSONObject.toJSONString(resultEntity));
    }

}
