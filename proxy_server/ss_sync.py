#!/usr/bin/python
import json
import urllib
import ConfigParser
import subprocess
import re
import logging
import getopt,sys
import time
import os
import BaseHTTPServer


CONFIG_FILE="/etc/ss_sync.conf"
LOG_FILE="/var/log/ss_sync.log"
IFCONFIG='/sbin/ifconfig'
IPTABLES='/sbin/iptables'
IPTABLES_SAVE='/sbin/iptables-save'

ALLOW_RECOVERY_PER_UNIT = 2
TIMING_UNIT = 'hour'
ALLOW_PASS_PER_UNIT = 1
CLIENT_PORT_RANGE = range(9000,9100)
PAGING_CMD_PORT = 8080

config = None
global logger

class AutoConfigParser(ConfigParser.RawConfigParser):
	LOCK_PATTERN = '{}.lock'
	config_path = None
	logger = logging.getLogger('ss_sync')

	def read(self,filenames):
		if filenames and type(filenames) == str:
			self.config_path = filenames
		ConfigParser.RawConfigParser.read(self,filenames)
	
	def commit(self):
		if self.config_path:
			while os.path.exists(self.LOCK_PATTERN.format(self.config_path)):
				time.sleep(1)
				if not os.path.exists(self.LOCK_PATTERN.format(self.config_path)):
					break
			with open(self.config_path, 'wb') as configfile:
				with open(self.LOCK_PATTERN.format(self.config_path),'wb') as fp:
					fp.write('{}'.format(time.time()))
				self.write(configfile)
				os.remove(self.LOCK_PATTERN.format(self.config_path))
		else:
			assert IOError,"No config file path to write"

def report_avail_port():
	raw_result = subprocess.check_output('netstat -an|grep tcp',shell=True)
	ports = re.findall('\d\.\d\.\d\.\d:(\d+)',raw_result)
	logger.debug('ports {} in use detected.'.format(ports))
	for port in ports:
		try:
			CLIENT_PORT_RANGE.remove(int(port))
		except ValueError:
			pass
	for port in config.sections():
		if re.match('\d+',port):
			try:
				CLIENT_PORT_RANGE.remove(int(port))
			except ValueError:
				pass
	return CLIENT_PORT_RANGE
		

class SimpleHttpHandler(BaseHTTPServer.BaseHTTPRequestHandler):

        def do_GET(self):
		logger.debug('{} request url {}.'.format(self.command,self.path))
		try:
			if self.path == '/report_avail_port':
				result = report_avail_port()
				self.send_response(200)
				self.send_header("Content-type", "text/html")
				self.end_headers()
				self.wfile.write(json.dumps(result))
			elif  self.path == '/sync_clients':
				sync_ss_client()
				self.send_response(200)
				self.send_header("Content-type", "text/html")
                                self.end_headers()
				self.wfile.write(json.dumps('OK'))
			else:
				self.send_response(404)
				self.send_header("Content-type", "text/html")
				self.end_headers()
				self.wfile.write(json.dumps([]))
				logger.warn('Unexpected {} request {}'.format(self.command,self.path))
		except:
			logger.exception('Unexpected error happened during process {}.'.format(self.path))
			self.send_response(500)
			self.send_header("Content-type", "text/html")
			self.end_headers()
			self.wfile.write(json.dumps([]))

		logger.debug('{} request process completed.'.format(self.path))

def fetch_remote_clients_config():
	url = 'http://{}/request_ss_clients/mine'.format(config.get('MainServer','ipaddr'))
	logger.debug('Fetching remote ss client config: {}'.format(url))
	remote = urllib.urlopen(url)
	result = remote.read()
	logger.debug('result: {}'.format(result))
	return json.loads(result)
		
def gether_local_ss_client():
	clients = {}
	logger.debug('Gethering local ss client instance from api...')
	raw_result = subprocess.check_output('echo "ping"|nc -Uu -q 1 {}'.format(config.get('LocalServer','socketpath')),shell=True)
	raw_result = re.findall('[0-9]+:[0-9]+',raw_result.replace('"',''))
	for item in raw_result:
		port,count = item.split(':')
		clients[port] = int(count)
	logger.debug('result: {}'.format(clients))
	return clients

