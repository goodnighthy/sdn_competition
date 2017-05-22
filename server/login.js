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
      var str = "invalid username !"
      $("#result").html(str);
    }
    else if(data.vlan == "-2"){
      var str = "wrong password !"
      $("#result").html(str);
    }
    else{
      var str = "login success !";
      $("#result").html(str);
    }
    }
  });
 };