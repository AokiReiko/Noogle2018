<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
System.out.println(request.getCharacterEncoding());
response.setCharacterEncoding("utf-8");
System.out.println(response.getCharacterEncoding());
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
System.out.println(path);
System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>AtTsinghua</title>
	<!-- bootstrap -->
    <link href="res/bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="res/bootstrap/css/bootstrap.min.css" rel="stylesheet" />
    <link href="res/bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <script src="res/jquery.min.js"></script>
	<script src="res/bootstrap/js/bootstrap.min.js"></script>
	<script src="res/util.js"></script>
	<script src="res/bootstrap/js/material.min.js"></script>
	<script type="text/javascript" >
    	window.onload = function() {
    		$.material.init();
    		$('#autocomplete').hide();
    		$('#queryInput').attr('autocomplete', 'off');
    		$('#queryInput').bind('input', 
    				function() {
    			console.log(1);
    			autocomplete($(this).val());
    		});
    	};
    	
    </script>
	<link href="res/search.css" rel="stylesheet" />
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      background-size: cover;
     }
    </style>
    
</head>
<body>
	<center>
	<div style="height:30px;margin-top:60px" >
  	</div>
  	<div>
  	<h1 height="220"><img src="res/logo0.png" width="400" height="220"></h1>
  	<form id="searchForm" method="get" action="servlet/Server">
	  <div id="inputDiv">
	 	 <input name="query" type="text"  id="queryInput" size="50" placeholder="在Noogle上搜索清华新闻">
	     <button type="submit" class="btn btn-info btn-lg"><span class="glyphicon glyphicon-search">Search</button>
	    <div id="autocomplete" class="panel panel-default autocomp"/>
	  
	  </div>
	  
	 
	  
	</form>
   	</div>
   </center>
</body>
</html>
