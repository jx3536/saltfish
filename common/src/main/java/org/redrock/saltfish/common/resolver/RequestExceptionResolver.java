package org.redrock.saltfish.common.resolver;

import com.google.gson.Gson;
import org.redrock.saltfish.common.exception.RequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局的WechatException 消息处理器
 */
public class RequestExceptionResolver implements HandlerExceptionResolver{

    @Nullable
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, @Nullable Object handler, Exception ex) {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); //设置ContentType
        response.setCharacterEncoding("UTF-8"); //避免乱码
        response.setHeader("Cache-Control", "no-cache, must-revalidate");
        RequestException wechatException;
        try {
            Map<String, String> result = new HashMap<>();
            result.put("errmsg", ex.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            ex.printStackTrace();
            if(ex instanceof RequestException){
                wechatException = (RequestException) ex;
                ex.printStackTrace();
                result.put("errmsg", wechatException.getMsg());
                response.setStatus(wechatException.getHttpStatus().value());
            }
            Gson gson = new Gson();
            String msgJson = gson.toJson(result);
            response.getWriter().write(msgJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView();
    }

    public static void main(String[] args) {
    }
}
