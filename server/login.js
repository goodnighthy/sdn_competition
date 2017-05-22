function login(){
  var cont = $("input").serialize();
  $.ajax({
    url:'login.php',
    type:'post',
    dataType:'json',
    data:cont,
    success:function(data){
    console.log("receive success!");
    if(data.vlan == "-1"){
      var str = "login failed, the user ID is nonexistent!"
      $("#result").html(str);
    }
    else if(data.vlan == "-2"){
      var str = "login failed, the password is wrong!"
      $("#result").html(str);
    }
    else{
      var str = "login sucess, the vlan ID is: " + data.vlan;
      $("#result").html(str);
    }
    }
  });
 };