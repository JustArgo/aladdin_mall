package com.aladdin.product.category.service;

import java.util.List;
import java.util.Map;

import com.aladdin.product.category.vo.ProductCategoryTreeVo;

/**
 * 商品分类服务
 * 
 * @author JSC
 *
 */
public interface ProductCategoryService {
	/**
	 * 列表
	 * 
	 * @return
	 */
	List<ProductCategoryTreeVo> findList();
}
