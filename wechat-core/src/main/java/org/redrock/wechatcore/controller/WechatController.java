package org.redrock.wechatcore.controller;

import org.redrock.wechatcore.bean.Token;
import org.redrock.wechatcore.bean.UserInfo;
import org.redrock.wechatcore.exception.WechatException;
import org.redrock.wechatcore.repository.StringRepository;
import org.redrock.wechatcore.repository.WechatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class WechatController {


    /**
     * 获取微信权限accessToken
     * @return
     */
    @GetMapping("/access_token")
    public ResponseEntity<String> getAccessToken() {
        String accessToken = wechatRepository.getAccessToken();
        if (stringRepository.isBlank(accessToken)) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(accessToken, HttpStatus.OK);
    }

    /**
     * 通过 code 获取 oauth token 以及 内部调用凭证 jwt
     * @param code
     * @return
     * @throws WechatException
     */
    @GetMapping("/tokenWithJwt/{code}")
    public ResponseEntity<Map> getTokenWithJwt(@PathVariable("code") Optional<String> code) throws WechatException {
        if (!code.isPresent()) throw new WechatException(HttpStatus.BAD_REQUEST, "code 参数不可为空");
        Token userToken = wechatRepository.getUserAccessToken(code.get());
        UserInfo userInfo = wechatRepository.getUserInfo(userToken.getOpenid());
        String jwt = wechatRepository.createJwt(userInfo);
        Map<String, String> tokenWithJwt = new HashMap<>();
        tokenWithJwt.put("access_token", userToken.getAccessToken());
        tokenWithJwt.put("refresh_token", userToken.getRefreshToken());
        tokenWithJwt.put("expire_in", userToken.getExpiresIn() + "");
        tokenWithJwt.put("jwt", jwt);
        return new ResponseEntity<>(tokenWithJwt, HttpStatus.OK);
    }

    /**
     * code 获取 测试接口
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("/test")
    public void test(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!stringRepository.isBlank(request.getParameter("code"))) {
            response.getWriter().println(request.getParameter("code"));
            return;
        }
        String apiUrl = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_base&state=STATE#wechat_redirect";
        String api = String.format(apiUrl, "wxdab44034ceb528e8", "http://jiangtianixng.s1.natapp.link/core/test");
        response.sendRedirect(api);
        return;
    }

    @GetMapping("/redis")
    public String redis() {
        redisTemplate.opsForValue().set("hello", "world");
        return (String) redisTemplate.opsForValue().get("hello");
    }
    @Autowired
    WechatRepository wechatRepository;
    @Autowired
    StringRepository stringRepository;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    StringRedisTemplate stringRedisTemplate;
}