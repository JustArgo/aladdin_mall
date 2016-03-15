package com.maiquan.aladdin_mall.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aladdin.account.service.AccountService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;

/**
 * 用户
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private AccountService accountService;

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
	public String wealth(ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("accountInfo", accountService.getAccountInfo(null, "1"));
		return "user/wealth";
	}

	/**
	 * 我要推广
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generalize", method = RequestMethod.GET)
	public String generalize() {
		return "user/generalize";
	}

	/**
	 * 我要提现
	 * 
	 * @return
	 */
	@RequestMapping(value = "/withDraw", method = RequestMethod.GET)
	public String withDraw() {
		return "user/withDraw";
	}
}
