To deploy UltraPeer to development

mvn clean assembly:assembly -Pdevelopment

put the zip to the ultraPeers servers on /opt/toBeDeploy

Go to deployer server on /opt/deployments/capistrano

cap prepareUltrapeersDirectory
cap shutdownUltrapeers
cap symbolicLinkToUltrapeers
cap startMonitorUltrapeers
cap startUltrapeers
