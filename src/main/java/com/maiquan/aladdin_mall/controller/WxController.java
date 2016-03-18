package com.maiquan.aladdin_mall.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import me.chanjar.weixin.mp.bean.result.WxMpUser;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.aladdin.user.service.UserService;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.JSONObject;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.DecryptUtil;
import com.maiquan.aladdin_mall.util.WebUtil;

/**
 * 微信验证接口
 * 
 * @author JSC
 *
 */
@Controller
@RequestMapping("/wx")
public class WxController {
	@Autowired
	private WxInteractionService wxInteractionService;
	@Autowired
	private UserService userService;

	/**
	 * 验证服务器地址的有效性
	 * 
	 * @param signature
	 *            微信加密签名，signature结合了开发者填写的token参数和请求中的timestamp参数、nonce参数。
	 * @param timestamp
	 *            时间戳
	 * @param nonce
	 *            随机数
	 * @param echostr
	 *            随机字符串
	 * @return 有效则返回echostr
	 */
	@RequestMapping(value = "/checkSignature", method = RequestMethod.GET)
	@ResponseBody
	public String checkSignature(String signature, String timestamp, String nonce, String echostr) {
		if (wxInteractionService.checkSignature(timestamp, nonce, signature)) {
			return echostr;
		}
		return "invalid request";
	}

