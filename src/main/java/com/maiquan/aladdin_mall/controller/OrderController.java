package com.maiquan.aladdin_mall.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.maiquan.aladdin_order.domain.Order;
import com.maiquan.aladdin_order.domain.OrderProduct;
import com.maiquan.aladdin_order.service.IOrderProductService;
import com.maiquan.aladdin_order.service.IOrderService;
import com.maiquan.aladdin_product.domain.Product;
import com.maiquan.aladdin_product.domain.ProductSku;
import com.maiquan.aladdin_product.service.IProductService;
import com.maiquan.aladdin_product.service.IProductSkuService;
import com.maiquan.aladdin_receadd.domain.ReceiveAddress;
import com.maiquan.aladdin_receadd.service.IManageReceAddService;
import com.maiquan.aladdin_shopcar.service.IShopCarService;

@Controller
public class OrderController {

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
	
	//@Autowired
	//private ISupplierService supplierService;
	
	@RequestMapping("/viewOrder")
	public String viewOrder(Integer orderID, Model model){
		
		//假设参数有一个orderID
		Order order = orderService.getOrderByID(orderID, UUID.randomUUID().toString());
		
		List<Order> childOrders = orderService.getChildOrdersByParentOrderID(orderID, UUID.randomUUID().toString());
		
		List<Map<String,Object>> viewList = new ArrayList<Map<String,Object>>();
		
		for(int i=0;i<childOrders.size();i++){
			
			Order childOrder = childOrders.get(i);

//			假设是一个订单商品对应一个子订单
			OrderProduct orderProduct = orderProductService.getOrderProductByOrderID(childOrder.getID(), UUID.randomUUID().toString()).get(0);
			
			//查找对应的商品
			Product product = productService.queryProduct(orderProduct.getProductID(),UUID.randomUUID().toString());
			
			//查找对应的sku
			ProductSku productSku = productSkuService.getSkuByID(orderProduct.getSkuID(),UUID.randomUUID().toString());
			
			
			Map<String,Object> orderProductMap = new HashMap<String,Object>();
			orderProductMap.put("supName", orderProduct.getSupName());						//设置供应商名字
			orderProductMap.put("sellDesc", product.getSellDesc());							//商品描述
			
			List<String> skuStrs = productSkuService.getSkuStr(orderProduct.getSkuID(),UUID.randomUUID().toString());
			
			orderProductMap.put("skuStrs", skuStrs);										//sku描述  尺码:39 颜色:红色
			orderProductMap.put("skuPrice", productSku.getSkuPrice());						//sku价格
			orderProductMap.put("skuImg", productSku.getSkuImg());							//sku图片
			orderProductMap.put("buyNum", orderProduct.getBuyNum());						//该sku的购买数量
//
			orderProductMap.put("postFee", 8L);												//该sku邮费
			
			viewList.add(orderProductMap);
			
		}
		
//		//下单的时候不一定要填电话号码 
		model.addAttribute("recName",order.getRecName());
		model.addAttribute("recMobile",order.getRecMobile());
		model.addAttribute("fullAddress",order.getProvince()+order.getCity()+order.getDistrict()+order.getAddress());
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
	public String order(Integer userID,Integer productID, Integer skuID,Integer buyNum, Long skuPrice, Model model){
		
		System.out.println(userID+"---"+productID+"---"+skuID+"---"+buyNum+"---"+skuPrice);
		
		int orderID = orderService.placeOrder(userID+"", new Integer[]{skuID}, new Integer[]{buyNum}, new Long[]{233L}, UUID.randomUUID().toString());
		
		//查找默认收货地址
		ReceiveAddress receAdd = manageReceAddService.getDefaultAddress(1+"", UUID.randomUUID().toString());
		System.out.println("收货地址:"+receAdd);
		if(receAdd!=null){
			model.addAttribute("recName",receAdd.getRecName());
			model.addAttribute("recMobile",receAdd.getRecMobile());
			model.addAttribute("fullAddress",manageReceAddService.getFullAddress(receAdd, UUID.randomUUID().toString()));
		}
		model.addAttribute("supplierName","alibaba");
		model.addAttribute("sellDesc","犬岚，魅力与时尚的结合");
		model.addAttribute("skuImgPath","http://7xrd9k.com2.z0.glb.qiniucdn.com/6309-110R916035585.jpg");
		List<String> skuStrs = new ArrayList<String>();
		skuStrs.add("尺码:x码");
		skuStrs.add("颜色:深驼");
		model.addAttribute("skuStrs",skuStrs);
		model.addAttribute("skuPrice",33L);
		model.addAttribute("postFee",100);
		model.addAttribute("buyNum",3);
		
		
		
		//点击立即购买 没有购物车这个环节

		// 1 查找出要购买的商品
		/*Product product = productService.queryProduct(productID);
		
		//List<OrderProduct> orderProducts = new ArrayList<OrderProduct>();
		
		Order order = new Order();
		order.setID((int)(Math.random()*2147483648L));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String orderCode = sdf.format(new Date());
		
		order.setOrderCode(orderCode);*/

		/*OrderProduct orderProduct = new OrderProduct();
		orderProduct.setID((int)(Math.random()*2147483648L));
		orderProduct.setOrderID(order.getOrderID()+"");
		orderProduct.setProduct(product.getID());
		orderProduct.setProductName(product.getProductName());
		orderProduct.setSkuID(skuID);
		orderProduct.setSkuName("");
		orderProduct.setSupName(product.getSupplyID()+"");
		
		orderProducts.add(orderProduct);
		
		order.setOrderProducts(orderProducts);
		
		//将orderProduct存入
		orderService.addOrder(order,UUID.randomUUID().toString());
		
		//将order存储到数据库
		orderProductService.addOrderProduct(orderProduct, UUID.randomUUID().toString());
		
		//获得morning收货地址
		ReceiveAddress receAdd = manageReceAddService.getDefaultAddress("mqID",UUID.randomUUID().toString());;
		
		model.addAttribute("product",prouct);
		model.addAttribute("sku",sku);
		
		model.addAttribute("shopCar",shopCar);*/
		
		return "redirect:viewOrder?orderID="+orderID;
	}
	
	/**
	 * 在购物车点击结算
	 */
	@RequestMapping("settle")
	public String settle(String mqID,Integer[] skuIDs,Integer[] buyNums,Long[] skuPrices){
		
		mqID = 2+"";
		System.out.println(skuIDs.length+"  ** "+buyNums.length+"    "+skuPrices.length);
		int orderID = orderService.placeOrder(mqID, skuIDs, buyNums, skuPrices, UUID.randomUUID().toString());
		shopCarService.emptyShopCar(2, UUID.randomUUID().toString());
		System.out.println("订单号:------"+orderID);
		return "redirect:viewOrder?orderID="+orderID;
		
	}
}
