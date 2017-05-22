<?php
header("Content-type:text/html;charset=utf-8");
$user_id = $_POST['user_id'];
$password = $_POST['password'];
$ip_address = $_SERVER["REMOTE_ADDR"];
$login_time=date("Y-m-d H:i:s");
#$time = this.getTime();


$con = mysql_connect("localhost","root","19951213hy");
if (!$con)
  {
  die('Could not connect: ' . mysql_error());
  }

mysql_select_db("sdn_competition_db", $con);

$result = mysql_query("SELECT * FROM groups
WHERE user_id = $user_id");

if(!$result) 
  die('invalid query: ' . mysql_error());

if(!($row = mysql_fetch_array($result)))
{
  $json_arr = array("user_id"=>$user_id,"vlan"=>-1);
  $json_obj = json_encode($json_arr);
  echo $json_obj;
}
else
{
  if($password != $row['password'])
  {
  	$json_arr = array("user_id"=>$user_id,"vlan"=>-2);
    $json_obj = json_encode($json_arr);
    echo $json_obj;
  }
  else
  {
  	$vlan = $row['vlan'];
  	$ins="INSERT INTO users (user_id, ip_address, online, vlan, login_time)
    VALUES
    ('$user_id','$ip_address','1','$vlan','$login_time') ON DUPLICATE KEY UPDATE user_id = $user_id, vlan = $vlan";
    if (!mysql_query($ins,$con))
      {
      die('Error: ' . mysql_error());
      }
    $json_arr = array("user_id"=>$user_id,"vlan"=>$vlan);
    $json_obj = json_encode($json_arr);
    echo $json_obj;
  }
  
}


/*$ins="INSERT INTO users (user_id, online, vlan, last_time)
VALUES
('$user_id','1','$vlan','time()')";

if (!mysql_query($ins,$con))
  {
  die('Error: ' . mysql_error());
  }*/
//echo "1 record added";
?>