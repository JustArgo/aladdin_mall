package com.maiquan.aladdin_mall.controller;

import java.util.ArrayList; 
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.maiquan.aladdin_receadd.domain.Address;
import com.maiquan.aladdin_receadd.domain.ReceiveAddress;
import com.maiquan.aladdin_receadd.service.IAddressService;
import com.maiquan.aladdin_receadd.service.IManageReceAddService;


/**
 * 管理收货地址
 * @author 黄永宗
 * @date 2016年2月18日 下午3:07:51
 */
@Controller
public class ReceiveAddressController {
	
	
	@Autowired
	private IManageReceAddService manageReceAddService;
	
	@Autowired
	private IAddressService addressService;
	
	/**
	 * 新增用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/add_rece_address")
	private String add(ReceiveAddress receiveAddress,Model model) throws Exception{
		
		System.out.println(receiveAddress.getIsDefault());
		manageReceAddService.addAddress(receiveAddress,UUID.randomUUID().toString());
		
		//返回地址列表页面
		return "redirect:manage_rece_add";
	}
	
	/**
	 * 删除用户收货地址
	 * @param ID
	 * @param model
	 * @return
	 */
	@RequestMapping("/del_rece_add")
	private String del(int ID,Model model){
		
		manageReceAddService.deleteAddress(ID,UUID.randomUUID().toString());

		return "redirect:manage_rece_add";
	}
	
	/**
	 * 更新用户收货地址
	 * @param receiveAddress
	 * @param model
	 * @return
	 */
	@RequestMapping("/update_rece_add")
	private String update(ReceiveAddress receiveAddress,Model model){
		
		manageReceAddService.updateAddress(receiveAddress,UUID.randomUUID().toString());
		
		return "redirect:manage_rece_add";
	}
	
	/**
	 * 管理用户收货地址
	 * @param model
	 * @return
	 */
	@RequestMapping("/manage_rece_add")
	private String manage(String mqID, Model model){
		
		mqID = "2";
		List<ReceiveAddress> adds = manageReceAddService.listUsableAddress(mqID,UUID.randomUUID().toString());
		for(int i=0;i<adds.size();i++){
			ReceiveAddress address = adds.get(i);
			String province = addressService.getAddress(address.getProvinceID(),UUID.randomUUID().toString()).getName();
			String city     = addressService.getAddress(address.getCityID(),UUID.randomUUID().toString()).getName();
			String district = address.getDistrictID()!=null?addressService.getAddress(address.getDistrictID(),UUID.randomUUID().toString()).getName():"";
			
			adds.get(i).setAddress(province+city+district+address.getAddress());
			
		}
		model.addAttribute("adds", adds);
		
		return "receadd/manage";
	}
	
	/**
	 * 编辑用户收货地址 可能是更新也可能是新增
	 * @param ID
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/edit_rece_add")
	private String edit(Integer ID,Model model) throws Exception{
		
		List<Address> provinces = addressService.getSubAddress(0,UUID.randomUUID().toString());
		model.addAttribute("provinces",provinces);
		List<Address> cities = new ArrayList<Address>();
		List<Address> districts = new ArrayList<Address>();
		
		if(ID!=null){
			
			ReceiveAddress address = manageReceAddService.getAddress(ID,UUID.randomUUID().toString());
			model.addAttribute("add",address);
			cities = addressService.getSubAddress(address.getProvinceID(),UUID.randomUUID().toString());
			districts = addressService.getSubAddress(address.getCityID(),UUID.randomUUID().toString());
			
		}else{
			cities = addressService.getSubAddress(10,UUID.randomUUID().toString());
			districts = addressService.getSubAddress(1010,UUID.randomUUID().toString());
		}
		model.addAttribute("cities",cities);
		model.addAttribute("districts",districts);
		return "receadd/edit";
		
	}
	
	@RequestMapping("/setUserDefaultAddress")
	@ResponseBody
	private int setUserDefaultAddress(Integer id,String isDefault){
		
		return manageReceAddService.setUserDefaultAddress(id, isDefault,UUID.randomUUID().toString());
		
	}
	
	@RequestMapping("/getAddressByPid")
	@ResponseBody
	private List<Address> getByPid(Integer pid){
		
		List<Address> addresses = new ArrayList<Address>();
		addresses = addressService.getSubAddress(pid,UUID.randomUUID().toString());
		return addresses;
		
	}
	
}
