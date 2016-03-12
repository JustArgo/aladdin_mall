package com.aladdin.user.entity;

import java.util.Date;

/**
 * 微信用户
 * 
 * @author JSC
 *
 */
public class WxUser {
	private Integer id;

	private String mqid;

	private String openid;

	private String nickname;

	private Byte sex;

	private String language;

	private String city;

	private String province;

	private String country;

	private String headimgurl;

	private Integer subscribetime;

	private String unionid;

	private String remark;

	private Byte subscribe;

	private Integer groupid;

	private Date addtime;

	private Date updatetime;

	private String qrticket;

	private Integer qrexpireseconds;

	private Integer qrcreatetime;

	private String privilege;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMqid() {
		return mqid;
	}

	public void setMqid(String mqid) {
		this.mqid = mqid;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Byte getSex() {
		return sex;
	}

	public void setSex(Byte sex) {
		this.sex = sex;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	public Integer getSubscribetime() {
		return subscribetime;
	}

	public void setSubscribetime(Integer subscribetime) {
		this.subscribetime = subscribetime;
	}

	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Byte getSubscribe() {
		return subscribe;
	}

	public void setSubscribe(Byte subscribe) {
		this.subscribe = subscribe;
	}

	public Integer getGroupid() {
		return groupid;
	}

	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}

	public Date getAddtime() {
		return addtime;
	}

	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}

	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public String getQrticket() {
		return qrticket;
	}

	public void setQrticket(String qrticket) {
		this.qrticket = qrticket;
	}

	public Integer getQrexpireseconds() {
		return qrexpireseconds;
	}

	public void setQrexpireseconds(Integer qrexpireseconds) {
		this.qrexpireseconds = qrexpireseconds;
	}

	public Integer getQrcreatetime() {
		return qrcreatetime;
	}

	public void setQrcreatetime(Integer qrcreatetime) {
		this.qrcreatetime = qrcreatetime;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}
}