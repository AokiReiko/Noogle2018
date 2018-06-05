<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String [] autocomplete = (String[]) request.getAttribute("autocomplete");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title> Noogle-<%= request.getAttribute("currentQuery") %> </title>
    <!-- bootstrap -->
    <link href="/search_engine/res/bootstrap/css/bootstrap.css" rel="stylesheet" />
    <link href="/search_engine/res/result.css" rel="stylesheet" />
    <link href="/search_engine/res/bootstrap/css/bootstrap-responsive.css" rel="stylesheet" />
    <script src="/search_engine/res/jquery.min.js"></script>
    <script src="/search_engine/res/bootstrap/js/bootstrap.min.js"></script>
	<script src="/search_engine/res/util.js"></script>
    <script src="/search_engine/res/bootstrap/js/bootstrap-typeahead.js"></script>
    <script src="/search_engine/res/bootstrap/js/material.min.js"></script>
	
    <!-- global font styles -->
    <style type="text/css">
        body,a,p,input,button{font-family:Arial,Verdana,"Microsoft YaHei",Georgia,Sans-serif}
        body{
      background-size: cover;
     }
    </style>
    <script type="text/javascript" >
    	window.onload = function() {
    		 $('.titleHref').bind('click', 
    					function() {
    				clickUrl($(this).context.href);
    			});
    	};
    	
    </script>
    
</head>

<body>
<%
	String currentQuery=(String) request.getParameter("query");
	int currentPage=(Integer) request.getAttribute("currentPage");
	int pageNum = (Integer) request.getAttribute("pageNum");
%>

<div class = "row-fluid" id="searchHead">

<div id="logo"><a href="/search_engine/search.jsp"><img src="/search_engine/res/logo0.png"  style="height:44px; width:100px; "></a></div>
<div id="search">
  <form id="searchForm" method="get" action="Server" >
     <div class="input-group">
      <input type="text" class="form-control searchInput" value="<%=currentQuery%>" name="query"/>
      <div class="input-group-btn">
        <button class="btn btn-default" style="border-left:0px;" type="submit"><i class="glyphicon glyphicon-search"></i>&nbsp</button>
      </div>
    </div>

  </form>
  
</div>
</div>
<div class = "container" id="searchAndResult">



<div class = "row-fluid" id="resultBody">
	
	<div class = "span8">
	<div  style="height:40px;">找到约..条结果</div>
  	<div id = "resultList">
  	<% 
		String [] suggestions = (String[]) request.getAttribute("suggestions");
	  	String[] paths=(String[]) request.getAttribute("paths");
	  	String[] titles = (String[] )request.getAttribute("titles");
	  	String[] descriptions = (String[]) request.getAttribute("descriptions");
	  	if(paths!=null && paths.length> 0) {
	  		for(int i=0;i<paths.length;i++){
	  		%>
	  		<div class="resultItem">
		  		<h3 class="titleHead"><a class="titleHref" href= "<%="http://"+paths[i].substring(paths[i].indexOf("news.tsinghua"))%>" target=" <%=i%>"><%= titles[i] %>
		  		</a></h3>
		  		<div class="urlDiv"><cite class="urlCite">http://news.tsinghua.edu.cn/publish/</cite></div>
		  		<span class="descriptions"><%=descriptions[i]%>
		  		</span>
			</div>
  		<%
  		}; 
  		%>  
	  	<%}else{ %>
	  		<div>no such result</div><%
	  	}; 
  	%>
  	</div>
  	
  	<div id="pagiDiv">
  	<ul class="pagination">
		<%if(currentPage>1){ %>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=currentPage-1%>">上一页</a></li>
		<%}; %>
		<%for (int i=Math.max(1,currentPage-5);i<currentPage;i++){%>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li class="disabled"><a href = ""><%=currentPage%></a></li>
		<%for (int i=currentPage+1;i<=Math.min(currentPage+9, pageNum);i++){ %>
			<li><a href="Server?query=<%=currentQuery%>&page=<%=i%>"><%=i%></a></li>
		<%}; %>
		    <li><a href="Server?query=<%=currentQuery%>&page=<%=currentPage+1%>">下一页</a></li>
	</ul>
	</div>
	</div>
	<div class="span4">
	<table class="table">
	<%if(suggestions!=null && suggestions.length>0){
	  		%><tr><td> <h4 class = "text-success">相关词汇：</h4><h5 > <%
		  	for(int i=0; i < suggestions.length;i++){ %>
		  		<a  href="/Searcher/servlet/Server?query=<%= suggestions[i] %>&Submit=Search">
		  		<%= suggestions[i] %> 
		  		</a>
	  			<br/>
		  	<% }
	  		%></h5></td></tr> <%
	  	}%>
	  </table>
	</div>
  </div>
</div>

</body>