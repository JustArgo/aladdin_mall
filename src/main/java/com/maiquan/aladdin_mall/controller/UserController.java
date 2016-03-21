package com.maiquan.aladdin_mall.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.account.service.AccountService;
import com.aladdin.user.entity.BaseCollect;
import com.aladdin.user.service.UserService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_product.service.IProductCollectService;

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
	private UserService userService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private IProductCollectService productCollectService;

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

	/**
	 * 申请提现
	 * 
	 * @param money
	 *            提现金额（分）
	 * @return
	 */
	@RequestMapping(value = "/applyWithDraw", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> applyWithDraw(Integer money) {
		return accountService.applyWithDraw(money);
	}

	/**
	 * 我的收藏
	 * 
	 * @return
	 */
	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	public String collect(ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("product", productCollectService.getProductCollectListByMqID("1", null));
		return "user/collect";
	}
}
