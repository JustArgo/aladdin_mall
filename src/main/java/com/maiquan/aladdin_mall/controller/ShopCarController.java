package com.maiquan.aladdin_mall.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
	public String shopping_cart(Integer mqID,Model model){
		
		List<Map<String,Object>> shopCarProducts = shopCarService.viewShopCar(mqID+"", UUID.randomUUID().toString());		
		
		Long totalPrice = 0L;
		
		for(int i=0;i<shopCarProducts.size();i++){
			totalPrice+=(Long)shopCarProducts.get(i).get("skuPrice")*(Integer)shopCarProducts.get(i).get("skuQuality");
		}
		
		model.addAttribute("productNum", shopCarProducts.size());
		model.addAttribute("totalPrice",totalPrice);
		model.addAttribute("shopCarProducts",shopCarProducts);
		
		return "shop-car";
	}
	
	@RequestMapping("/remove_shopcar_product")
	@ResponseBody
	public String remove_shopcar_product(Integer userID, Integer[] skuIDs){
		System.out.println(userID+"------------"+skuIDs);
		if(userID==null || skuIDs==null){
			return "{\"errcode\":10042,\"errormsg\":\"invalid arguments\"}";
		}
		shopCarService.removeShopCarProduct(userID, skuIDs, UUID.randomUUID().toString());
		return "{\"errcode\":0,\"errormsg\":\"success\"}";
	}
	
	@RequestMapping("/add_to_shopcar")
	@ResponseBody
	public String add_to_shopcar(Integer productID, Integer skuID, Integer buyNum){
		
		shopCarService.addToShopCar(2, productID, skuID, buyNum, UUID.randomUUID().toString());
		
		return "{\"errcode\":\"0\"}";
	}
	
}
