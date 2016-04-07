package com.maiquan.aladdin_mall.controller;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_order.domain.GoodsReturn;
import com.maiquan.aladdin_order.domain.MoneyReturn;
import com.maiquan.aladdin_order.domain.Order;
import com.maiquan.aladdin_order.domain.OrderPayment;
import com.maiquan.aladdin_order.domain.OrderProduct;
import com.maiquan.aladdin_order.service.IOrderProductService;
import com.maiquan.aladdin_order.service.IOrderService;
import com.maiquan.aladdin_product.domain.Product;
import com.maiquan.aladdin_product.domain.ProductSku;
import com.maiquan.aladdin_product.service.IPostFeeService;
import com.maiquan.aladdin_product.service.IProductService;
import com.maiquan.aladdin_product.service.IProductSkuService;
import com.maiquan.aladdin_receadd.domain.Address;
import com.maiquan.aladdin_receadd.domain.ReceiveAddress;
import com.maiquan.aladdin_receadd.service.IAddressService;
import com.maiquan.aladdin_receadd.service.IManageReceAddService;
import com.maiquan.aladdin_shopcar.domain.ShopCarProduct;
import com.maiquan.aladdin_shopcar.service.IShopCarService;
import com.maiquan.aladdin_supplier.service.ISupplierService;

