package com.demo.Controller;

import com.demo.action.RightVerifyAction;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by shuoshuo on 2017/9/8.
 */
@RightVerifyAction(menuId = "5502")
@Controller
@RequestMapping("/read")
public class ReadController {
}
