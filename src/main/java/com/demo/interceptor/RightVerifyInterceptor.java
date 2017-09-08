package com.demo.interceptor;

import com.alibaba.fastjson.JSON;
import com.demo.action.RightVerifyAction;
import com.demo.cache.RightVerifyCache;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by shuoshuo on 2017/9/6.
 */
public class RightVerifyInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RightVerifyInterceptor.class);

    private static final String COOKIE_KEY = "Admin";
    private static final long EXPIREDTIME = 600000L;
    private List<String> menuList;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            logger.info("不需要权限校验");
            return true;
        }

        //通过调用的controller找到注解中的rightverify
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Class<?> clazz = handlerMethod.getBeanType();
        RightVerifyAction rightVerifyAction = clazz.getAnnotation(RightVerifyAction.class);

        //对于不需要权限校验的页面直接通过
        if(rightVerifyAction == null) {
            logger.info(clazz + "此controller {} 不需要权限校验");
            return true;
        }

        logger.info("权限校验开始");
        String menuId = rightVerifyAction.menuId();
        String[] menuIds = rightVerifyAction.menuIdList();

        //没有获取到权限菜单,跳转到登录页
        if(menuId == null || menuId.length() == 0 || menuIds.length == 0) {
            logger.error("没有配置权限菜单");
            //跳转到登录页面
//            redirectLogin(request, response);
            return false;
        }

        //将所有声明的menuId都放入到list中
        initMenuIdList(menuId, menuIds);

        //判断是否登录成功
        Pair<Boolean, Map> retValid = isLoginSucc(request, response);
        if (retValid.getKey() == false) {
            //跳转到登录页面
//            redirectLogin(request, response);
            return false;
        }

        //校验权限
        if(!isValid(retValid.getValue())) {
            logger.warn("权限校验失败");
            return false;
        }
        logger.info("权限校验成功");
        return true;
    }

    /**
     * 初始化菜单
     * @param menuId
     * @param menuIds
     */
    private void initMenuIdList(String menuId, String[] menuIds) {
        if(menuId != null) {
            menuList.add(menuId);
        }

        for (String menu : menuIds) {
            menuList.add(menu);
        }
    }

    /**
     * 校验用户是否具备权限
     * @param staffMap
     * @return
     */
    private boolean isValid(Map staffMap) {
        if (staffMap == null || staffMap.isEmpty()) {
            return false;
        }
        //从缓存或数据库中取出该用户的权限
        List<Map> rights = RightVerifyCache.queryRightList((String) staffMap.get("STAFF_ID"));
        return true;
    }

    /**
     * 判断是否登录成功
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    private Pair<Boolean, Map> isLoginSucc(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cookie[] cookies = request.getCookies();
        Cookie staffCookie = null;

        //获取用户登录信息
        if (cookies == null || cookies.length == 0) {
            logger.error("用户未登录");
            return new Pair<Boolean, Map>(false, null);
        }

        for(Cookie cookie : cookies) {
            if(COOKIE_KEY.equals(cookie.getName())) {
                staffCookie = cookie;
                break;
            }
        }

        if(staffCookie == null) {
            logger.error("用户未登录");
            return new Pair<Boolean, Map>(false, null);
        }

        String cookieValue = staffCookie.getValue();
        if(cookieValue == null) {
            logger.info("Cookie值为空");
            return new Pair<Boolean, Map>(false, null);
        }

        //判定登录是否超时
        Map staffMap = (Map) JSON.parse(cookieValue);
        if(staffMap.get("CURRENT_TIME") != null) {
            long loginTime = Long.parseLong((String)staffMap.get("CURRENT_TIME"));

            if(Calendar.getInstance().getTimeInMillis() - loginTime > EXPIREDTIME) {
                logger.error("登录超时");
                return new Pair<Boolean, Map>(false, null);
            }
        }

        return new Pair<Boolean, Map>(true, staffMap);
    }
}