def __get_port_usage(port):
	clients = {}
        logger.debug('Gethering port {} usage from api...'.format(port))
	try:
		raw_result = subprocess.check_output('echo "ping"|nc -Uu -q 1 {}'.format(config.get('LocalServer','socketpath')),shell=True)
        	raw_result = re.findall('[0-9]+:[0-9]+',raw_result.replace('"',''))
        	for item in raw_result:
                	dport,count = item.split(':')
			if dport == port:
				return count
		else:
			return None

	except:
		logger.exception('Unexpected error occured during get port usage from api')
		return None

def gether_local_ss_client_from_ps():
	clients = {}
	logger.debug('Gethering local ss client instance from ps command...')
	#raw_result = subprocess.check_output("ps -ef|grep -oE 'shadowsocks_[0-9]+'|awk -F_ '{print $2}'",shell=True)
	raw_result = subprocess.check_output("ps -ax",shell=True)
	for item in re.findall('shadowsocks_([0-9]+)',raw_result):
		clients[item] = item
	logger.debug('result: {}'.format(clients))
	return clients

def iptable_port_monitor_setup(remote_cfg):
	'''shall be called by cron'''
        logger.debug('Gethering iptable moniter rule for local ss client')
        inspect_rule = '{} -A INPUT -d {}/32 -p tcp -m tcp --dport {} -m limit --limit {}/{} --limit-burst {} -m comment --comment PORT_REQUEST_LOG -j LOG --log-prefix "PORT REQUEST " --log-level info'

        inspect_ports = []
	try:
        	raw_result = subprocess.check_output("{}|grep 'PORT_REQUEST_LOG'".format(IPTABLES_SAVE),shell=True)
                inspect_ports.extend(re.findall('--dport (\d+)',raw_result))
        	logger.debug('Total {} port monitered, {}.'.format(len(inspect_ports),inspect_ports))
	except subprocess.CalledProcessError:
		logger.debug("No iptables rule found for PORT_REQUEST_LOG")
        if remote_cfg:
                for port in remote_cfg:
                        try:
                                inspect_ports.index(port)
                        except ValueError as ie:
                                try:
                                        subprocess.check_call(inspect_rule.format(IPTABLES,SERVER_IP,port,ALLOW_RECOVERY_PER_UNIT,TIMING_UNIT,ALLOW_PASS_PER_UNIT),shell=True)
                                        logger.info('Port {} added to iptables inspect list.'.format(port))
                                except subprocess.CalledProcessError,error:
                                        logger.exception('Faild to insert inspect rule for port {}:{}'.format(port,error.cmd))
        else:
                logger.info('Empty client port config from remote')

def iptables_port_monitor_install(port):
	inspect_rule = '{} -A INPUT -d {}/32 -p tcp -m tcp --dport {} -m limit --limit {}/{} --limit-burst {} -m comment --comment PORT_REQUEST_LOG -j LOG --log-prefix "PORT REQUEST " --log-level info'
	try:
		exist_check = subprocess.check_output("{} -L INPUT --numeric --line-numbers|grep 'PORT_REQUEST_LOG'|grep 'dpt:{}'".format(IPTABLES,port),shell=True)
		logger.debug('Port {} already monitored.'.format(port))
	except subprocess.CalledProcessError,e:
		try:
			subprocess.check_call(inspect_rule.format(IPTABLES,SERVER_IP,port,ALLOW_RECOVERY_PER_UNIT,TIMING_UNIT,ALLOW_PASS_PER_UNIT),shell=True)
			logger.debug('Port {} added to iptables monitor chain.'.format(port))
		except subprocess.CalledProcessError,e:
			logger.exception('Faild to insert inspect rule for port {}:{}'.format(port,e.cmd))
	except:
		logger.exception('Unexpected error occured during install iptables monitor rule for port {}.'.format(port))
		

def iptables_port_monitor_uninstall(port):
	try:
		raw_result = subprocess.check_output("{} -L INPUT --numeric --line-numbers|grep 'PORT_REQUEST_LOG'|grep 'dpt:{}'".format(IPTABLES,port),shell=True)
		subprocess.check_output("{} -D INPUT {}".format(IPTABLES,raw_result.split()[0]))
	except subprocess.CalledProcessError,e:
		logger.warn('No iptables monitor rule found for port {}'.format(port))
		logger.warn('Related command:{}'.format(e.cmd))
	except:
		logger.exception('Unexpected error found when uninstall monitor rule for port {}.'.format(port))

