function poll(callback, channel) {
	xh = new XMLHttpRequest();
	xh.onreadystatechange = function() {
		if (xh.readyState==4 && xh.status==200) {
			callback(xh.responseText);
		}
	}
	xh.open("GET", "comet?channel="+channel, true);
	xh.send();
}
