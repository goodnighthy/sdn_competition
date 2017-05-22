<?php

$con = mysql_connect("localhost", "root", "19951213hy");
if(!$con)
die('Could not connect: ' . mysql_error());

if(!mysql_query("CREATE DATABASE sdn_competition_db", $con))
	echo "Error creating database: " . mysql_error();

mysql_select_db("sdn_competition_db", $con);
$user = "CREATE TABLE users
(
	user_id int NOT NULL,
	PRIMARY KEY(user_id),
	#mac_address varchar(32) NOT NULL,
	#PRIMARY KEY(mac_address),
	#port int,
	ip_address varchar(16),
	online boolean,
	vlan int,
	#login_time timestamp DEFAULT CURRENT_TIMESTAMP
	login_time timestamp
)";
if(!mysql_query($user, $con))
	echo "Error creating table users: " . mysql_error();
$group = "CREATE TABLE groups
(
	vlan int,
	user_id int NOT NULL,
	PRIMARY KEY(user_id),
	password varchar(32)
)";
if(!mysql_query($group, $con))
	echo "Error creating table groups: " . mysql_error();

mysql_close($con);
?>