def paging_port_monitor_install(port):
	inspect_rule = '{} -A INPUT -d {}/32 -p udp -m udp --dport {}  -m comment --comment PAGING_REQUEST_LOG -j LOG --log-prefix PAGING_REQUEST --log-level info'
	try:
		logger.debug('Try setup paging port monitor.')
		if port:
	                exist_check = subprocess.check_output("{} -L INPUT --numeric --line-numbers|grep 'PAGING_REQUEST_LOG'|grep 'dpt:{}'".format(IPTABLES,port),shell=True)
	                logger.debug('Paging port {} already monitored.'.format(port))
		else:
			logger.error('Paging port number not specificed.')
        except subprocess.CalledProcessError,e:
                try:
			subprocess.check_call(inspect_rule.format(IPTABLES,SERVER_IP,port),shell=True)
			logger.debug('Port {} added to paging monitor chain.'.format(port))
		except subprocess.CalledProcessError,e:
                        logger.exception('Faild to insert inspect rule for port {}:{}'.format(port,e.cmd))
	except:
		logger.exception('Unexpected error occured during install iptables monitor rule for port {}.'.format(port))


def __port_shut_down(port):
	logger.info('Trying to shutdown port {}.'.format(port))
	cmd = "echo 'remove: {\"server_port\": %s}' | nc -Uuq 1 %s"%(port,config.get('LocalServer','socketpath'))
	try:
		subprocess.check_call(cmd,shell=True)
	except subprocess.CalledProcessError:
		logger.exception('Port {} shutdown FAILED!'.format(port))
	except:
		logger.exception('Unexpected error occured during shutdown port {}.'.format(port))

def __port_resetup(port):
        try:
                token = config.get(port,'token')
                alive = config.getboolean(port,'alive')
        except ConfigParser.NoSectionError:
                logger.warn('Port {} not found in local config,resetup check failed.'.format(port))
        except ConfigParser.NoOptionError:
                logger.error('No token or alive tag found for port {},resetup check failed.'.format(port))
        except:
                logger.exception('Unexpected error found during querying port resetup config.')
        cmd = "echo 'add: {\"server_port\":%s, \"password\":\"%s\"}' | nc -Uuq 1 %s"%(port,token,config.get('LocalServer','socketpath'))
        if not alive:
		logger.debug('Trying reSetup port {}'.format(port))
                try:
                        subprocess.check_call(cmd,shell = True)
			clients = gether_local_ss_client_from_ps()
			if clients.get(port,None):
				logger.debug('Port {} restarted.'.format(port))
				config.set(port,'LastTimeStamp',time.time())
				config.commit()
			else:
				logger.warn('Port {} restart failed.'.format(port))
                except subprocess.CalledProcessError,e:
			logger.error('Failed to restart port {}, with cmd :{}'.format(port,e.cmd))
                except:
                        logger.exception('Unexpected error occured during port {} resetup subprocess')
        else:
                logger.debug("Port {} reported its alive status.".format(port))

