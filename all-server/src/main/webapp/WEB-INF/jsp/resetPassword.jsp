<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title>:: Reset your password ::</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta http-equiv="Content-Style-Type" content="text/css" />
<link href="/backend/css/password-style.css" rel="stylesheet" type="text/css" />
<style type="text/css">
<!--
.style6 {font-size: 2em}
.style7 {
	color: #009900;
	font-weight: bold;
} 
-->
</style>
<script type="text/javascript" src="/backend/js/jquery.js"></script>
<script type="text/javascript" src="/backend/js/jquery.validate.js"></script>
<script type="text/javascript" src="/backend/js/jquery.validate.password.js"></script>
<!--  
<script type="text/javascript" src="/backend/js/all.resetPassword.js"></script>
-->

<script type="text/javascript">
	$(document).ready(function() {
		$("#form1").validate({
			rules: {
				password: {
					required: true,
					minlength: 8,
					maxlength: 14,
					password: true
				},
				confirmedPassword: {
					required: true,
					minlength: 8,
					maxlength: 14,
					equalTo: "#password"
				}
			},
			messages: {
				password: {
					required: "Please provide a password",
					minlength: "Password minimum 8 letters long",
					maxlength: "Password maximum 14 letters long"
				},
				confirmedPassword: {
					required: "Please provide a password",
					minlength: "Password minimum 8 letters long",
					maxlength: "Password maximum 14 letters long",
					equalTo:   "Enter the same password as above"
				}
			}
		});

		// check if confirm password is still valid after password changed
		$("#password").blur(function() {
			$("#confirmedPassword").valid();
		});

		$("#password").valid();
	});

	
</script>
</head>

<body id="page2">
	<div class="tail-top">
      <div id="main">
         <div class="box">
         	<div class="border-top">
            	<div class="border-right">
               	<div class="border-bot">
                  	<div class="border-left">
                     	<div class="left-top-corner">
                        	<div class="right-top-corner">
                           	<div class="right-bot-corner">
                              	<div class="left-bot-corner">
                                 	<div class="bg">
                                       <div class="inside">
                                         <div class="wrapper">
                                             <!-- content -->
                                           <div id="content">
                                                <div id="menu">
                                                   <ul>
                                                      <li></li>
                                                      <li></li>
                                                      <li></li>
                                                      <li></li>
                                                      <span class="style6">Reset your password</span>
                                                   </ul>
                                               </div>
                                               <div class="inner">
                                                 <p>Welcome ${userFullName}, <br />
                                                     Enter a new password to protect your All account. Password should have 8-14 characters without spaces.<br />
                                                     <img class="title" alt="" src="/backend/images/2page-title1.gif" width="162" height="17" /><br />
                                                     New password<br />
                                                 </p>
                                                 <form id="form1" method="post" action="${submitUrl}">
                                                   
                                                     
                                                   <div class="fieldHolder">
	                                                <input name="password" type="password" class="password" id="password" value="" size="22" maxlength="22" />
                                                       <div class="password-meter">Level security : <span class="password-meter-message">&nbsp;</span></div>
                                                   </div>                                                   
                                                   <p>&nbsp;</p>
                                                   <label>
                                                </label>
                                                <div align="left"><br />
                                                </div>
                                                <div align="left">
                                                  <p><br />
                                                  Confirm your password                                                 </p>
                                                  <div class="fieldHolder">
	                                                
                                                    
                                                       <input name="confirmedPassword" type="password" class="password1" id="confirmedPassword" value="" size="22" maxlength="22" />
                                                    
                                                   </div>
                                                   <div align="left"><br />
                                                     <br />
                                                     <br />
                                                     
                                                     <a href="/backend/canceledPassword.jsp"><img src="/backend/images/btnCancel.gif" /></a>
                                                     &nbsp;&nbsp;&nbsp;&nbsp;<input type="image" src="/backend/images/btnSet.gif" alt="Submit button"/>
                                                   </div>
                                                </div>
                                                <input type="hidden" name="key" id="key" value="${key}" />
                                                 </form>
                                             </div> 
                                           </div>
                                            <!-- sidebar -->
                                         </div>
                                         <!-- footer -->
                                          <div id="footer"></div>
                                      </div>
                                    </div>
                                 </div>
                              </div>
                           </div>
                        </div>
                     </div>
                  </div>
               </div>
            </div>
         </div>
      </div>
   </div>
</body>
</html>