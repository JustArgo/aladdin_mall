package com.maiquan.aladdin_mall.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.common.json.JSON;
import com.maiquan.aladdin_product.domain.Product;
import com.maiquan.aladdin_product.domain.ProductAttr;
import com.maiquan.aladdin_product.domain.ProductAttrValue;
import com.maiquan.aladdin_product.domain.ProductSku;
import com.maiquan.aladdin_product.domain.ProductSkuAttr;
import com.maiquan.aladdin_product.service.IProductService;
import com.maiquan.aladdin_product.service.IProductSkuService;
import com.maiquan.aladdin_product.service.IProductVoService;
import com.maiquan.aladdin_product.vo.ProductVo;

@Controller
@RequestMapping("/product")
public class ProductController {

	@Autowired
	private IProductService productService;

	@Autowired
	private IProductVoService productVoService;

	// @Autowired
	// private ICommentVoService commentVoService;

	@Autowired
	private IProductSkuService productSkuService;

	/**
	 * 查看商品详情
	 * 
	 * @param productID
	 * @param model
	 * @return
	 */
	@RequestMapping("/product_detail")
	public String productDetail(Integer productID, Model model) {

		List<Map<String, Object>> attrItems = new ArrayList<Map<String, Object>>();
		ProductVo productVo = productVoService.getProductVo(productID, UUID.randomUUID().toString());

		System.out.println(productVo);

		Product p = productService.queryProduct(productID, UUID.randomUUID().toString());
		model.addAttribute("productVo", productVo);

		// 封装该产品的属性
		List<ProductAttr> productAttrs = productService.getProductAttrByProductID(productID,
				UUID.randomUUID().toString());
		for (int i = 0; i < productAttrs.size(); i++) {

			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("attrName", productAttrs.get(i).getAttrName());
			attrMap.put("attrID", productAttrs.get(i).getID());

			List<ProductAttrValue> productAttrValues = productService.getAttrValuesByAttrID(productAttrs.get(i).getID(),
					UUID.randomUUID().toString());
			List<String[]> attrValues = new ArrayList<String[]>();
			for (int j = 0; j < productAttrValues.size(); j++) {
				String[] valueTwin = new String[2];
				valueTwin[0] = productAttrValues.get(j).getID() + "";
				valueTwin[1] = productAttrValues.get(j).getAttrValue();
				attrValues.add(valueTwin);
			}

			attrMap.put("attrValues", attrValues);

			attrItems.add(attrMap);
		}

		model.addAttribute("attrItems", attrItems);
		model.addAttribute("productID", productID);
		return "productdetail";
	}

	@RequestMapping("/commentVo")
	public void getCommentVo() {
		System.out.println("java");
		// CommentVo commentVo = commentVoService.getCommentVo(1);
		try {
			System.out.println(JSON.json(""/* commentVo */));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("querySku")
	@ResponseBody
	public Map<String, Object> querySku(Integer productID, Integer[] attrs, Integer[] values) {

		Map<String, Object> retMap = new HashMap<String, Object>();
		Map<Integer, Integer> skuMap = new HashMap<Integer, Integer>();

		// 先对参数做基础验证
		if (productID == null || attrs == null || values == null || attrs.length != values.length) {
			return retMap;
		}

		System.out.println("productID---" + productID);

		for (int i = 0; i < attrs.length; i++) {
			System.out.println(attrs[i] + "   " + values[i]);
		}

		for (int i = 0; i < attrs.length; i++) {
			skuMap.put(attrs[i], values[i]);
		}

		List<ProductSku> skus = productSkuService.getSkuByProductID(productID, UUID.randomUUID().toString());

		System.out.println("该产品对应几个sku---" + skus.size());

		for (int i = 0; i < skus.size(); i++) {

			int matchAttr = 0;

			ProductSku sku = skus.get(i);
			System.out.println("当前是第" + (i + 1) + "个sku id--" + sku.getID());
			List<ProductSkuAttr> skuAttrs = productSkuService.getSkuAttrBySkuID(sku.getID(),
					UUID.randomUUID().toString());

			if (skuMap.size() == skuAttrs.size()) {
				for (int j = 0; j < skuAttrs.size(); j++) {
					ProductSkuAttr skuAttr = skuAttrs.get(j);
					if (skuMap.containsKey(skuAttr.getAttrID())
							&& skuMap.get(skuAttr.getAttrID()) == skuAttr.getAttrValueID()) {
						matchAttr++;
					} else {
						break;
					}
				}
				// 说明这个sku就是我们要找的sku
				if (matchAttr == skuMap.size()) {
					retMap.put("errcode", 0);
					retMap.put("skuID", sku.getID());
					retMap.put("skuImg", sku.getSkuImg());
					retMap.put("skuStock", sku.getSkuStock());
					retMap.put("skuPrice", sku.getSkuPrice());
				}
			}

		}

		return retMap;
	}

	@RequestMapping(value = "/list/{categoryId}", method = RequestMethod.GET)
	public String list(@PathVariable Integer categoryId) {
		productService.getProductListByCategoryID(categoryId,null);
		return "";
	}
}
