package com.maiquan.aladdin_mall.controller;

import java.io.OutputStream;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.account.service.AccountService;
import com.aladdin.user.service.UserService;
import com.aladdin.vertical.distribution.service.VerticalDistributionService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.QRCodeUtil;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_product.service.IProductCollectService;
import com.util.MapUtil;
import com.util.MapUtil.MapData;

/**
 * 用户
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/user")
public class UserController {
	Logger logger = Logger.getLogger(this.getClass());
	@Value("${hostName}")
	private String hostName;
	@Autowired
	private AccountService accountService;
	@Autowired
	private IProductCollectService productCollectService;
	@Autowired
	private VerticalDistributionService verticalDistributionService;

	/**
	 * 个人中心
	 */
	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(String requestId) {
		return "user/index";
	}

	/**
	 * 我的财富
	 */
	@RequestMapping(value = "/wealth", method = RequestMethod.GET)
	public String wealth(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("accountInfo", accountService.getAccountInfo(requestId, principal.getMqId()));
		return "user/wealth";
	}

	/**
	 * 余额明细
	 */
	@RequestMapping(value = "/wealthDetail", method = RequestMethod.GET)
	public String wealthDetail(String requestId, ModelMap modelMap, Integer tabIdx) {
		modelMap.put("tabIdx", tabIdx == null ? 0 : tabIdx);
		return "user/wealthDetail";
	}

	/**
	 * 查询余额明细
	 */
	@RequestMapping(value = "/wealthDetail/query", method = RequestMethod.POST)
	@ResponseBody
	public Object wealthDetailQuery(String requestId, ModelMap modelMap, String accountType, int page, int pageSize) {
		// Principal principal = WebUtil.getCurrentPrincipal();
		MapData data = MapUtil.newInstance(accountService.getAccountDetail(requestId, "1", accountType, page, pageSize));
		logger.info(data.errorString());
		return data.getObject("result");
	}

	/**
	 * 我要推广
	 * 
	 * @return
	 */
	@RequestMapping(value = "/generalize", method = RequestMethod.GET)
	public String generalize(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		modelMap.addAttribute("mqId", principal.getMqId());
		return "user/generalize";
	}

	@RequestMapping(value = "/clear", method = RequestMethod.GET)
	public void clear(String d) throws Exception {
		WebUtil.getSession().setAttribute(Principal.ATTRIBUTE_KEY, null);
	}

	@RequestMapping(value = "/qrCode", method = RequestMethod.GET)
	public void qrCode(String requestId, HttpServletResponse response, ModelMap modelMap) throws Exception {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		response.setContentType("image/jpeg");
		OutputStream os = response.getOutputStream();
		String content = "http://" + hostName + "?invite=" + mqId;
		QRCodeUtil.encode(os, content);
		os.flush();
		os.close();
	}

	/**
	 * 我要提现
	 * 
	 * @return
	 */
	@RequestMapping(value = "/withDraw", method = RequestMethod.GET)
	public String withDraw(String requestId, ModelMap modelMap) {
		Principal principal = WebUtil.getCurrentPrincipal();
		// modelMap.addAttribute("accountInfo",
		// accountService.getRemainingSum(principal.getMqId(), "1"));
		modelMap.addAttribute("remainingSum", accountService.getRemainingSum(UUID.randomUUID().toString().replaceAll("-", ""), "1"));
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
	public Map<String, Object> applyWithDraw(String requestId, Integer money) {
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
	public String collect(String requestId, ModelMap modelMap) {
		modelMap.addAttribute("product", productCollectService.getProductCollectListByMqID("1", requestId));
		return "user/collect";
	}

	/**
	 * 我的销售
	 * 
	 * @return
	 */
	@RequestMapping(value = "/sales", method = RequestMethod.GET)
	public String sales(String requestId, ModelMap modelMap, int page, int pageSize) {
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqId = principal.getMqId();
		modelMap.addAttribute("sales", verticalDistributionService.findSales(requestId, mqId, page, pageSize));
		modelMap.addAttribute("memberSales", verticalDistributionService.findMemberSales(requestId, mqId, page, pageSize));
		return "user/sales";
	}

	/**
	 * 我的团队
	 * 
	 * @return
	 */
	@RequestMapping(value = "/team", method = RequestMethod.GET)
	public String team(String requestId, ModelMap modelMap, Integer levelIdx, Integer ddd) {
		MapData data = MapUtil.newInstance(verticalDistributionService.findMemberCount(requestId, "1"));
		logger.info(data.errorString());
		modelMap.put("levelIdx", levelIdx == null ? 0 : levelIdx);
		modelMap.put("counts", data.getObject("result"));
		return "user/team";
	}

	/**
	 * 我的团队成员
	 * 
	 * @return
	 */
	@RequestMapping(value = "/teamMember", method = RequestMethod.POST)
	@ResponseBody
	public Object teamMember(String requestId, ModelMap modelMap, int levelNum, int page, int pageSize) {
		MapData data = MapUtil.newInstance(verticalDistributionService.findMemberByLevelNum(requestId, "1", levelNum, page, pageSize));
		logger.info(data.errorString());
		return data.getObject("result");
	}
}
