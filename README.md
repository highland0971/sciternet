# sciternet
Scientific Interneting Solution

:main_server
To run as master server for central control with proxy server and client router

:proxy_server
To run on proxy server for shadowsocks server and client sync with master server

How To:
Prapare for proxy_server
1. Install dnsmasq netcat
2. Configure DnsMasq
#Configure the DNSMASQ for DNS resolve
echo "port=5353" >> /etc/dnsmasq.conf
echo "server=8.8.8.8" >> /etc/dnsmasq.conf
echo "server=8.8.4.4" >> /etc/dnsmasq.conf
/etc/init.d/dnsmasq restart or enable dnsmasq by systemctl enable && restart

3. Deploy ss_sync.py
#Add following script into /etc/rc.local to start shadowsocks manager
/usr/local/bin/ss_sync.py --start 

4. Configure crontab
crontab -e
*/1 * * * * /usr/local/bin/ss_sync.py

5. Configure rsyslog
#In kernal log to file enabled server,setup /etc/rsyslog.d/ss_sync.conf :
:msg, contains, "PORT REQUEST" -/var/log/ss_sync.watchdog   
