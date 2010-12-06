function calcage(secs, num1, num2) {
  s = ((Math.floor(secs/num1))%num2).toString();
  return s;
}

// Provide a timer for real-time fake updates
function increment() {
  cntdwn = document.getElementById("cntdwn");
  if (cntdwn != null) {
    var secs = parseInt(cntdwn.name) + 1;
    var days = calcage(secs,86400,100000);
    var hours = calcage(secs,3600,24);
    var minutes = calcage(secs,60,60);
    var seconds = calcage(secs,1,60);
    formattedTime = "";
    if (days > 0)
      formattedTime += (days + "d ");
    if (hours > 0 || days > 0)
      formattedTime += (hours + "h ");
    if (minutes > 0 || hours > 0 || days > 0)
      formattedTime += (minutes + "m ");
    if (seconds > 0 || minutes > 0 || hours > 0 || days > 0) 
      formattedTime += (seconds + "s");

    cntdwn.innerHTML = formattedTime;
    cntdwn.name = secs;
  }
  setTimeout("increment()", 990);
}

increment();
