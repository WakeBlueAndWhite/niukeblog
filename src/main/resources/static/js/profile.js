$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				if (data.code == 0){
					//关注成功 刷新页面
					window.location.reload();
				}else {
					alert(data.msg);
				}
			}
		);
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (data) {
				data = $.parseJSON(data);
				//取消成功 刷新页面
				if(data.code == 0) {
					window.location.reload();
				} else {
					alert(data.msg);
				}
			}
		);
	}
}