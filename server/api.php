<?php
header("Content-type:text/html;charset=utf-8");
$ip_address = $_POST['ip_address'];
$actino = $_POST['action'];
#$ip_address = '127.0.0.1';
#$action = "inform";
  
switch($action)
{
  case"query";
    $con = mysql_connect("localhost","root","19951213hy");
    if (!$con)
      {
        die('Could not connect: ' . mysql_error());
      }

    mysql_select_db("sdn_competition_db", $con);

    $result = mysql_query("SELECT * FROM users
    WHERE ip_address = '$ip_address'");

    if(!$result) 
      die('invalid query1: ' . mysql_error());

    if(!($row = mysql_fetch_array($result)))
    {
      $json_arr = array("vlan"=>-1);
      $json_obj = json_encode($json_arr);
      echo $json_obj;
    }
    else
    {
      $vlan = $row["vlan"];
      $json_arr = array("vlan"=>$vlan);
      $json_obj = json_encode($json_arr);
      echo $json_obj;
    }
    break;

  case"inform";
    $con = mysql_connect("localhost","root","19951213hy");
    if (!$con)
      {
        die('Could not connect: ' . mysql_error());
      }

    mysql_select_db("sdn_competition_db", $con);

    $result = mysql_query("DELETE FROM users
    WHERE ip_address = '$ip_address'");

    if(!$result) 
      die('invalid query2: ' . mysql_error());

    $json_arr = array("success"=>1);
    $json_obj = json_encode($json_arr);
    echo $json_obj;
    break;
}
?>