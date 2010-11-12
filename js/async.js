function poll(callback, channel, lastheard) {
	xh = new XMLHttpRequest();
	xh.onreadystatechange = function() {
		if (xh.readyState==4 && xh.status==200) {
			callback(xh.responseText);
		}
	}
	xh.open("GET", "comet?channel="+channel+"&lastheard="+lastheard, true);
	xh.send();
}
