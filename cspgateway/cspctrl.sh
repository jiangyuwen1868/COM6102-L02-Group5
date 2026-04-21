# *********************************************************************
# * A script to set classpath and path on Linux/Unix System.
# * create by pengdy
# * run this script.
# *****************************************************************
Start_cspGateway()
{

		# --spring.config.location=./config/  --logging.config=./config/logback-spring.xml
        nohup java -Dname=cspGateway -verbose:gc -Xms2048M -Xmx2048M -XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M -XX:+PrintGCDetails -XX:+PrintGCDateStamps -Xloggc:/home/anycsp/cspGateway/logs/gc.log.$$ -jar cspGateway-0.0.1-SNAPSHOT.jar --spring.config.location=./config/  --logging.config=./config/logback-spring.xml &   

        echo "cspGateway Server is started."
        >nohup.out
        tail -10f nohup.out
}

Stop_cspGateway()
{
        # kill $1 if it exists.
        PID_LIST=`ps -ef|grep cspGateway|grep -v grep|awk '{printf "%s ", $2}'`
        for PID in $PID_LIST
        do
          if kill -9 $PID
                 then
                        echo "Process $one($PID) was stopped at " `date`
                        echo "cspGateway Server is stoped."
          fi
        done
}

Status_cspGateway()
{
	PID_NUM=`ps -ef|grep "cspGateway"|grep -v grep|wc -l`
	if [ $PID_NUM -gt 0 ]
		then
		{
			echo "cspGateway server is started."
		}
		else
		{
			echo "cspGateway server is stoped."
		}
	fi
}


####----  main  ----####
#export JAVA_HOME=/usr/local/opt/jdk1.8.0_181
#export CLASSPATH=.:$JAVA_HOME/lib:$JAVA_HOME/jre/lib
cspGateway_ROOT=./
export cspGateway_ROOT
cd $cspGateway_ROOT

case "$1" in
'start')
        Start_cspGateway
        ;;
'stop')
        Stop_cspGateway
        ;;
'restart')
        Stop_cspGateway
        Start_cspGateway
        ;;
'status')
	Status_cspGateway
	;;        
*)
        echo "Usage: $0 {start|stop}"
        echo "  start : To start the application of cspGateway"
        echo "  stop  : To stop the application of cspGateway"
        echo "  restart  : To restart the application of cspGateway"
        echo "  status  : To view status the application of cspGateway"
        RETVAL=1
        ;;
esac

exit 0
