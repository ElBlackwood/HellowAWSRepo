<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
<head>
	<title>Home</title>
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
</head>
<body>
<h1>
	Hello ${name}!  
</h1>

<P>  The time on the server is ${serverTime}. </P>

<form action="${pageContext.request.contextPath}/formHandler" method="POST">
	<input name="input"></input>
	<input type="submit" value="Firerockets!" />
</form>

<p>Form submission: ${submitData}</p>

</body>

<script src="https://code.jquery.com/jquery-2.1.4.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
</html>
