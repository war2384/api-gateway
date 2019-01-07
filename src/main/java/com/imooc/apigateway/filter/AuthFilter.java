package com.imooc.apigateway.filter;

import com.imooc.apigateway.constant.RedisConstant;
import com.imooc.apigateway.util.CookieUtil;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by weiyuhang on 2019/1/7.
 */
public class AuthFilter extends ZuulFilter{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        /**
         * /order/create 只能买家访问(cookie里有openid)
         * /order/finish 只能卖家访问(cookie里有token, 并且对应的redis中值)
         * product/list 都可访
         */
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();
        if("/order/create".equals(request.getRequestURI())){
            Cookie cookie = CookieUtil.get(request,"openid");
            if(cookie == null || StringUtils.isEmpty(cookie.getValue())){
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        if("/order/finish".equals(request.getRequestURI())){
            Cookie cookie = CookieUtil.get(request,"token");
            if(cookie == null || StringUtils.isEmpty(cookie.getValue()) ||
                    StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(String.format(RedisConstant.TOKEN_TEMPLATE,cookie.getVersion())))){
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.SC_UNAUTHORIZED);
            }
        }
        return null;
    }
}