def sync_ss_client():
	try:
		logger.debug('Start sync ss client with controller server...')
		remote_cfgs = fetch_remote_clients_config()
		local_instances =  gether_local_ss_client()
		local_instances_ps = gether_local_ss_client_from_ps()
		for port in remote_cfgs:
			try:
				logger.debug('Check local port {} existence.'.format(port))
				cmd = "echo 'add: {\"server_port\":%s, \"password\":\"%s\"}' | nc -Uuq 1 %s"%(port,remote_cfgs[port]['token'],config.get('LocalServer','socketpath'))
				if local_instances.get(port,None) == None: #BECAREFULL some client with 0 usage return will also pass if not local_instances.get(port,None) test
					logger.debug('Port {} not found on local server'.format(port))
					try:
						if config.getboolean(port,'alive'):
							'''Actually no client shall be setup here,in case unexpected error accured casused local config mis-sync with remote server'''
							logger.debug("Starting MIS-SYNC local ss on port {}.".format(port))
							subprocess.call(cmd,shell=True)
							config.set(port,'LastTimeStamp',time.time())
                                			config.set(port,'LastDataUsage',0)
							config.set(port,'token',remote_cfgs[port]['token'])
						else:
							logger.debug('Remote config for port {} found,but local activity tag is idle,setup escaped.'.format(port))
					except ConfigParser.NoSectionError:
						logger.debug("Adding new local ss client on port {}.".format(port))
                                                subprocess.call(cmd,shell=True)
						config.add_section(port)
		                                config.set(port,'LastTimeStamp',time.time())
		                                config.set(port,'LastDataUsage',0)
		                                config.set(port,'alive',True)
						config.set(port,'token',remote_cfgs[port]['token'])
					finally:
						config.commit()
						iptables_port_monitor_install(port)
			except:
				logger.exception("Error occured on command [{}].".format(cmd))

		#iptable_port_monitor_setup(remote_cfgs)

		for port in local_instances:
			'''TODO remove remote removed client info from local config file and iptables rules'''
			try:
				if remote_cfgs.get(port,None) == None:
					logger.debug("Port {} not found on remote configuration, need to be killed and report to {}".format(port,config.get('MainServer','ipaddr')))
					report_ss_client_usage(port,local_instances[port])
					__port_shut_down(port)
					iptables_port_monitor_uninstall(port)
					try:
						config.remove_section(port)
					except ConfigParser.NoSectionError:
						logger.error('Port {} not found in local config file ,remove failed.'.format(port))
					finally:
						config.commit()
					
			except:
				logger.exception("Error occured on command [{}].".format(cmd))
			try:
				if not local_instances_ps.get(port,None):
					logger.warn("Dead port {} found,try to reestablish".format(port))
					__port_shut_down(port)
					cmd = "echo 'add: {\"server_port\":%s, \"password\":\"%s\"}' | nc -Uuq 1 %s"%(port,remote_cfgs[port]['token'],config.get('LocalServer','socketpath'))
					subprocess.call(cmd,shell=True)
					if not gether_local_ss_client_from_ps().get(port,None):
						logger.error("Dead port {} reestablish failed.".format(port))

			except:
				logger.exception("Error occured when reestablish proxy server for port {}.".format(port))
		
		for port in config.sections():
			if re.match('\d+',port) and remote_cfgs.get(port,None) == None:
				config.remove_section(port)
				config.commit()
				logger.debug('Unused local client port {} config removed'.format(port))
	except:
		logger.exception('Failed to sync ss client with remote server {}'.format(config.get('MainServer','ipaddr')))

def report_ss_client_usage(port,usage):
	url = 'http://{}/report_client_usage/{}/{}'.format(config.get('MainServer','ipaddr'),port,usage)
	logger.debug('Report client usage for port {} data {} to {}.'.format(port,usage,config.get('MainServer','ipaddr')))
	try:
		remote = urllib.urlopen(url)
       		response = remote.read()
		if response == 'OK':
			return True
		else:
			return False
	except:
		logger.exception("Error occured when report client usage to {},url {}.".format(config.get('MainServer','ipaddr'),url))
		return False

def sync_client_usage():
	logger.debug("Starting sync local ss client usage to controller server.")
	local_instances =  gether_local_ss_client()
	for port in local_instances:
		try:
			report_ss_client_usage(port,local_instances[port])
		except:
			logger.exception("Error occured on report usage of port {}.".format(port))

def idle_port_proceed(deltaTime = 1800):
        try:
		logger.debug('Idle port check started')
                local_instances =  gether_local_ss_client()
                currentTime = time.time()
                for port in local_instances:
			logger.debug('Port {} check'.format(port))
			try:
                        	lastTimestamp = config.getfloat(port,'LastTimeStamp')
                                if local_instances[port] == config.getint(port,'LastDataUsage'):
					if  currentTime - lastTimestamp > deltaTime:
                                        	'''IDLE PORT DETECTED'''
	                                       	logger.info('Idle port {} detected,to be kill,idle time threshold {}s.'.format(port,deltaTime))
        	                                config.set(port,'alive',False)
						report_ss_client_usage(port,local_instances[port])
						__port_shut_down(port)
					else:
						logger.debug('Idle port {} detected, {}s later will be removed.'.format(port,lastTimestamp+deltaTime - currentTime))
				else:
					config.set(port,'LastDataUsage',local_instances[port])
					'''ONLY IF THERE IS AN USAGE DIFFERENCE THEN UPDATE THE TIMESTAMP'''
					config.set(port,'LastTimeStamp',currentTime)
					logger.debug('Port {} pass.'.format(port))
        		except ConfigParser.NoSectionError:
				config.add_section(port)
				config.set(port,'LastTimeStamp',currentTime)
				config.set(port,'LastDataUsage',local_instances[port])
				config.set(port,'alive',True)			
				logger.debug('Port {} usage monitor added.'.format(port))
			except:
				logger.exception('Unexpected error during uninstall idle client port {}'.format(port))
			finally:
				config.commit()
        except:
                logger.exception('Unexpected error during uninstall idle client port')
	finally:
		logger.debug('Idle port check completed')

