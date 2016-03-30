package com.maiquan.aladdin_mall.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.aladdin.product.category.service.ProductCategoryService;

@Controller
@RequestMapping("/productCategory")
public class ProductCategoryController {
	@Autowired
	private ProductCategoryService productCategoryService;

	@RequestMapping(value = "/index", method = RequestMethod.GET)
	public String index(ModelMap modelMap) {
		modelMap.addAttribute("productCategory",productCategoryService.findList(UUID.randomUUID().toString().replaceAll("-", "")));
		return "productCategory/index";
	}
}
