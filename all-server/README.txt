#How deploy backend on development
mvn clean install -Pdevelopment

on backend development server
copy backend.war on /opt/toBeDeploy

on deployer server
cd /opt/deployments/capistrano/

cap shutdown_tomcat:backend
cap check_status:backend
cap prepare_web_project:backend
cap start_tomcat:backend

Note: When you're trying to test the recovery password process you have to 
change: http://www.all.com/resetPassword/e4494f1260693f749965fd52a53ced1e 
to 
http://192.168.2.152:8080/backend/services/reset/e4494f1260693f749965fd52a53ced1e
or something similar.