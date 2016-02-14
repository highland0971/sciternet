#!/bin/sh

MODEL=NETGEAR6100
VER=735950


MASTER_SRV=$(uci get sciternet.system.server)
DNS_PROVIDER=$MASTER_SRV
TOKEN="$(uci get sciternet.system.token)"
GFW_LIST='/etc/dnsmasq.d/gfwlist.conf'
CN_LIST='/etc/ignore.list'
LOG='/var/log/sciternet.log'
UPDATE_SCRIPT='/tmp/sciternet.update'
RESULT_FILE='/tmp/sciternet.status'
touch $LOG
WAN_NAME=$(route|grep default|awk '{print $NF}')
MAC=$(ifconfig|grep $WAN_NAME|awk '{print $NF}'|awk -F: 'BEGIN{a=""}{for(i=1;i<=NF;i++) a = a $i ;print a}')



start()
{
	startv2 $1
}

log()
{
	echo $0 $(date) $1 >> $LOG && echo $1
	return 0
}

startv2()
{
	minRtt=10000
	selectIP=""

	echo "BOOTING" > $RESULT_FILE

        startResult="OK"

        err1="$0 $(date) Invalied token, service starting abort!"
        err2="$0 $(date) Service disabled, service starting abort!"

	log "Starting...."

        /etc/init.d/shadowsocks stop

	rest=$( wget -q --timeout 5 -t 3 -O- http://$MASTER_SRV/request_avail_server/$TOKEN)
	log "Get available server list: $rest"

	if echo $rest|egrep -o '([0-9]+\.*){4}' ; then
		for ip in  $(echo $rest|egrep -o '([0-9]+\.*){4}'); do
			log "Ping $ip"
			rtt=$(ping -qc 5 $ip |egrep -o '[0-9]+(\.[0-9]+)*/[0-9]+(\.[0-9]+)*/[0-9]+(\.[0-9]+)*' |awk -F/ '{print $2}'|awk -F. '{print $1}')
			[ $rtt -lt $minRtt ] && minRtt=$rtt && selectIP=$ip
		done
		if [ -z $selectIP ];then
			echo "FAILURE" > $RESULT_FILE
			log "No available server candidate selected"
			return 2
		fi
	else
		echo "FAILURE" > $RESULT_FILE
		log "No available server candidate to test"
		return 2
	fi
	
	log "Selected server is $selectIP,min rtt is $minRtt"
	startv2phs2 "$1" "$selectIP"
}

startv2phs2()
{
        err1="$0 $(date) Invalied token, service starting abort!"
        err2="$0 $(date) Service disabled, service starting abort!"
	
	wgetcmd="wget -q --timeout 5 -t 3 -O- http://$MASTER_SRV/request_dynamic_router/$TOKEN/$2/$MAC"
	log "$wgetcmd"
	rest=$($wgetcmd)
        #rest=$( wget -q --timeout 5 -t 3 -O- http://$MASTER_SRV/request_dynamic_router/$TOKEN/$2/$MAC)

        log "Get remote server info: $rest"

        for o in $rest; do
                option=$(echo $o|awk -F: '{print $1}')
                value=$(echo $o|awk -F: '{print $2}')
                if [ "$option" = "ip" ]; then
                        server_ip=$value
                fi
                if [ "$option" = "port" ]; then
                        server_port=$value
                fi
                if [ "$option" = "method" ]; then
                        encrypt_method=$value
                fi
        done

        if [ $server_ip ] && [ $server_port ] && [ $encrypt_method ]; then
                log "Remote server info parser complete."
        else
                log "Found invalid remote server configuration,starting aborted."
                uci set shadowsocks.@shadowsocks[0].enable=0
                echo "FAILURE" > $RESULT_FILE
                return 2
        fi

        log "$0 $(date) Get APNIC CHINA IP LIST...."
        if [ -s $CN_LIST ] && [ "$1" != "fresh_cnlist" ]; then
                log "APNIC CHINA IP list found in /etc,download escaped."
        else
                wget -t 10 --read-timeout 600 -O- 'http://ftp.apnic.net/apnic/stats/apnic/delegated-apnic-latest' | awk -F\| '/CN\|ipv4/ { printf("%s/%d\n", $4, 32-log($5)/log(2)) }' > $CN_LIST
		log "Complete."
        fi

        log "Config shadowsocks..."

        if [ -s $CN_LIST ]; then
                uci set shadowsocks.@shadowsocks[0].ignore_list=$CN_LIST
        else
                startResult="ALL_ROUTED"
        fi

        if [ -n "$server_ip" ]; then
                log "configuring shadowsocks to $server_ip:$server_port by $encrypt_method"
                uci set shadowsocks.@shadowsocks[0].server=$server_ip
                uci set shadowsocks.@shadowsocks[0].server_port=$server_port
                uci set shadowsocks.@shadowsocks[0].encrypt_method=$encrypt_method
                uci set shadowsocks.@shadowsocks[0].password=$TOKEN
                uci set shadowsocks.@shadowsocks[0].enable=$(uci get sciternet.system.auto_start)
                uci set shadowsocks.@shadowsocks[0].lan_ac_mode=0
                uci set shadowsocks.@shadowsocks[0].tunnel_enable=0
                uci set shadowsocks.@shadowsocks[0].udp_mode=0
        fi

        uci commit shadowsocks

        log "Preparing DNSMASQ..."

	log "Download GFW domain list..."

        mkdir /etc/dnsmasq.d && echo 'conf-dir=/etc/dnsmasq.d' >> /etc/dnsmasq.conf
        wget  -q -t 3 -O $GFW_LIST http://$DNS_PROVIDER/request_gfwlist/$TOKEN
        log " Complete."
        if [ -s $GFW_LIST ]; then
                if [ "$(cat $GFW_LIST)" != "TOKEN_ERR" ] && [ "$(cat $GFW_LIST)" != "None" ]; then
                        log "Rename GFW Domain name resolver"
                        sed -i "s|^\(server.*\)/[^/]*$|\1/$DNS_PROVIDER#5353|" $GFW_LIST
                else
                        [ "$(cat $GFW_LIST)" = "TOKEN_ERR" ] &&  echo $0 $(date) "TOKEN ERROR" >> $LOG
                        [ "$(cat $GFW_LIST)" = "None" ] && echo $0 $(date) "No valid GFW list" >> $LOG
                        rm $GFW_LIST
                        return 3
                fi
        else
                log "Fail to fetch GFW List file !"
                return 4
        fi

        log "Starting shadowsocks.."
        /etc/init.d/shadowsocks start

        log "Restart DNSMASQ"

        /etc/init.d/dnsmasq stop
        /etc/init.d/dnsmasq start

        log "Sciternet start sequence completed."
        echo $startResult > $RESULT_FILE
        exit 0
}


startv1()
{
	echo "BOOTING" > $RESULT_FILE

        startResult="OK"

        err1="$0 $(date) Invalied token, service starting abort!"
        err2="$0 $(date) Service disabled, service starting abort!"

        echo $0 $(date) Starting.... >> $LOG

        /etc/init.d/shadowsocks stop

        rest=$( wget -q --timeout 5 -t 3 -O- http://$MASTER_SRV/request_avail_server/$TOKEN)

        str="Get remote server info: $rest"
        echo $0 $(date) $str >> $LOG && echo $str

        for o in $rest; do
                option=$(echo $o|awk -F: '{print $1}')
                value=$(echo $o|awk -F: '{print $2}')
                if [ "$option" = "ip" ]; then
                        server_ip=$value
                fi
                if [ "$option" = "port" ]; then
                        server_port=$value
                fi
                if [ "$option" = "method" ]; then
                        encrypt_method=$value
                fi
        done

        if [ $server_ip ] && [ $server_port ] && [ $encrypt_method ]; then
                str="Remote server info parser complete."
                echo $0 $(date) $str >> $LOG && echo $str
        else
                str="Found invalid remote server configuration,starting aborted."
                echo $0 $(date) $str >> $LOG && echo $str
                uci set shadowsocks.@shadowsocks[0].enable=0
		echo "FAILURE" > $RESULT_FILE
                return 2
        fi

        str="$0 $(date) Get APNIC CHINA IP LIST...."
        echo $str >> $LOG && echo $str
        if [ -s $CN_LIST ] && [ "$1" != "fresh_cnlist" ]; then
                str="$0 $(date) APNIC CHINA IP list found in /etc,download escaped."
                echo $str >> $LOG && echo $str
        else
                wget -t 10 --read-timeout 600 -O- 'http://ftp.apnic.net/apnic/stats/apnic/delegated-apnic-latest' | awk -F\| '/CN\|ipv4/ { printf("%s/%d\n", $4, 32-log($5)/log(2)) }' > $CN_LIST
                echo $0 $(date) " Complete." >> $LOG
        fi

        str="$0 $(date) Config shadowsocks..."
        echo $str >> $LOG && echo $str


        if [ -s $CN_LIST ]; then
                uci set shadowsocks.@shadowsocks[0].ignore_list=$CN_LIST
        else
                startResult="ALL_ROUTED"
        fi

        if [ -n "$server_ip" ]; then
                echo configuring shadowsocks to $server_ip:$server_port by $encrypt_method
                uci set shadowsocks.@shadowsocks[0].server=$server_ip
                uci set shadowsocks.@shadowsocks[0].server_port=$server_port
                uci set shadowsocks.@shadowsocks[0].encrypt_method=$encrypt_method
                uci set shadowsocks.@shadowsocks[0].password=$TOKEN
                uci set shadowsocks.@shadowsocks[0].enable=$(uci get sciternet.system.auto_start)
                uci set shadowsocks.@shadowsocks[0].lan_ac_mode=0
                uci set shadowsocks.@shadowsocks[0].tunnel_enable=0
                uci set shadowsocks.@shadowsocks[0].udp_mode=0
        fi

        uci commit shadowsocks

        str="$0 $(date) Preparing DNSMASQ..."
        echo $str >> $LOG && echo $str

        str="$0 $(date) Download GFW domain list..."
        echo $str >> $LOG && echo $str

        mkdir /etc/dnsmasq.d && echo 'conf-dir=/etc/dnsmasq.d' >> /etc/dnsmasq.conf
        wget  -q -t 3 -O $GFW_LIST http://$DNS_PROVIDER/request_gfwlist/$TOKEN
        echo $0 $(date) " Complete." >> $LOG
        if [ -s $GFW_LIST ]; then
                if [ "$(cat $GFW_LIST)" != "TOKEN_ERR" ] && [ "$(cat $GFW_LIST)" != "None" ]; then
                        str="$0 $(date) rename GFW Domain name resolver"
                        echo $str >> $LOG && echo $str
                        sed -i "s|^\(server.*\)/[^/]*$|\1/$DNS_PROVIDER#5353|" $GFW_LIST
                else
                        [ "$(cat $GFW_LIST)" = "TOKEN_ERR" ] &&  echo $0 $(date) "TOKEN ERROR" >> $LOG
                        [ "$(cat $GFW_LIST)" = "None" ] && echo $0 $(date) "No valid GFW list" >> $LOG
                        rm $GFW_LIST
                        return 3
                fi
        else
                str="$0 $(date) Fail to fetch GFW List file !"
                echo $str >> $LOG && echo $str
                return 4
        fi

        str="$0 $(date) starting shadowsocks.."
        echo $str >> $LOG && echo $str
        /etc/init.d/shadowsocks start

        str="$0 $(date) Restart DNSMASQ"
        echo $str >> $LOG && echo $str

        /etc/init.d/dnsmasq stop
        /etc/init.d/dnsmasq start

        str="$0 $(date) Sciternet start sequence completed."
        echo $str >> $LOG && echo $str
        echo $startResult > $RESULT_FILE
        exit 0
}

stop()
{

        /etc/init.d/shadowsocks stop
        [ -s $GFW_LIST ] && rm $GFW_LIST && /etc/init.d/dnsmasq stop && /etc/init.d/dnsmasq start
	echo "SHUTDOWN" > $RESULT_FILE

        str="$0 $(date) Sciternet stopped."
        echo $str >> $LOG && echo $str

        exit 0
}

new_ver_check()
{
        str="$0 $(date) Checking newest upgrade candidate version..."
        echo $str >> $LOG
        wget -t 3 -O /tmp/newver http://$MASTER_SRV/update_check/$MODEL/$VER && echo $(cat /tmp/newver) && echo $0 $(date) $(cat /tmp/newver) >> $LOG
}

upgrade()
{
        new_ver_check
        retCode=0
        targetVersion=$(cat /tmp/newver)
        str="$0 $(date) Starting Upgrade..."
        echo $str >> $LOG && echo $str
        if [ $targetVersion -gt $VER ]; then
                mkdir /tmp/sciternet_update
                cd /tmp/sciternet_update
                str="$0 $(date) Fetching update script..."
                echo $str >> $LOG && echo $str
                wget -t 3 ftp://$MASTER_SRV//$MODEL/$targetVersion/upgrade.sh && chmod +x upgrade.sh
                if [ -s upgrade.sh ]; then
                        str="$0 $(date) Fetching completed."
                        echo $str >> $LOG && echo $str
                        str="$0 $(date) Executing upgrade script."
                        echo $str >> $LOG && echo $str
                        ./upgrade.sh
                else
                        str="$0 $(date) Fetching failed."
                        echo $str >> $LOG && echo $str
                        retCode=1
                fi
        else
                str="$0 $(date) Already newest firmware,upgrade canceled."
                echo $str >> $LOG && echo $str
        fi

        str="$0 $(date) Upgrade complete..."
        echo $str >> $LOG && echo $str
        return $retCode
}

get_version()
{
        echo $VER
}

case $1 in
"start")
        if [ "$2" = "fresh_cnlist" ]; then
                start "fresh_cnlist"
        else
                start
        fi
;;
"stop")
        stop
;;
"upgrade")
        upgrade
;;
"new_ver_check")
        new_ver_check
;;
"ver")
        get_version
;;
esac