@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	private WxInteractionService wxInteractionService;
	
	@Autowired
	private IShopCarService shopCarService;
	
	@Autowired
	private IProductSkuService productSkuService;
	
	@Autowired
	private IOrderService orderService;
	
	@Autowired
	private IOrderProductService orderProductService;
	
	@Autowired
	private IProductService productService;
	
	@Autowired
	private IManageReceAddService manageReceAddService;
	
	@Autowired
	private IPostFeeService postFeeService;
	
	@Autowired
	private IAddressService addressService;
	
	@Autowired
	private ISupplierService supplierService;
	
	@RequestMapping("/placeOrder")
	public Map<String,String> placeOrder(String requestId, Integer[] skuIds, Integer[] buyNums, Long[] skuPrices, Long[] supplierAmounts, Long pFee, Long pSum, String invoiceName, String notes){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");		
		
		String mqID = principal.getMqId();		
		
		for(int i=0;i<skuIds.length;i++){
			System.out.print(skuIds[i]+" ");
		}
		System.out.println("buyNums  ");
		for(int i=0;i<buyNums.length;i++){
			System.out.print(buyNums[i]+" ");
		}
		System.out.println("skuPrices  ");
		for(int i=0;i<skuPrices.length;i++){
			System.out.print(skuPrices[i]+" ");
		}
		System.out.println("supplierAmounts");
		for(int i=0;i<supplierAmounts.length;i++){
			System.out.print(supplierAmounts[i]+" ");
		}
		System.out.println("pFee  "+pFee);
		System.out.println("pSum  "+pSum);
		
		String openID = WebUtil.getCurrentPrincipal().getOpenId();
		
		if(openID==null){
			System.out.println("openID is null");
			return null;
		}
		
		//TODO 此处应该返回orderCode 供下面使用
		orderService.placeOrder(mqID, "NOR", skuIds, buyNums, skuPrices, pFee, pSum, invoiceName, notes, "");

		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("openid", openID);
		parameters.put("body", "null");
		parameters.put("out_trade_no",new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+(new Random().nextInt(900)+100));
		parameters.put("total_fee", pSum*100+"");
		
		return wxInteractionService.unifiedOrder(UUID.randomUUID().toString().replaceAll("-",""), parameters);
	}
	
	@RequestMapping("/previewOrder")
	public String previewOrder(String requestId,Model model){
		//预览订单
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");		
		
		String mqID = principal.getMqId();
		
		List<Map<String,Object>> supplierProducts = shopCarService.viewShopCar(mqID, UUID.randomUUID().toString().replaceAll("-",""));		
		
		Long totalPrice = 0L;
		double totalPostFee = 0L;
		
		for(int i=0;i<supplierProducts.size();i++){
			
			Map<String,Object> map = supplierProducts.get(i);
			List<Map<String,Object>> sameSupplierOrderProductList = (List<Map<String, Object>>) map.get("shopCarProducts");
			
			for(Map<String,Object> eachMap:sameSupplierOrderProductList){
				totalPrice += (Long)eachMap.get("skuPrice")*(Integer)eachMap.get("skuQuality");
			}
		}
		
		model.addAttribute("totalPrice",totalPrice);
		model.addAttribute("supplierProducts",supplierProducts);
		
		ReceiveAddress receiveAddress = manageReceAddService.getDefaultAddress("2", UUID.randomUUID().toString().replaceAll("-",""));
		
		for(int i=0;i<supplierProducts.size();i++){
			
			//计算一次邮费
			List<Map<String,Object>> shopCarProducts = (List<Map<String, Object>>) supplierProducts.get(i).get("shopCarProducts");
			Long supplierPostFee = 0L;
			double supplierAmount = 0.0;
			for(int j=0;j<shopCarProducts.size();j++){
				Integer skuID = (Integer) shopCarProducts.get(j).get("skuID");
				Integer productID = productSkuService.getSkuByID(skuID, UUID.randomUUID().toString().replaceAll("-","")).getProductID();
				if(receiveAddress!=null){
					Long postFee = postFeeService.calcPostFee(productID, (Integer) shopCarProducts.get(j).get("skuQuality"), receiveAddress.getCountryID(), receiveAddress.getProvinceID(), receiveAddress.getCityID(), receiveAddress.getDistrictID(), UUID.randomUUID().toString().replaceAll("-",""));
					supplierPostFee += postFee;
				}
				supplierAmount+=(Integer) shopCarProducts.get(j).get("skuQuality")*(Long) shopCarProducts.get(j).get("skuPrice");
				
			}
			//如果供应商对应的小计 不含运费 则 去掉以下这一行
			supplierAmount+=supplierPostFee/100.0;
			supplierProducts.get(i).put("postFee", supplierPostFee/100.0);
			supplierProducts.get(i).put("supplierAmount", supplierAmount);
			totalPostFee+=(supplierPostFee/100.0);
		}
		if(receiveAddress!=null){
			model.addAttribute("recName",receiveAddress.getRecName());
			model.addAttribute("recMobile",receiveAddress.getRecMobile());
			model.addAttribute("fullAddress",manageReceAddService.getFullAddress(receiveAddress, UUID.randomUUID().toString().replaceAll("-","")));
		}
		model.addAttribute("totalPostFee",totalPostFee);
		model.addAttribute("totalPrice",totalPrice+totalPostFee);
		
		return "order-generate";
		
	}
	
	@RequestMapping("/viewOrder")
	public String viewOrder(String requestId,Integer orderID, Model model){
		
		//假设参数有一个orderID
		Order order = orderService.getOrderByID(orderID, UUID.randomUUID().toString().replaceAll("-",""));
		order = orderService.setReceAdd("2", order, UUID.randomUUID().toString().replaceAll("-",""));
		
		List<Order> childOrders = orderService.getChildOrdersByParentOrderID(orderID, UUID.randomUUID().toString().replaceAll("-",""));
		
		List<Map<String,Object>> viewList = new ArrayList<Map<String,Object>>();
		
		
		for(int i=0;i<childOrders.size();i++){
			
			Order childOrder = childOrders.get(i);

			//设置子订单的收货地址相关信息
			childOrder.setRecName(order.getRecName());
			childOrder.setRecMobile(order.getRecMobile());
			childOrder.setAddress(order.getAddress());
			childOrder.setCountry(order.getCountry());
			childOrder.setProvince(order.getProvince());
			childOrder.setCity(order.getCity());
			childOrder.setDistrict(order.getDistrict());
			
//			一个订单商品对应多个子订单
			List<OrderProduct> orderProducts = orderProductService.getOrderProductByOrderID(childOrder.getID(), UUID.randomUUID().toString().replaceAll("-",""));
			
			for(OrderProduct orderProduct:orderProducts){
				//查找对应的商品
				Product product = productService.queryProduct(orderProduct.getProductID(),UUID.randomUUID().toString().replaceAll("-",""));
				
				//查找对应的sku
				ProductSku productSku = productSkuService.getSkuByID(orderProduct.getSkuID(),UUID.randomUUID().toString().replaceAll("-",""));
				
				
				Map<String,Object> orderProductMap = new HashMap<String,Object>();
				orderProductMap.put("supName", orderProduct.getSupName());						//设置供应商名字
				orderProductMap.put("sellDesc", product.getSellDesc());							//商品描述
				
				List<String> skuStrs = productSkuService.getSkuStr(orderProduct.getSkuID(),UUID.randomUUID().toString().replaceAll("-",""));
				
				orderProductMap.put("skuStrs", skuStrs);										//sku描述  尺码:39 颜色:红色
				orderProductMap.put("skuPrice", productSku.getSkuPrice());						//sku价格
				orderProductMap.put("skuImg", productSku.getSkuImg());							//sku图片
				orderProductMap.put("buyNum", orderProduct.getBuyNum());						//该sku的购买数量
	//			
				ReceiveAddress receiveAddress = manageReceAddService.getDefaultAddress("2", UUID.randomUUID().toString().replaceAll("-",""));
				if(order.getRecName()==null){
					orderProductMap.put("postFee", 0);
				}else{
					
					Long postFee = postFeeService.calcPostFee(orderProduct.getProductID(), orderProduct.getBuyNum(), receiveAddress.getCountryID(), receiveAddress.getProvinceID(), receiveAddress.getCountryID(), receiveAddress.getDistrictID(), UUID.randomUUID().toString().replaceAll("-",""));
					if(postFee==0){
						orderProductMap.put("postFee", "包邮");
					}else{
						orderProductMap.put("postFee", postFee);
					}
					
				}
				
				viewList.add(orderProductMap);
			}
			
		}
		
//		//下单的时候不一定要填电话号码 
		model.addAttribute("recName",order.getRecName());
		model.addAttribute("recMobile",order.getRecMobile());
		model.addAttribute("fullAddress",order.getFullAddress());
		model.addAttribute("viewList",viewList);
		model.addAttribute("productNum",childOrders.size());
		model.addAttribute("orderSum",order.getpSum());
		
		return "order-generate";
	}
	
	
	/**
	 * 点击立即购买
	 * @param userID
	 * @param productID
	 * @param skuID
	 * @param buyNum
	 * @param model
	 * @return
	 */
	@RequestMapping("/buyNow")
	public String order(String requestId,Integer productID, Integer skuID,Integer buyNum, Long skuPrice, Model model){
		
		System.out.println("productID"+productID);
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");		
		
		String mqID = principal.getMqId();
		
		shopCarService.emptyShopCar(2, UUID.randomUUID().toString().replaceAll("-",""));
			
		shopCarService.addToShopCar(2, productID, skuID, buyNum, UUID.randomUUID().toString().replaceAll("-",""));

		return "redirect:order/previewOrder";
	}
	
	/**
	 * 在购物车点击结算
	 */
	@RequestMapping("settle")
	public String settle(String requestId,Integer[] skuIDs,Integer[] buyNums,Long[] skuPrices){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");		
		
		String mqID = principal.getMqId();
		
		List<ShopCarProduct> shopCarProducts = shopCarService.getShopCarProducts(mqID, UUID.randomUUID().toString().replaceAll("-",""));
		for(int i=0;i<skuIDs.length;i++){
			for(int j=0;j<shopCarProducts.size();j++){
				System.out.println(skuIDs[i]+" "+buyNums[i]);
				if(skuIDs[i].equals(Integer.valueOf(shopCarProducts.get(j).getSkuID()))){
					if(!buyNums[i].equals(shopCarProducts.get(j).getQuality())){
						ShopCarProduct shopCarProduct = shopCarProducts.get(j);
						shopCarProduct.setQuality(buyNums[i]);
						shopCarService.updateShopCarProduct(mqID, shopCarProduct, UUID.randomUUID().toString().replaceAll("-",""));
					}
				}
			}
		}
		
		List<Integer> skuIDList = new ArrayList<Integer>();
		for(int i=0;i<skuIDs.length;i++){
			skuIDList.add(skuIDs[i]);
		}
		
		Set<Integer> deletedSkuIDs = new HashSet<Integer>();
		
		for(int i=0;i<shopCarProducts.size();i++){
			if(!skuIDList.contains(shopCarProducts.get(i).getSkuID())){
				deletedSkuIDs.add(shopCarProducts.get(i).getSkuID());
			}
		}
		
		
		if(deletedSkuIDs.size()!=0){
			Integer[] deletedSkuIDArray = new Integer[deletedSkuIDs.size()];
			deletedSkuIDs.toArray(deletedSkuIDArray);
			shopCarService.removeShopCarProduct(2, deletedSkuIDArray, UUID.randomUUID().toString().replaceAll("-",""));
		}
		
		/*mqID = 2+"";
		System.out.println(skuIDs.length+"  ** "+buyNums.length+"    "+skuPrices.length);
		int orderID = orderService.placeOrder(mqID, skuIDs, buyNums, skuPrices, UUID.randomUUID().toString().replaceAll("-",""));
		shopCarService.emptyShopCar(2, UUID.randomUUID().toString().replaceAll("-",""));
		System.out.println("订单号:------"+orderID);*/
		return "redirect:order/previewOrder";
		
	}
	
	@RequestMapping("chooseReceAdd")
	public String chooseReceAdd(Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
//if(principal==null) principal=new Principal("2", "");		
		String mqID = principal.getMqId();
		
		List<ReceiveAddress> adds = manageReceAddService.listUsableAddress(mqID, UUID.randomUUID().toString().replaceAll("-",""));
		for(int i=0;i<adds.size();i++){
			adds.get(i).setAddress(manageReceAddService.getFullAddress(adds.get(i), UUID.randomUUID().toString().replaceAll("-","")));
		}
		model.addAttribute("adds",adds);
		return "orderReceAdd/manage";
	}
	
	/**
	 * 编辑用户收货地址 可能是更新也可能是新增
	 * @param ID
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/order_edit_rece_add")
	private String edit(Integer ID,Model model) throws Exception{
		
		List<Address> provinces = addressService.getSubAddress(0,UUID.randomUUID().toString().replaceAll("-",""));
		model.addAttribute("provinces",provinces);
		List<Address> cities = new ArrayList<Address>();
		List<Address> districts = new ArrayList<Address>();
		
		if(ID!=null){
			
			ReceiveAddress address = manageReceAddService.getAddress(ID,UUID.randomUUID().toString().replaceAll("-",""));
			model.addAttribute("add",address);
			cities = addressService.getSubAddress(address.getProvinceID(),UUID.randomUUID().toString().replaceAll("-",""));
			districts = addressService.getSubAddress(address.getCityID(),UUID.randomUUID().toString().replaceAll("-",""));
			
		}else{
			cities = addressService.getSubAddress(10,UUID.randomUUID().toString().replaceAll("-",""));
			districts = addressService.getSubAddress(1010,UUID.randomUUID().toString().replaceAll("-",""));
		}
		model.addAttribute("cities",cities);
		model.addAttribute("districts",districts);
		return "orderReceAdd/edit";
		
	}
	
	/**
	 * 新增用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/order_add_rece_address")
	private String add(ReceiveAddress receiveAddress,Model model) throws Exception{
		
		System.out.println(receiveAddress.getIsDefault());
		manageReceAddService.addAddress(receiveAddress,UUID.randomUUID().toString().replaceAll("-",""));
		
		//返回地址列表页面
		return "redirect:chooseReceAdd";
	}
	
	/**
	 * 删除用户收货地址
	 * @param ID
	 * @param model
	 * @return
	 */
	@RequestMapping("/order_del_rece_add")
	private String del(int ID,Model model){
		
		manageReceAddService.deleteAddress(ID,UUID.randomUUID().toString().replaceAll("-",""));

		return "redirect:chooseReceAdd";
	}
	
	/**
	 * 更新用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 */
	@RequestMapping("/order_update_rece_add")
	private String update(ReceiveAddress receiveAddress,Model model){
		
		manageReceAddService.updateAddress(receiveAddress,UUID.randomUUID().toString().replaceAll("-",""));
		
		return "redirect:chooseReceAdd";
	}
	
	/**
	 * 获得signature 作为前台wx.config
	 * @return
	 */
	@RequestMapping("/getConfig")
	@ResponseBody
	public Map<String,String> getConfig(String requestId){
		
		Map<String,String> retMap = new HashMap<String,String>();
		retMap = wxInteractionService.getConfig(UUID.randomUUID().toString().replaceAll("-",""));
		
		return retMap;
	}
	
	/**
	 * 统一下单通知
	 * @param retXml
	 * @return
	 */
	@RequestMapping("/unifiedorder_notify")
	public String unifiedorder_notify(String requestId,String retXml, HttpServletRequest req, HttpServletResponse resp){

		Enumeration pNames=req.getParameterNames();
		while(pNames.hasMoreElements()){
		    String name=(String)pNames.nextElement();
		    String value=req.getParameter(name);
		    System.out.println(name+": "+value);
		}

		System.out.println("requestId: "+requestId);
		try {
			System.out.println("retXml: "+java.net.URLDecoder.decode(retXml,"utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		Map<String,String> retMap = new TreeMap<String,String>();
		
		Pattern pattern = Pattern.compile("<([^/]\\S*?)>(.*?)</(\\S*?)>");
		Matcher m = pattern.matcher(retXml.replace("<xml>", "").replace("</xml>", ""));
		while(m.find()){
			retMap.put(m.group(1), m.group(2).replace("<![CDATA[", "").replace("]]>", ""));
		}
		
		String return_code = retMap.get("return_code");
		
		if(return_code.equals("SUCCESS")){
			
			String result_code = retMap.get("result_code");
			//先验证签名
			String sign = retMap.get("sign");
			System.out.println("sign: "+sign);
			retMap.remove("sign");
			String createSign = "B552ED6B279343CB493C5DD0D78AB241";//wxInteractionService.createSign(retMap);
			if(createSign.equals(sign)){
				String orderCode = retMap.get("out_trade_no");
				Long total_fee = Long.valueOf(retMap.get("total_fee"));
				String payTime = retMap.get("time_end");
				String fee_type = retMap.get("fee_type");
				String transaction_id = retMap.get("transaction_id");
				Order order = orderService.getOrderByOrderCode(orderCode, UUID.randomUUID().toString().replace("-", ""));
				if(order!=null){
					if(result_code.equals("SUCCESS")){
						System.out.println("result_code .equals Success");
						if(!order.getpSum().equals(total_fee)){
							//支付错误 支付金额被篡改
							System.out.println("total_fee not equals");
						}else{
							//设置订单的支付时间和支付金额 还有支付状态
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							try {
								Date pay_time = sdf.parse(payTime);
								order.setPayTime(pay_time);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							order.setPaySum(total_fee);
							order.setPayStatus("PAY");
							orderService.updateOrder(order, UUID.randomUUID().toString());
							OrderPayment orderPayment = new OrderPayment();
							
							//新增订单支付对象
							orderPayment.setCreateTime(new Date());
							orderPayment.setMoneyType(fee_type);
							orderPayment.setMqID(order.getMqID());
							orderPayment.setOrderCode(orderCode);
							orderPayment.setPayChannel("WX#");
							orderPayment.setPayNum(transaction_id);
							orderPayment.setPayStatus("PAY");
							orderPayment.setPaySum(total_fee);
							orderPayment.setPayTime(order.getPayTime());
							
							orderService.addOrderPayment(orderPayment,UUID.randomUUID().toString().replace("-", ""));
							
						}
					}else{
						OrderPayment orderPayment = new OrderPayment();
						
						//新增订单支付对象
						orderPayment.setCreateTime(new Date());
						orderPayment.setMoneyType(fee_type);
						orderPayment.setMqID(order.getMqID());
						orderPayment.setOrderCode(orderCode);
						orderPayment.setPayChannel("WX#");
						orderPayment.setPayNum(transaction_id);
						orderPayment.setPayStatus("NOT");
						orderPayment.setPaySum(total_fee);
						orderPayment.setPayTime(order.getPayTime());
						
						orderService.addOrderPayment(orderPayment,UUID.randomUUID().toString().replace("-", ""));
						
					}
				}else{
					// 订单不存在
				}
				
			}else{
				//签名失败 数据被篡改
				//支付错误
			}
		}else{
			//支付错误
		}
		
		return "";
		
	}
	
	
	/**
	 * 点击 某个订单的退货按钮 跳转到这里 然后查找出相关的信息 再跳转到申请退货页面
	 */
	@RequestMapping("/return-goods")
	public String returnGoods(String requestID, String orderCode, Integer orderProductID, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestID);
		model.addAttribute("refundLimit",order.getPaySum()/100.0);//支付多少钱 最多退款多少钱
		model.addAttribute("orderCode",orderCode);
		model.addAttribute("orderProductID", orderProductID);
		return "order/return-goods";
		
	}
	
	/**
	 * 点击某个已支付订单的退款按钮  经过这里 再跳转到申请退款的页面
	 */
	@RequestMapping("/return-money")
	public String returnMoney(String requestID, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestID);
		model.addAttribute("refundLimit",order.getPaySum()/100.0);//支付多少钱 最多退款多少钱
		model.addAttribute("orderCode",orderCode);
		return "order/return-money";
	}
	
	/**
	 * 在退货页面 点击 申请退货
	 * @param requestID
	 * @param orderCode
	 * @param returnReason 退货原因
	 * @param refundFee    退款金额
	 * @param returnDesc   退货说明
	 * @return
	 */
	@RequestMapping("/apply-return-goods")
	public String applyReturnGoods(String requestID, String orderCode, Integer orderProductID, String returnReason, Long refundFee, String returnDesc, String[] imgs, Model model){

		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");
		
		String mqID = principal.getMqId();
		try{
			GoodsReturn goodsReturn = orderService.applyReturnGoods(mqID, orderCode, orderProductID, refundFee, returnReason, returnDesc, imgs, requestID);
			model.addAttribute("goodsReturn", goodsReturn);
			if(goodsReturn.getReturnReason().equals("SJ#")){
				model.addAttribute("returnReason","少件/漏发");
			}else if(goodsReturn.getReturnReason().equals("FP#")){
				model.addAttribute("returnReason","发票问题");
			}else{
				model.addAttribute("returnReason",GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
			}
		}catch(RuntimeException e){
			//返回错误页面
			return "404";
		}
		
		return "order/returnG-examine";
	}
	
	@RequestMapping("/modify-return-goods")
	public String modifyReturnGoods(String requestID, Integer goodsReturnID, Model model){
		
		GoodsReturn goodsReturn = orderService.getGoodsReturnByID(goodsReturnID, requestID);
		Order order = orderService.getOrderByID(goodsReturn.getOrderID(), requestID);
		model.addAttribute("orderCode",goodsReturn.getOrderCode());
		model.addAttribute("orderProductID",goodsReturn.getOrderProductID());
		model.addAttribute("returnReasonCode",goodsReturn.getReturnReason());
		if(goodsReturn.getReturnReason().equals("SJ#")){
			model.addAttribute("returnReason","少件/漏发");
		}else if(goodsReturn.getReturnReason().equals("FP#")){
			model.addAttribute("returnReason","发票问题");
		}else{
			model.addAttribute("returnReason",GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
		}
		model.addAttribute("refundLimit",order.getPaySum()/100.0);
		model.addAttribute("applySum",goodsReturn.getApplySum()/100.0);
		model.addAttribute("returnDesc",goodsReturn.getReturnDesc());
		return "order/modify-returnG";
		
	}
	
	
	/**
	 * 点击 提交申请 按钮  
	 * @param requestID
	 * @param orderCode 订单编号
	 * @param returnReason 退款原因
	 * @param refundFee    退款金额
	 * @param returnDesc   退款描述
	 * @param model
	 * @return
	 */
	@RequestMapping("/apply-return-money")
	public String applyReturnMoney(String requestID, String orderCode, String returnReason, Long refundFee, String returnDesc, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null) principal=new Principal("2", "");
				
		String mqID = principal.getMqId();
		try{
			MoneyReturn moneyReturn = orderService.applyReturnMoney(mqID, orderCode, refundFee, returnReason, returnDesc, requestID);
			model.addAttribute("moneyReturn", moneyReturn);
			if(moneyReturn.getReturnReason().equals("SJ#")){
				model.addAttribute("returnReason","少件/漏发");
			}else if(moneyReturn.getReturnReason().equals("FP#")){
				model.addAttribute("returnReason","发票问题");
			}else{
				model.addAttribute("returnReason",MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
			}
		}catch(RuntimeException e){
			//返回错误页面
			return "404";
		}
		
		return "order/returnR-examine";
	}
	
	/**
	 * 点击 修改退款申请  只有在 前一次申请 还没有审核的情况下 才能 修改
	 * @param requestID
	 * @param moneyReturnID
	 * @param model
	 * @return
	 */
	@RequestMapping("/modify-return-money")
	public String modifyReturnMoney(String requestID, Integer moneyReturnID, Model model){
		
		MoneyReturn moneyReturn = orderService.getMoneyReturnByID(moneyReturnID, requestID);
		Order order = orderService.getOrderByID(moneyReturn.getOrderID(), requestID);
		model.addAttribute("orderCode",moneyReturn.getOrderCode());
		model.addAttribute("returnReasonCode",moneyReturn.getReturnReason());
		if(moneyReturn.getReturnReason().equals("SJ#")){
			model.addAttribute("returnReason","少件/漏发");
		}else if(moneyReturn.getReturnReason().equals("FP#")){
			model.addAttribute("returnReason","发票问题");
		}else{
			model.addAttribute("returnReason",MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
		}
		model.addAttribute("refundLimit",order.getPaySum()/100.0);
		model.addAttribute("applySum",moneyReturn.getApplySum()/100.0);
		model.addAttribute("returnDesc",moneyReturn.getReturnDesc());
		return "order/modify-returnR";
		
	}
	
	/**
	 * 查看我的订单
	 * @param requestID
	 * @param model
	 * @return
	 */
	@RequestMapping("/order-index")
	public String orderIndex(String requestID, Model model){
		return "/order/order-index";
	}
	
	@RequestMapping("/return-goods-detail")
	public String returnGoodsDetail(String requestID, Integer goodsReturnID, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null)principal = new Principal("2","");

		String mqID = principal.getMqId();
		GoodsReturn goodsReturn = orderService.getGoodsReturnByID(goodsReturnID, requestID);
		ReceiveAddress receiveAddress = manageReceAddService.getDefaultAddress(mqID, requestID);
		if(receiveAddress!=null){
			model.addAttribute("fullAddress",manageReceAddService.getFullAddress(receiveAddress, requestID));
			model.addAttribute("recName",receiveAddress.getRecName());
			model.addAttribute("recMobile",receiveAddress.getRecMobile());
		}
		model.addAttribute("goodsReturn",goodsReturn);
		//退货原因
		if(goodsReturn.getReturnReason().equals("SJ#")){
			model.addAttribute("returnReason","少件/漏发");
		}else if(goodsReturn.getReturnReason().equals("FP#")){
			model.addAttribute("returnReason","发票问题");
		}else{
			model.addAttribute("returnReason",GoodsReturn.ReturnReason.valueOf(goodsReturn.getReturnReason()).getValue());
		}
		if(goodsReturn.getAuditStatus()==null){//此处auditStatus==null  则下面必然不会==null
			model.addAttribute("applySum",goodsReturn.getApplySum()/100.0);
			model.addAttribute("returnDesc",goodsReturn.getReturnDesc());
			return "order/returnG-examine";
		}else if(goodsReturn.getAuditStatus().equals("NO#")){
			return "order/returnG-reject";
		}else if(goodsReturn.getAuditStatus().equals("YES") && goodsReturn.getStatus()==null){//审核通过且商家还未收到 退货
			return "order/returnG-pass";
		}else if(goodsReturn.getAuditStatus().equals("YES") && goodsReturn.getStatus().equals("THZ")){//审核通过 且商家收到退货 但是还没有将钱退给用户
			return "order/returnG-takeG";
		}else if(goodsReturn.getAuditStatus().equals("YES") && (goodsReturn.getStatus().equals("TKZ")||goodsReturn.getStatus().equals("COM"))){
			return "order/returnG-success";
		}
		
		return "404";
	}
	
	@RequestMapping("/return-money-detail")
	public String returnMoneyDetail(String requestID, Integer moneyReturnID, Model model){
		
		Principal principal = WebUtil.getCurrentPrincipal();
		
//if(principal==null)principal = new Principal("2","");

		String mqID = principal.getMqId();
		
		MoneyReturn moneyReturn = orderService.getMoneyReturnByID(moneyReturnID, requestID);
		
		model.addAttribute("moneyReturn",moneyReturn);
		//退货原因
		if(moneyReturn.getReturnReason().equals("SJ#")){
			model.addAttribute("returnReason","少件/漏发");
		}else if(moneyReturn.getReturnReason().equals("FP#")){
			model.addAttribute("returnReason","发票问题");
		}else{
			model.addAttribute("returnReason",MoneyReturn.ReturnReason.valueOf(moneyReturn.getReturnReason()).getValue());
		}
		
		if(moneyReturn.getAuditStatus()==null){//此处auditStatus==null  则下面必然不会==null
			return "order/returnR-examine";
		}else if(moneyReturn.getAuditStatus().equals("NO#")){
			return "order/returnR-reject";
		}else if(moneyReturn.getAuditStatus().equals("YES") && moneyReturn.getStatus().equals("ING")){//审核通过 正在退款中
			return "order/returnR-pass";
		}else if(moneyReturn.getAuditStatus().equals("YES") && moneyReturn.getStatus().equals("YES")){//后台确认退款
			return "order/returnR-success";
		}
		
		return "404";
	}
	
}
