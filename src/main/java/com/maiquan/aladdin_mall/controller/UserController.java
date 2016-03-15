package com.maiquan.aladdin_mall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 用户
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
	/**
	 * 个人中心
	 */
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() {
		return "user/index";
	}

	/**
	 * 我的财富
	 */
	@RequestMapping(value = "/wealth", method = RequestMethod.GET)
	public String wealth() {
		return "user/wealth";
	}
	/**
	 * 我要推广
	 * @return
	 */
	@RequestMapping(value = "/generalize", method = RequestMethod.GET)
	public String generalize(){
		return "user/generalize";
	}
}
