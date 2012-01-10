<%@ taglib prefix="display" uri="http://displaytag.sf.net" %>
<html>
<head>
<title>All.com Users</title>
</head> 
<body>

<h2>Users DataBase</h2>
<display:table name="users">
	<display:column property="id"/>
	<display:column property="confirmed"/>
	<display:column property="firstName"/>
	<display:column property="lastName"/>
	<display:column property="gender"/>
	<display:column property="email"/> 
	<display:column property="birthday"/>
	<display:column property="idLocation"/>
	<display:column property="registrationDate"/>
	<display:column property="password"/>
	<display:column property="quote"/>
	<display:column property="version"/>
</display:table>

<h2>Contacts</h2>
<display:table name="contacts">
	<display:column property="idUser"/>
	<display:column property="idFriend"/>
	<display:column property="status"/>
</display:table>

<h2>DefaultContacts</h2>
<display:table name="defaultContacts">
	<display:column property="email"/>
</display:table>

<h2>Contact Requests</h2>
<display:table name="contactRequests">
	<display:column property="idRequester"/>
	<display:column property="idRequested"/>
	<display:column property="accepted"/>
	<display:column property="date"/>
</display:table>

</body>

</html>