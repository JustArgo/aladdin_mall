package com.maiquan.aladdin_mall.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 会员
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/member")
public class MemberController {
	/**
	 * 会员个人中心
	 */
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index() {
		return "member/index";
	}

	/**
	 * 我的财富
	 */
	@RequestMapping(value = "/wealth", method = RequestMethod.GET)
	public String wealth() {
		return "member/wealth";
	}
	/**
	 * 
	 * @return
	 */
	@RequestMapping(value = "/popularize", method = RequestMethod.GET)
	public String popularize(){
		return "member/popularize";
	}
}
