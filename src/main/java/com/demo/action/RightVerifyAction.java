package com.demo.action;

import javax.xml.ws.Action;
import java.util.List;

/**
 * Created by shuoshuo on 2017/9/7.
 */

public @interface RightVerifyAction {
    String menuId();
    String[] menuIdList() default {};
}
