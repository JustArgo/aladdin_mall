package com.maiquan.aladdin_mall.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maiquan.aladdin_mall.Principal;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_shopcar.service.IShopCarService;


@Controller
public class ShopCarController {

	@Autowired
	private IShopCarService shopCarService;
	
	/**
	 * 查看购物车
	 * @return
	 */
	@RequestMapping("/shop_car")
	public String shopping_cart(Model model){
		
		//Principal principal = (Principal) WebUtil.getSession().getAttribute(Principal.ATTRIBUTE_KEY);
		String mqID = "2";//principal.getMqID();
		
		List<Map<String,Object>> supplierProducts = shopCarService.viewShopCar(mqID, UUID.randomUUID().toString());		
		
		Long totalPrice = 0L;
		
		for(int i=0;i<supplierProducts.size();i++){
			
			Map<String,Object> map = supplierProducts.get(i);
			List<Map<String,Object>> sameSupplierOrderProductList = (List<Map<String, Object>>) map.get("shopCarProducts");
			
			for(Map<String,Object> eachMap:sameSupplierOrderProductList){
				totalPrice += (Long)eachMap.get("skuPrice")*(Integer)eachMap.get("skuQuality");
			}
		}
		
		model.addAttribute("totalPrice",totalPrice);
		model.addAttribute("supplierProducts",supplierProducts);
		
		return "shop-car";
	}
	
	@RequestMapping("/remove_shopcar_product")
	@ResponseBody
	public String remove_shopcar_product(Integer[] skuIDs){
		Principal principal = WebUtil.getCurrentPrincipal();
		String mqID = "2";
//		if(principal!=null){
//			mqID = principal.getMqID();
//		}
		for(int i=0;i<skuIDs.length;i++){
			System.out.println("---"+skuIDs[i]);
		}
		if(mqID==null || skuIDs==null){
			return "{\"errcode\":10042,\"errormsg\":\"invalid arguments\"}";
		}
		shopCarService.removeShopCarProduct(Integer.valueOf(mqID), skuIDs, UUID.randomUUID().toString());
		return "{\"errcode\":0,\"errormsg\":\"success\"}";
	}
	
	@RequestMapping("/add_to_shopcar")
	@ResponseBody
	public String add_to_shopcar(Integer productID, Integer skuID, Integer buyNum){
		
		shopCarService.addToShopCar(2, productID, skuID, buyNum, UUID.randomUUID().toString());
		
		return "{\"errcode\":\"0\"}";
	}
	
}
