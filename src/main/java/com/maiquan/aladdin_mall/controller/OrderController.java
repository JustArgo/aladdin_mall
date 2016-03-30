package com.maiquan.aladdin_mall.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.aladdin.interaction.wx.service.WxInteractionService;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.JSONObject;
import com.maiquan.aladdin_mall.util.DecryptUtil;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_order.domain.Order;
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
	
	//@Autowired
	//private ISupplierService supplierService;
	
	@RequestMapping("/placeOrder")
	public Map<String,String> placeOrder(String mqID, Integer[] skuIds, Integer[] buyNums, Long[] skuPrices, Long[] supplierAmounts, Long pFee, Long pSum, String invoiceName, String notes){
		
		mqID="2";
		System.out.println("mqID"+mqID);
		System.out.print("skuIDs  ");
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
		orderService.placeOrder(mqID, skuIds, buyNums, skuPrices, pFee, pSum, invoiceName, notes, "");

		Map<String,String> parameters = new HashMap<String,String>();
		parameters.put("openid", openID);
		parameters.put("body", "null");
		parameters.put("out_trade_no",new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+(new Random().nextInt(900)+100));
		parameters.put("total_fee", pSum*100+"");
		
		return wxInteractionService.unifiedOrder(UUID.randomUUID().toString().replaceAll("-",""), parameters);
	}
	
	@RequestMapping("/order/previewOrder")
	public String previewOrder(Model model){
		//预览订单
		String mqID = "2";//principal.getMqID();
		
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
	
	@RequestMapping("/return-goods")
	public String returnGoods(String requestID, String orderCode, Model model){
		
		Order order = orderService.getOrderByOrderCode(orderCode, requestID);
		model.addAttribute("refundLimit",order.getPaySum());//支付多少钱 最多退款多少钱
		model.addAttribute("orderCode",orderCode);
		return "order/return-goods";
		
	}
	
	@RequestMapping("/viewOrder")
	public String viewOrder(Integer orderID, Model model){
		
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
	public String order(Integer productID, Integer skuID,Integer buyNum, Long skuPrice, Model model){
		
		System.out.println("productID"+productID);
		
		//Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = "2";//principal.getMqID();
		
		shopCarService.emptyShopCar(2, UUID.randomUUID().toString().replaceAll("-",""));
			
		shopCarService.addToShopCar(2, productID, skuID, buyNum, UUID.randomUUID().toString().replaceAll("-",""));

		/*
		List<Map<String,Object>> supplierList = new ArrayList<Map<String,Object>>();
		List<Map<String,Object>> skuList = new ArrayList<Map<String,Object>>();
		
		
		Map<String,Object> supplierMap = new HashMap<String,Object>();
		Map<String,Object> skuMap = new HashMap<String,Object>();
		
		double totalPostFee = 0.0;
		double supplierAmount = 0.0;
		
		
		Product product = productService.queryProduct(productID, UUID.randomUUID().toString().replaceAll("-",""));
		Supplier supplier = supplierService.getSupplier(product.getSupplyID(), UUID.randomUUID().toString().replaceAll("-",""));
		ProductSku sku = productSkuService.getSkuByID(skuID, UUID.randomUUID().toString().replaceAll("-",""));
		List<String> skuStrs = productSkuService.getSkuStr(sku.getID(), UUID.randomUUID().toString().replaceAll("-",""));
		
		//查找默认收货地址
		ReceiveAddress receAdd = manageReceAddService.getDefaultAddress(2+"", UUID.randomUUID().toString().replaceAll("-",""));
		if(receAdd!=null){
			//总运费 即是 该sku对应的运费
			totalPostFee = postFeeService.calcPostFee(productID, buyNum, receAdd.getCountryID(), receAdd.getProvinceID(), receAdd.getCityID(), receAdd.getDistrictID(), UUID.randomUUID().toString().replaceAll("-",""));
			
			model.addAttribute("recName",receAdd.getRecName());
			model.addAttribute("recMobile",receAdd.getRecMobile());
			model.addAttribute("fullAddress",manageReceAddService.getFullAddress(receAdd, UUID.randomUUID().toString().replaceAll("-","")));
		}
		
		//1 存储skuID
		skuMap.put("skuID", skuID);
		//2 存储sku对应的图片
		skuMap.put("imgPath",sku.getSkuImg());
		//3 存储sku对应的商品描述
		skuMap.put("sellDesc", product.getSellDesc());
		//4 存储sku对应的sku属性
		skuMap.put("skuStrs",skuStrs);
		//5 存储sku对应的价格
		skuMap.put("skuPrice", sku.getSkuPrice());
		//6 存储sku对应的购买数量
		skuMap.put("skuQuality", buyNum);
		
		skuList.add(skuMap);
		
		supplierMap.put("supName",supplier.getName());
		supplierMap.put("shopCarProducts", skuList);
		supplierMap.put("supplierAmount",skuPrice*buyNum+totalPostFee/100.0);
		supplierMap.put("postFee", totalPostFee/100.0);
		
		supplierList.add(supplierMap);
		
		model.addAttribute("supplierProducts",supplierList);
		model.addAttribute("totalPostFee",totalPostFee/100.0);
		model.addAttribute("totalPrice",skuPrice*buyNum+totalPostFee/100.0);*/
		
		
		return "redirect:order/previewOrder";
	}
	
	/**
	 * 在购物车点击结算
	 */
	@RequestMapping("settle")
	public String settle(Integer[] skuIDs,Integer[] buyNums,Long[] skuPrices){
		
		//Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = "2";//principal.getMqID();
		
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
		
		//Principal principal = WebUtil.getPrincipal();
		String mqID = "2";//principal.getMqID();
		
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
	public Map<String,String> getConfig(){
		
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
	public String unifiedorder_notify(String retXml){
		System.out.println("unifiedorder_notify");
		System.out.println("retXml----"+retXml);
		return "";
	}
	
}