	/**
	 * 微信验证回调接口
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/callback", method = RequestMethod.GET)
	@ResponseBody
	public void login(HttpServletResponse response, String code, String state) throws Exception {
		System.out.println("code "+code);
		
		WxMpUser wxMpUser = wxInteractionService.getSnsapiBaseUserInfo(code);
		String openId = getOpenID(code);//wxMpUser.getOpenId();
		System.out.println("get the openId:" + openId);
		Principal principal = new Principal(null, openId);
		WebUtil.login(principal);
		response.sendRedirect(String.valueOf(WebUtil.getSession().getAttribute(WebUtil.SAVE_REQUEST_KEY)));
	}
	
	/**
	 * 统一下单
	 */
	@RequestMapping("/unifiedorder")
	@ResponseBody
	public String unifiedOrder(Long totalFee){
		
		String appid			= null;
		String attach			= null;
		String body				= null;
		String detail 			= null;
		String device_info 		= null;
		String fee_type			= null; 
		String goods_tag		= null;
		String limit_pay		= null;
		String mch_id			= null;
		String nonce_str		= null;
		String notify_url		= null;
		String openid			= null;
		String out_trade_no		= null;
		String product_id		= null;
		String spbill_create_ip	= null;
		Date   time_expire		= null;
		Date   time_start		= null;
		String total_fee		= null;
		String trade_type		= null;
		String sign				= null;
		
		//Principal principal = WebUtil.getCurrentPrincipal();
System.out.println("totalFee---"+totalFee);		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String prepay_id = "";
		
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost("https://api.mch.weixin.qq.com/pay/unifiedorder");
		
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("wx.properties");
		Properties p = new Properties();
		try {
			p.load(is);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		appid=p.getProperty("appid");
		openid=WebUtil.getCurrentPrincipal().getOpenId();
		attach="js";
		body="sdfdf";
		mch_id=p.getProperty("mch_id");
		nonce_str=UUID.randomUUID().toString().replaceAll("-", "");
		notify_url=p.getProperty("notify_url");
		out_trade_no=new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		spbill_create_ip=p.getProperty("spbill_create_ip");
		total_fee=totalFee.toString();
		trade_type="JSAPI";
		
		String mch_key = p.getProperty("mch_key");
		
System.out.println("appid---"+appid);
System.out.println("openid---"+openid);
System.out.println("mch_id---"+mch_id);
System.out.println("notify_url---"+notify_url);
System.out.println("spbill_create_ip---"+spbill_create_ip);
		
		time_start = new Date();
		time_expire = new Date(time_start.getTime()+15*60*1000);
		
		String beforeSigned = prepareSign(appid, attach, body, detail, device_info, fee_type, goods_tag, limit_pay, mch_id, nonce_str, notify_url, openid, out_trade_no, product_id, spbill_create_ip, time_expire, time_start, total_fee, trade_type);
		
		beforeSigned = beforeSigned+"&key="+mch_key;
		
		sign = DecryptUtil.MD5(beforeSigned).toUpperCase();
		
		String xmlParam = prepareParam(appid, attach, body, detail, device_info, fee_type, goods_tag, limit_pay, mch_id, nonce_str, notify_url, openid, out_trade_no, product_id, spbill_create_ip, time_expire, time_start, total_fee, trade_type, sign);
		
		try {
			StringEntity entity = new StringEntity(xmlParam);
			entity.setContentType("application/xml");
			post.setEntity(entity);
			post.setHeader("Content-Type", "application/xml");
			
			HttpResponse resp = client.execute(post);
System.out.println("respcode----"+resp.getStatusLine().getStatusCode());
			BufferedReader br = new BufferedReader(new InputStreamReader(resp.getEntity().getContent()));
			String line = "";
			StringBuffer sb = new StringBuffer();
			while((line=br.readLine())!=null){
				sb.append(line+"\n");
			}
System.out.println("sb---"+sb);
			prepay_id = getPrepayID(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return prepay_id;
	}
	
	/**
	 * 获得signature 作为前台wx.config
	 * @return
	 */
	@RequestMapping("/getConfig")
	@ResponseBody
	public Map<String,String> getConfig(){
		
		HttpClient client = new DefaultHttpClient();
		InputStream is = WxController.class.getClassLoader().getResourceAsStream("wx.properties");
		Properties p = new Properties();
		String accessToken = "";
		String ticket = "";
		String signature = "";
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String appid = p.getProperty("appid");
		String secret = p.getProperty("secret");
		String url   = p.getProperty("url");
		Map<String,String> retMap = new HashMap<String,String>();
		
		HttpGet get = new HttpGet("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret);
		JSONObject retObj = null;
		try {
			HttpResponse resp = client.execute(get);
			if(resp.getStatusLine().getStatusCode()==200){
				retObj = (JSONObject)JSON.parse(new InputStreamReader(resp.getEntity().getContent()));
				if(retObj.contains("access_token")){
					accessToken = retObj.getString("access_token");
					HttpGet getTicket = new HttpGet("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi");
					resp = client.execute(getTicket);
					if(resp.getStatusLine().getStatusCode()==200){
						retObj = (JSONObject)JSON.parse(new InputStreamReader(resp.getEntity().getContent()));
						if(retObj.getInt("errcode", 40000)==0){
							ticket = retObj.getString("ticket");
							Long timestamp = System.currentTimeMillis()/1000L;
							String noncestr = UUID.randomUUID().toString().replaceAll("-", "");
							String signStr = "jsapi_ticket="+ticket+"&noncestr="+noncestr+"&timestamp="+timestamp+"&url="+url;
							signature = DecryptUtil.SHA1(signStr);
							retMap.put("appId", appid);
							retMap.put("timestamp", timestamp.toString());
							retMap.put("nonceStr", noncestr);
							retMap.put("signature", signature);
						}
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		
		
		return retMap;
	}
	
	/**
	 * 统一下单通知
	 * @param retXml
	 * @return
	 */
	@RequestMapping("/unifiedorder_notify")
	public String unifiedorder_notify(String retXml){
		System.out.println("unifiedorder_notify");
		System.out.println("retXml----"+retXml);
		return "";
	}
	
	/**
	 * 发送统一订单请求 返回prepay_id
	 * @param retXml
	 * @return
	 */
	private String getPrepayID(String retXml){
		
		String prepay_id = "";
		if(retXml.indexOf("<?")<0){
			retXml = "<?xml version='1.0' encoding='UTF-8'?>" + retXml;
		}
		try {
			Document doc = DocumentHelper.parseText(retXml);
			Element root = doc.getRootElement();
			Node return_code = root.element("return_code");
			
			if(return_code.getText().equals("SUCCESS")){
				prepay_id = root.element("prepay_id").getText();
			}
			
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return prepay_id;
	}
	
	/**
	 * 准备预签名
	 * @return
	 */
	private String prepareSign(String appid, String attach,	String body,String detail, String device_info,String fee_type,String goods_tag,
			String limit_pay,String mch_id,	String nonce_str,String notify_url,String openid,String out_trade_no,String product_id,
			String spbill_create_ip,Date time_expire,Date time_start, String total_fee, String trade_type){
		
		String beforeSigned = "appid="+appid
				+ (attach!=null?"&attach="+attach:"")
				+ "&body="+body
				+ (detail!=null?"&detail="+detail:"")
				+ (device_info!=null?"&device_info="+device_info:"")
				+ (fee_type!=null?"&fee_type="+fee_type:"")
				+ (goods_tag!=null?"&goods_tag="+goods_tag:"")
				+ (limit_pay!=null?"&limit_pay="+limit_pay:"")
				+ "&mch_id="+mch_id
				+ "&nonce_str="+nonce_str
				+ "&notify_url="+notify_url
				+ "&openid="+openid
				+ "&out_trade_no="+out_trade_no
				+ (product_id!=null?"&product_id="+product_id:"")
				+ "&spbill_create_ip="+spbill_create_ip
				+ (time_expire!=null?"&time_expire="+new SimpleDateFormat("yyyyMMddHHmmss").format(time_expire):"")
				+ (time_start!=null?"&time_start="+new SimpleDateFormat("yyyyMMddHHmmss").format(time_start):"")
				+ "&total_fee="+total_fee
				+ "&trade_type="+trade_type;
		
		
		return beforeSigned;
	}
	
	/**
	 * 准备xml 发起统一下单请求
	 * @return
	 */
	private String prepareParam(String appid, String attach,	String body,String detail, String device_info,String fee_type,String goods_tag,
			String limit_pay,String mch_id,	String nonce_str,String notify_url,String openid,String out_trade_no,String product_id,
			String spbill_create_ip,Date time_expire,Date time_start, String total_fee, String trade_type, String sign){
		
		String xmlParam = "<xml>"
				+ "<appid><![CDATA[" + appid + "]]></appid>"
				+ (attach!=null?"<attach><![CDATA[" + attach +"]]></attach>":"")
				+ "<body><![CDATA[" + body + "]]></body>"
				+ (detail!=null?"<detail><![CDATA[" + detail +"]]></detail>":"")
				+ (device_info!=null?"<device_info><![CDATA[" + device_info +"]]></device_info>":"")
				+ (fee_type!=null?"<fee_type><![CDATA[" + fee_type +"]]></fee_type>":"")
				+ (goods_tag!=null?"<goods_tag><![CDATA[" + goods_tag +"]]></goods_tag>":"")
				+ (limit_pay!=null?"<limit_pay><![CDATA[" + limit_pay +"]]></limit_pay>":"")
				+ "<mch_id><![CDATA[" + mch_id + "]]></mch_id>"
				+ "<nonce_str><![CDATA[" + nonce_str + "]]></nonce_str>"
				+ "<notify_url><![CDATA[" + notify_url + "]]></notify_url>"
				+ "<openid><![CDATA[" + openid + "]]></openid>"
				+ "<out_trade_no><![CDATA[" + out_trade_no + "]]></out_trade_no>"
				+ (product_id!=null?"<product_id><![CDATA[" + product_id +"]]></product_id>":"")
				+ "<spbill_create_ip><![CDATA[" + spbill_create_ip + "]]></spbill_create_ip>"
				+ (time_expire!=null?"<time_expire><![CDATA[" + new SimpleDateFormat("yyyyMMddHHmmss").format(time_expire) + "]]></time_expire>":"")
				+ (time_start!=null?"<time_start><![CDATA[" + new SimpleDateFormat("yyyyMMddHHmmss").format(time_start) + "]]></time_start>":"")
				+ "<total_fee><![CDATA[" + total_fee + "]]></total_fee>"
				+ "<trade_type><![CDATA[" + trade_type + "]]></trade_type>"
				+ "<sign><![CDATA[" + sign + "]]></sign>"
				+ "</xml>";
		
		return xmlParam;
		
	}
	
	/**
	 * 根据code 计算openID
	 * @param code
	 * @return
	 */
	private String getOpenID(String code){
		
		
		HttpClient client = new DefaultHttpClient();
		InputStream is = WxController.class.getClassLoader().getResourceAsStream("wx.properties");
		Properties p = new Properties();
		String openid="";
		try {
			p.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String appid = p.getProperty("appid");
		String secret = p.getProperty("secret");
		
		
		HttpGet get = new HttpGet("https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appid+"&secret="+secret+"&code="+code+"&grant_type=authorization_code");
		try {
			HttpResponse resp = client.execute(get);
			JSONObject retObj = (JSONObject)JSON.parse(new InputStreamReader(resp.getEntity().getContent()));
			openid = retObj.getString("openid");
			System.out.println("getOpenID()   openID--"+openid);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return openid;
	}
	
	
}
