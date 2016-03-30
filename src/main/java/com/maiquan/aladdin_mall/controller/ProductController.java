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
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.dubbo.common.json.JSON;
import com.maiquan.aladdin_mall.util.WebUtil;
import com.maiquan.aladdin_product.domain.Product;
import com.maiquan.aladdin_product.domain.ProductAttr;
import com.maiquan.aladdin_product.domain.ProductAttrValue;
import com.maiquan.aladdin_product.domain.ProductSku;
import com.maiquan.aladdin_product.domain.ProductSkuAttr;
import com.maiquan.aladdin_product.service.IPostFeeService;
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

	@Autowired
	private IProductSkuService productSkuService;
	
	@Autowired
	private IPostFeeService postFeeService;
	
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
		ProductVo productVo = productVoService.getProductVo(productID, UUID.randomUUID().toString().replaceAll("-",""));

		System.out.println(productVo);
		//获得该商品 共有多少个sku
		Integer skuStock = 0;
		for(ProductSku sku:productVo.getSkus()){
			skuStock += sku.getSkuStock();
		}
		
		Product p = productService.queryProduct(productID, UUID.randomUUID().toString().replaceAll("-",""));
		model.addAttribute("productVo", productVo);

		// 封装该产品的属性
		List<ProductAttr> productAttrs = productService.getProductAttrByProductID(productID,
				UUID.randomUUID().toString().replaceAll("-",""));
		for (int i = 0; i < productAttrs.size(); i++) {

			Map<String, Object> attrMap = new HashMap<String, Object>();
			attrMap.put("attrName", productAttrs.get(i).getAttrName());
			attrMap.put("attrID", productAttrs.get(i).getID());

			List<ProductAttrValue> productAttrValues = productService.getAttrValuesByAttrID(productAttrs.get(i).getID(),
					UUID.randomUUID().toString().replaceAll("-",""));
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
		model.addAttribute("productStock",skuStock);
		return "productdetail";
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

		List<ProductSku> skus = productSkuService.getSkuByProductID(productID, UUID.randomUUID().toString().replaceAll("-",""));

		System.out.println("该产品对应几个sku---" + skus.size());

		for (int i = 0; i < skus.size(); i++) {

			int matchAttr = 0;

			ProductSku sku = skus.get(i);
			System.out.println("当前是第" + (i + 1) + "个sku id--" + sku.getID());
			List<ProductSkuAttr> skuAttrs = productSkuService.getSkuAttrBySkuID(sku.getID(),
					UUID.randomUUID().toString().replaceAll("-",""));

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
	
	@RequestMapping("/collect")
	@ResponseBody
	public Map<String,Object> collect(String mqID, Integer productID, Integer collect){
		
		Map<String,Object> ret = new HashMap<String,Object>();
		
		if(mqID==null || productID==null ||collect==null){
			ret.put("errcode",10042);
			ret.put("errmsg", "invalid arguments");
			return ret;
		}
		
		if(collect==0){//0代表取消收藏
			productService.uncollectProduct(mqID, productID, UUID.randomUUID().toString().replaceAll("-",""));
			ret.put("errcode", 0);
			ret.put("errmsg", "uncollect success");
		}else{//1代表收藏
			productService.collectProduct(mqID, productID, UUID.randomUUID().toString().replaceAll("-",""));
			ret.put("errcode", 0);
			ret.put("errmsg", "collect success");
		}
		
		return ret;
		
	}
	
	@RequestMapping("/calcPostFee")
	@ResponseBody
	public Long calcPostFee(Integer productID, Integer buyNum, Integer countryID,Integer provinceID,Integer cityID,Integer districtID){
		return postFeeService.calcPostFee(productID, buyNum, countryID, provinceID, cityID, districtID, UUID.randomUUID().toString().replaceAll("-",""));
	}

	/**
	 * 商品列表
	 * @param categoryId 商品分类id
	 * @param modelMap
	 * @return
	 */
	@RequestMapping(value = "/list/{categoryId}", method = RequestMethod.GET)
	public String list(@PathVariable Integer categoryId,ModelMap modelMap) {
//		modelMap.addAttribute("products", productService.getProductListByCategoryID(categoryId,WebUtil.getCurrentPrincipal().getMqId()));
		modelMap.addAttribute("products", productService.getProductListByCategoryID(categoryId,null));
		return "product/list";
	}
}
