$(function(){
	 $(".btn").click(function(){
		var text = $(".search-text");
		if($(this).find(".img1").is(":visible")){
			$(this).find(".img1").hide();
			$(this).find(".img2").show();
		}else{
			$(this).find(".img2").hide();
			$(this).find(".img1").show();
		}
		if(text.val()==""){
			$(".btn").attr("href","search-fail.html");
		}else{
			$(".btn").attr("href","search-success.html");
		}
  });
  
  //排序
  $(".sort-1").click(function(){
		if($(this).hasClass("color")){
			$(this).removeClass("color");
		}else{
			$(this).addClass("color");
			$(".up > .img2").hide();
			$(".up > .img1").show();
			$(".down > .img1").show();
			$(".down > .img2").hide();
		}
	});

	$(".sort-2").click(function(){
		if($(".sort-1").hasClass("color")){
			$(".sort-1").removeClass("color");
		}
		if($(this).find(".up > .img2").is(":visible")){
			$(".up > .img2").hide();
			$(".up > .img1").show();
			$(".down > .img1").hide();
			$(".down > .img2").show();
		}else{
			$(".up > .img2").show();
			$(".up > .img1").hide();
			$(".down > .img1").show();
			$(".down > .img2").hide();
		}
	});
	//分类
	$(".select").click(function(){
			if($(this).find(".flag").is(":visible")){
				$(this).find(".flag").hide();
				$(this).removeClass("bg");
			}else{
				$(".select").removeClass("bg");
				$(".select").find(".flag").hide();
				$(this).find(".flag").show();
				$(this).addClass("bg");
			}
		})
		$(".select-2").click(function(){
			if($(this).find(".bg2").hasClass("bg2")){
				$(this).removeClass("bg2");
			}else{
				$(".select-2").removeClass("bg2");
				$(this).addClass("bg2");
			}
		})
});