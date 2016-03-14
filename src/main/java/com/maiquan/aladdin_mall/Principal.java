package com.maiquan.aladdin_mall;

import java.io.Serializable;

/**
 * 登陆用户身份信息
 * 
 * @author JSC
 *
 */
public class Principal implements Serializable {
	private static final long serialVersionUID = 5468975118212385151L;
	/** 在session中身份信息的key */
	public static final String ATTRIBUTE_KEY = "principal";

	/** 麦圈用户id */
	private String mqID;
	/** 微信openId */
	private String openId;

	public Principal(String mqID, String openId) {
		this.mqID = mqID;
		this.openId = openId;
	}

	public String getMqID() {
		return mqID;
	}

	public void setMqID(String mqID) {
		this.mqID = mqID;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

}
