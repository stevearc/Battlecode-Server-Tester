/*
Author: Robert Hashemian
http://www.hashemian.com/

You can use this code in any manner so long as the author's
name, Web address and this disclaimer is kept intact.
********************************************************
Usage Sample:

<script language="JavaScript">
Seconds = 10;
BackColor = "palegreen";
ForeColor = "navy";
CountStepper = 1;
LeadingZero = true;
DisplayFormat = "%%D%% Days, %%H%% Hours, %%M%% Minutes, %%S%% Seconds.";
</script>
<script language="JavaScript" src="js/countdown.js"></script>
*/

function calcage(secs, num1, num2) {
  s = ((Math.floor(secs/num1))%num2).toString();
  if (LeadingZero && s.length < 2)
    s = "0" + s;
  return s;
}

function CountBack() {
  cntdwn = document.getElementById("cntdwn");
  if (cntdwn != null) {
    var secs = parseInt(cntdwn.name) + CountStepper;
    var days = calcage(secs,86400,100000);
    var hours = calcage(secs,3600,24);
    var minutes = calcage(secs,60,60);
    var seconds = calcage(secs,1,60);
    DisplayStr = "";
    if (days > 0)
      DisplayStr += (days + "d ");
    if (hours > 0 || days > 0)
      DisplayStr += (hours + "h ");
    if (minutes > 0 || hours > 0 || days > 0)
      DisplayStr += (minutes + "m ");
    if (seconds > 0 || minutes > 0 || hours > 0 || days > 0) 
      DisplayStr += (seconds + "s");

    cntdwn.innerHTML = DisplayStr;
    cntdwn.name = secs;
  }
  setTimeout("CountBack()", SetTimeOutPeriod);
}

if (typeof(BackColor)=="undefined")
  BackColor = "white";
if (typeof(ForeColor)=="undefined")
  ForeColor= "black";
if (typeof(Seconds)=="undefined")
  Seconds = 10;
if (typeof(FinishMessage)=="undefined")
  FinishMessage = "";
if (typeof(CountStepper)!="number")
  CountStepper = 1;
if (typeof(LeadingZero)=="undefined")
  LeadingZero = false;


CountStepper = Math.ceil(CountStepper);
var SetTimeOutPeriod = (Math.abs(CountStepper)-1)*1000 + 990;
CountBack();
