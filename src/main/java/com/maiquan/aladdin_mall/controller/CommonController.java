package com.maiquan.aladdin_mall.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiniu.util.Auth;

/**
 * 用于各种公共功能
 * @author 黄永宗
 * @date 2016年4月6日 上午10:38:44
 */
@Controller
@RequestMapping("/common")
public class CommonController {

	@Value("${qiniu.space}")
	private String qiniuSpace;
	
	@Value("${qiniu.domain}")
	private String qiniuDomain;
	
	@Value("${qiniu.accessKey}")
	private String qiniuAccessKey;
	
	@Value("${qiniu.secretKey}")
	private String qiniuSecretKey;
	
	/**
	 * 用于获得七牛空间的上传凭证
	 */
	@RequestMapping("/uptoken")
	@ResponseBody
	public String getUpToken(){
		Auth auth = Auth.create(qiniuAccessKey, qiniuSecretKey);
		return auth.uploadToken(qiniuSpace);
	}
	
	/**
	 * 获得一张图片的访问地址  
	 * @param imgPath 在七牛空间上的地址
	 * @return 返回 加密后的地址
	 */
	@RequestMapping("/imgPath")
	@ResponseBody
	public String getImgPath(String imgPath){
		Auth auth = Auth.create(qiniuAccessKey, qiniuSecretKey);
		return auth.privateDownloadUrl(imgPath);
	}
	
}
