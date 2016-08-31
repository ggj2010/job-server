<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>
<html>
<head>
    <title>正在跳转</title>
</head>
<body>
正在跳转至管理页面...
<script>
    window.location.href = "monitor/jobList"
</script>
</body>
</html>
