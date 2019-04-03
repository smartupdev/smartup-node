#!/bin/bash

usage() {
    echo "Usage:"
    echo "    -c [command] args: start / stop / restart / update"
    echo "    -b [branch] args: master / test / ..."
    echo "    -e [environment] args: test / prd / ..."
    exit -1
}

projectRoot=$(cd "$(dirname "$0")"; cd ..; pwd)

cmd=""
branch=""
env=""

# Param
while getopts "c:b:e:" opt
do
    case $opt in
    	c)
            cmd=$OPTARG
            ;;
    	p)
			branch=$OPTARG
			;;
        e)
            env=$OPTARG
            ;;
        *)
            echo "Unkonw argument."
            usage
            ;;
    esac
done


# check command
if [[ "$cmd" != "start" ]] && [[ "$cmd" != "stop" ]] && [[ "$cmd" != "restart" ]] && [[ "$cmd" != "update" ]]; then
    echo "Error command [$cmd]."
    usage
fi

# update
if [[ "$cmd" == "update" ]]; then
    echo "git updating ..."
    git fetch --all
    git reset --hard origin/${branch}
    exit 0
fi

# start
if [[ "$cmd" == "start" ]]; then
    echo "maven installing ..."
    cd ${projectRoot}
    mvn clean install -e -U -q -Dmaven.test.skip=true
    cp ${projectRoot}/target/smartup-node.jar ${projectRoot}/../bin/smartup-node.jar
    cd ${projectRoot}/../bin
    java -jar smartup-node.jar --spring.profiles.active=${env} >> /root/log/smartup-node.log 2>&1 &
    echo "App started. Log file path = /root/log/smartup-node.log"
fi

# stop
if [[ "$cmd" == "stop" ]]; then
    app_id=`ps -ef|grep smartup-node|grep -v "grep"|awk '{print $2}'`
    if [[ ${app_id} != "" ]]; then
        echo "Kill smartup-node id = [${app_id}]"
        kill -9 ${app_id}
    else
        echo "smartup-node not running"
    fi
    echo "smartup-node stopped"
    exit 0
fi

# restart
if [[ "$cmd" == "restart" ]]; then
    cd ${projectRoot}/bin
    sh deploy.sh -e ${env} -c stop
    sh deploy.sh -e ${env} -c start
fi




