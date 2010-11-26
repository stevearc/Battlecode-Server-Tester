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

function query(method, url, params, callback) {
  xh = new XMLHttpRequest();
  if (callback != null) {
    xh.onreadystatechange = function() {
      if (xh.readyState==4 && xh.status==200) {
        callback(xh.responseText);
      }
    }
  }
  if (method == "POST") {
    xh.open(method, url, true);
    xh.send(params);
  } else if (method == "GET") {
    xh.open(method, url + "?" + params, true);
    xh.send();
  }

}
