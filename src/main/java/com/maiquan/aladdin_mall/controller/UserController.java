package com.maiquan.aladdin_mall.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.account.service.AccountService;
import com.aladdin.user.service.UserService;
import com.aladdin.vertical.distribution.service.DistributionService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.QRCodeUtil;
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
	@Autowired
	private DistributionService distributionService;

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
		// modelMap.addAttribute("accountInfo",
		// accountService.getAccountInfo(principal.getMqId(), "1"));
		modelMap.addAttribute("accountInfo", accountService.getAccountInfo(null, "1"));
		return "user/wealth";
	}

	/**
	 * 我要推广
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generalize", method = RequestMethod.GET)
	public String generalize(ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("mqId", principal.getMqId());
		return "user/generalize";
	}

	@RequestMapping(value = "/qrCode", method = RequestMethod.GET)
	public void qrCode(HttpServletResponse response, ModelMap modelMap) throws Exception {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		response.setContentType("image/jpeg");
		OutputStream os = response.getOutputStream();
		QRCodeUtil.encode(os, "http://aladdin.mi360.me?invitation=" + mqId);
		os.flush();
		os.close();
	}

	/**
	 * 我要提现
	 * 
	 * @return
	 */
	@RequestMapping(value = "/withDraw", method = RequestMethod.GET)
	public String withDraw(ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		// modelMap.addAttribute("accountInfo",
		// accountService.getRemainingSum(principal.getMqId(), "1"));
		modelMap.addAttribute("remainingSum",
				accountService.getRemainingSum(UUID.randomUUID().toString().replaceAll("-", ""), "1"));
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
		String requestId = UUID.randomUUID().toString().replace("-", "");
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		return accountService.applyWithDraw(requestId, money, mqId);
	}

	/**
	 * 我的收藏
	 * 
	 * @return
	 */
	@RequestMapping(value = "/collect", method = RequestMethod.GET)
	public String collect(ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("product", productCollectService.getProductCollectListByMqID("1",
				UUID.randomUUID().toString().replaceAll("-", "")));
		return "user/collect";
	}

	/**
	 * 我的销售
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sales", method = RequestMethod.GET)
	public String sales(ModelMap modelMap, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		String requestId = UUID.randomUUID().toString().replace("-", "");
		modelMap.addAttribute("sales", distributionService.findSales(requestId, mqId, page, pageSize));
		modelMap.addAttribute("memberSales", distributionService.findMemberSales(requestId, mqId, page, pageSize));
		return "user/sales";
	}
}
