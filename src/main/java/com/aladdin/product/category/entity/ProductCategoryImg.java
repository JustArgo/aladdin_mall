package com.aladdin.product.category.entity;

import java.util.Date;
/**
 * Entity - 商品分类图片
 * @author JSC
 *
 */
public class ProductCategoryImg {
    private Integer id;

    private Integer categoryid;

    private String imgtype;

    private String status;

    private String attrvalueimg;

    private Integer uid;

    private Integer sortorder;

    private Date createtime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategoryid() {
        return categoryid;
    }

    public void setCategoryid(Integer categoryid) {
        this.categoryid = categoryid;
    }

    public String getImgtype() {
        return imgtype;
    }

    public void setImgtype(String imgtype) {
        this.imgtype = imgtype;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttrvalueimg() {
        return attrvalueimg;
    }

    public void setAttrvalueimg(String attrvalueimg) {
        this.attrvalueimg = attrvalueimg;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Integer getSortorder() {
        return sortorder;
    }

    public void setSortorder(Integer sortorder) {
        this.sortorder = sortorder;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }
}