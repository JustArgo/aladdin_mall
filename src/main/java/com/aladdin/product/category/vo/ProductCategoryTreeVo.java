package com.aladdin.product.category.vo;

import java.io.Serializable;
import java.util.List;

import com.aladdin.product.category.entity.ProductCategory;
import com.aladdin.product.category.entity.ProductCategoryImg;

/**
 * 树形商品分类
 * 
 * @author JSC
 *
 */
public class ProductCategoryTreeVo implements Serializable {
	/** 分类图片 */
	private ProductCategoryImg img;

	/** 父商品分类 */
	private ProductCategory parent;

	/**
	 * 子商品分类
	 */
	private List<ProductCategory> leafs;

	public ProductCategoryImg getImg() {
		return img;
	}

	public void setImg(ProductCategoryImg img) {
		this.img = img;
	}

	public ProductCategory getParent() {
		return parent;
	}

	public void setParent(ProductCategory parent) {
		this.parent = parent;
	}

	public List<ProductCategory> getLeafs() {
		return leafs;
	}

	public void setLeafs(List<ProductCategory> leafs) {
		this.leafs = leafs;
	}

}
