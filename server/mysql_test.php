<?php
$link = mysql_connect("localhost", "root", "123");
if(!$link)
die('Could not connect: ' . mysql_error());
else
echo "Mysql 配置正确!";
mysql_close($link);
?>