
var NetWork={
	online:true,
	//监听网络状态
	networkListener:function(onCallBack,offCallBack){
		window.addEventListener('online',  function(){
		 	 console.log("网络链接了.....");
		 	 NetWork.online=true;
		 	 onCallBack();
		});
		window.addEventListener('offline', function(){
		 	 console.log("网络断开了.....");
		 	 NetWork.online=false;
		 	 offCallBack();
		});
	}
}



// 对象序列化
jQuery.prototype.serializeObject = function() {
		var data = new Object();
		$.each(this.serializeArray(), function(i, o) {
			if (!(o.name in data)) {
				data[o.name] = o.value;
			}
		});
		return data;
};

Date.prototype.format = function(format) {
	var o = {
		"M+" : this.getMonth() + 1,
		// month
		"d+" : this.getDate(),
		// day
		"h+" : this.getHours(),
		// hour
		"m+" : this.getMinutes(),
		// minute
		"s+" : this.getSeconds(),
		// second
		"q+" : Math.floor((this.getMonth() + 3) / 3),
		// quarter
		"S" : this.getMilliseconds()
	// millisecond
	};
	if (/(y+)/.test(format) || /(Y+)/.test(format)) {
		format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	}
	for ( var k in o) {
		if (new RegExp("(" + k + ")").test(format)) {
			format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
		}
	}
	return format;
};

//判断字符串已什么开头的
String.prototype.startWith = function(compareStr){
	return this.indexOf(compareStr) == 0;
}

function obj2string(o){ 
    var r=[]; 
    if(null==o)
    	return 'null';
    if(typeof o=="string"){ 
        return "\""+o.replace(/([\'\"\\])/g,"\\$1").replace(/(\n)/g,"\\n").replace(/(\r)/g,"\\r").replace(/(\t)/g,"\\t")+"\""; 
    } 
    if(typeof o=="object"){ 
        if(!o.sort){ 
            for(var i in o){ 
                r.push(i+":"+obj2string(o[i])); 
            } 
            if(!!document.all&&!/^\n?function\s*toString\(\)\s*\{\n?\s*\[native code\]\n?\s*\}\n?\s*$/.test(o.toString)){ 
                r.push("toString:"+o.toString.toString()); 
            } 
            r="{"+r.join()+"}"; 
        }else{ 
            for(var i=0;i<o.length;i++){ 
                r.push(obj2string(o[i])) 
            } 
            r="["+r.join()+"]"; 
        }  
        return r; 
    }  
    return o.toString(); 
} 










