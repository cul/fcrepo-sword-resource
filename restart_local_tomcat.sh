


date=`date`

/Users/ap2972/my_programs/fedora_3_6_2/tomcat/bin/shutdown.sh
echo 'shutdown at: ' $date >> tomcat_restart.log

sleep 5

date=`date`
/Users/ap2972/my_programs/fedora_3_6_2/tomcat/bin/startup.sh
echo 'startup at: ' $date >> tomcat_restart.log


