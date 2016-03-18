package com.maiquan.aladdin_mall.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.maiquan.aladdin_comment.domain.Comment;
import com.maiquan.aladdin_comment.service.ICommentService;
import com.maiquan.aladdin_product.service.IProductService;


/**
 * 管理收货地址
 * @author 黄永宗
 * @date 2016年2月18日 下午3:07:51
 */
@Controller
@RequestMapping("/comments")
public class CommentController {
	
	@Autowired
	private IProductService productService; 
	
	@Autowired
	private ICommentService commentService;
	
	private String[] names = new String[]{"jimi","halei","jiu"};
	
	@RequestMapping("/commentlist")
	public String getCommentList(Integer productID,Integer pageIndex,Integer pageSize,Model model){
		
		
		//获得共有多少条未删除评论
		int commentCount = commentService.getCountNoDeletedByProductID(productID,UUID.randomUUID().toString());
		model.addAttribute("commentCount", commentCount);
		
		//获得评论列表
		List<Map<String,Object>> comments = new ArrayList<Map<String,Object>>();
		
		if(commentCount>0){
			
			List<Comment> commentList = commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,UUID.randomUUID().toString());
			System.out.println("评论个数:"+commentList.size());
			//遍历每一条评论
			for(int i=0;i<commentList.size();i++){
				
				Map<String,Object> map = new HashMap<String,Object>();
				//User u = userService.getUserByMqID(commentList.get(i).getMqID());
				map.put("userHeadImg","images/img/head_portrait.png"/*u.getHeadImg()*/);
				map.put("userName","zara"/*u.getUserName()*/); 
				Comment comment = commentList.get(i);
				//orderProdService.getSkuStrByOrderProdID(comment.getOrderProdID());
				map.put("skuStr", "购买尺码: XL    颜色: 橄榄绿");
				map.put("comment",commentList.get(i));
				//加入规格
				comments.add(map);
				
			}
			
		}
		
		model.addAttribute("comments", comments);
		
		return "comments";
		
	}
	
	@RequestMapping("/uptoken")
	@ResponseBody
	public String getUpToken(){
		return "6EZmwsqQYeHvlaA44_LwiBAePez-rjpOv4jwg4t4:UENCitOcA8BGmi7roBWnC86g-W8=:IntcInNjb3BlXCI6XCJhbGFkZGluOnN1bmZsb3dlci5qcGdcIixcImRlYWRsaW5lXCI6MTQ1MTQ5MTIwMCxcInJldHVybkJvZHlcIjpcIntcIm5hbWVcIjokKGZuYW1lKSxcInNpemVcIjokKGZzaXplKSxcIndcIjokKGltYWdlSW5mby53aWR0aCksXCJoXCI6JChpbWFnZUluZm8uaGVpZ2h0KSxcImhhc2hcIjokKGV0YWcpfVwifSI=";
	}
	
	/**
	 * 根据产品id 及 分页信息 查找商品评论
	 * @param productID
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	@RequestMapping("/page_comments")
	@ResponseBody
	public List<Comment> getComments(Integer productID,Integer pageIndex,Integer pageSize){
		System.out.println(productID+" "+pageIndex+" "+pageSize);
		List<Comment> comments = commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,UUID.randomUUID().toString());
		System.out.println(comments.size());
		return commentService.getCommentNoDeletedList(productID,pageIndex,pageSize,UUID.randomUUID().toString());
	}
	
	/**
	 * 
	 */
	@RequestMapping("/product_detail")
	public String productDetail(){
		return "productdetail";
	}
}
