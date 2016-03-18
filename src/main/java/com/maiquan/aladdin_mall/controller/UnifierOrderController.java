package com.maiquan.aladdin_mall.controller;

import org.springframework.web.bind.annotation.RequestMapping;

public class UnifierOrderController {

	@RequestMapping("/callback")
	public void callback(String code, String state){
		System.out.println(code);
	}
	
}