def sync_ss_client_and_usage():
	sync_ss_client()
	sync_client_usage()

def local_ip_detect():
	try:
		raw_result = subprocess.check_output('{} {}'.format(IFCONFIG,'venet0:0'),shell=True)
        	ip = re.findall('addr:(\d+\.\d+\.\d+\.\d+)',raw_result)
        	if ip and ip[0]:
                	logger.debug('Local public ip detected {}'.format(ip[0]))
			return ip[0]
		else:
			logger.error('Failed to detect public ip.')
			return None
	except subprocess.CalledProcessError,e:
		logger.exception("Unexpected error occured during detect local server ip")
		logger.error(e.cmd)
	except:
		logger.exception("Unexpect error happend during local server ip.")

def service_start_helper():
	logger.debug('Start shadowsocks master deamon')

	cmd = '/usr/local/bin/ss-manager --manager-address /tmp/ss_manager.socket --executable /usr/local/bin/ss-server -m rc4-md5 -s {} &'
	if SERVER_IP:
		try:
			subprocess.check_call(cmd.format(SERVER_IP),shell=True)
			try:
				logger.debug('Try setup watch dog for client proxy request')
				try:
					path = config.get('LocalServer','watchdogpath')
					cmd = "tail -f {}".format(path)
					fp = open(path,'wb')
					fp.write('Started on {}'.format(time.time()))
					fp.close()
					subprocess.check_call('chown syslog:adm {}'.format(path),shell=True)
				except ConfigParser.NoOptionError:
					path = '/proc/kmsg'
					cmd = "cat {}".format(path)
				cmd +=  """ | awk '/PORT REQUEST/{for(i=1;i<=NF;i++) if ($i~/DPT=[0-9]*/) system("/usr/local/bin/ss_sync.py --" $i)} /PAGING_REQUEST/{system("/usr/local/bin/ss_sync.py --paging")}' &"""
				logger.debug(cmd)
				subprocess.check_call(cmd,shell=True)
				logger.debug('Done.')
				paging_port_monitor_install(config.get('LocalServer','pingport'))
				logger.debug('Shadowsocks master deamon started')
			except subprocess.CalledProcessError,e:
		                logger.error('Failed to setup watch dog with cmd:{}'.format(e.cmd))
				sys.exit(2)
			except:
				logger.exception("Unexpected error occured during setup ss client activity watchdog")
				sys.exit(2)
		except subprocess.CalledProcessError as error:
			logger.exception('Shadowsocks master deamon start failed,with return code {}'.format(error.returncode))
			sys.exit(2)
	else:
		logger.error('Failed to get local public ip address,server start aborted')
		sys.exit(2)

def paging_response():
	logger.debug('Paging request detected')
	server_addr = ('',PAGING_CMD_PORT)
	try:
		httpd = BaseHTTPServer.HTTPServer(server_addr,SimpleHttpHandler)
		httpd.handle_request()
	except:
		logger.exception('Unexpected error occured when handle paging request.')


if __name__ == "__main__":

	#config = ConfigParser.SafeConfigParser()
	config = AutoConfigParser()
	config.read(CONFIG_FILE)
	logging.basicConfig(format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',filename=LOG_FILE,level=logging.DEBUG)
        logger = logging.getLogger('ss_sync')
	logger.debug('SS_SYNC script started with {}'.format(sys.argv))

	SERVER_IP = local_ip_detect()

	try:
		opts, args = getopt.getopt(sys.argv[1:], "",['sync','start','paging','DPT='])
	except getopt.GetoptError as err:
		logger.exception('Error accured during parsing opt')
		sys.exit(2)
	if opts:
		for o,v in opts:
			if o == '--sync':
				sync_ss_client()
			        sync_client_usage()
				idle_port_proceed()
			elif o == '--start':
				service_start_helper()
				sync_ss_client()
			elif o == '--DPT':
				logger.debug('Port {} input request detected.'.format(v))
				__port_resetup(v)
			elif o == '--paging':
				paging_response()
			else:
				logger.debug('Unrecongised command option {}.'.format(o))
	else:
		logger.debug('Not command specified,nothing will happen.')
