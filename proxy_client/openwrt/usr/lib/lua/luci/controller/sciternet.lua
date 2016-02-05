--[[
openwrt-dist-luci: SciTernet
]]--

fs = require("nixio.fs")

module("luci.controller.sciternet", package.seeall)

local init_conif = [[
config sciternet 'system'
        option server '104.129.0.243'
        option dnsProvider '104.129.0.243'
        option auto_upgrade '1'
        option email 'your.email@anywhere.com'
        option password 'None'
        option token 'None'
        option auto_start '1'

config sciternet 'ui'
        option auto_refresh '1'
]]

function index()
        if not nixio.fs.access("/etc/config/sciternet") then
                fs.writefile("/etc/config/sciternet",init_conif)
        end
	entry({"admin", "wow"}, template("sciternet/wow"), _("World outside the Wall"),20).dependent = false
        entry({"admin", "services", "sciternet"}, cbi("sciternet"), _("SciTernet"), 32).dependent = true
	entry({"admin", "services", "sciternet","domainverify"}, call("_domain_verify")).dependent = false
	entry({"admin", "services", "sciternet","urlverify"}, call("_url_verify")).dependent = false
	entry({"admin", "services", "sciternet","domain_hack_test"},call("_try_hack_domain")).dependent = false
	entry({"admin", "services", "sciternet","apply_custom_domain_hack"},call("_apply_custom_domain_hack")).dependent = false
	entry({"admin", "services", "sciternet","service_watch_dog"},call("_report_service_status")).dependent = false
	entry({"admin", "services", "sciternet","stop_service"},call("_stop_service")).dependent = false
	entry({"admin", "services", "sciternet","start_service"},call("_start_service")).dependent = false

end

function _start_service()
	luci.sys.call("/etc/init.d/sciternet stop")
	luci.sys.call("/etc/init.d/sciternet start")
	luci.http.write_json("OK")
end

function _stop_service()
        luci.sys.call("/etc/init.d/sciternet stop")
        luci.http.write_json("OK")
end

function _report_service_status()
	RESULT_FILE = '/tmp/sciternet.status'

	result_explain = "Sciternet is in unknown mode ~_~||"

	if luci.sys.call("pidof ss-redir >/dev/null") == 0 then
        	local f = io.open(RESULT_FILE,'r')
        	if f then
                	result_code = f:read()
                	f:close()
        	end
        	if result_code == "OK" then
                	result_explain = "Sciternet is running, fast and secure :-)"
        	elseif result_code == "ALL_ROUTED" then
                	result_explain = "Sciternet is running, but in global route mode.<br>Which may slowing the speed and cause high charging data usage!"
        	elseif result_code == "BOOTING" then
                	result_explain = "Sciternet is booting up, please wait a little moment :-)"
		elseif result_code == "FAILURE" then
                        result_explain = "Sciternet booting failed, please try again later :-("
        	end
	else
        	local f = io.open(RESULT_FILE,'r')
        	if f then
                	result_code = f:read()
                	f:close()
                	if result_code == "BOOTING" then
                        	result_explain = "Sciternet is booting up, please wait a little moment :-)"
                	elseif result_code == "SHUTDOWN" then
                        	result_explain = "SciTernet was shutdown"
                	elseif result_code == "FAILURE" then
                        	result_explain = "Sciternet booting failed, please try again later :-("
                	end
        	else
                	result_explain = "SciTernet was shutdown"
        	end

	end
	luci.http.write_json(result_explain)
end


function _apply_custom_domain_hack()
	domainString = luci.http.formvalue("domains")
	domains = {}
	nixio.fs.writefile("/tmp/apply_hacklist.debug",domainString)
	if domainString then
		for domain in string.gmatch(domainString,",?([%w%.%-]+)") do
			domains[#domains + 1 ] = domain
		end

		_compose_dnsmasq_domain_list("custom_domain_list.conf",domains,'append')
		luci.http.write("OK")
	else
		luci.http.write("Nothing to hack.")
	end
end

function _compose_dnsmasq_domain_list(listName,domains,mode)
	dnsProvider = string.sub(luci.sys.exec("uci get sciternet.system.dnsProvider"),1,-2)
	hackStrings = ""
	for i,domain in ipairs(domains) do
		hackStrings = hackStrings .. "server=/" .. domain .. "/" .. dnsProvider .. "#5353\n"
	end
	if mode == 'append' and luci.sys.call('cat /etc/dnsmasq.d/'..listName) == 0 then
		f = assert(io.open('/etc/dnsmasq.d/'..listName,'r'))
		for line in f:lines() do
			hackStrings = hackStrings .. line .. '\n'
		end
		f:close()
	end
	nixio.fs.writefile('/etc/dnsmasq.d/'..listName,hackStrings)
end

function _try_hack_domain()
	dnsProvider = string.sub(luci.sys.exec("uci get sciternet.system.dnsProvider"),1,-2)
	domainString = luci.http.formvalue("domains")
	nixio.fs.writefile("/tmp/hacklist.debug",domainString)
	hackStrings = ""
	for domain in string.gmatch(domainString,",?([%w%.%-]+)") do
		nixio.fs.writefile("/tmp/hacklist.debug.last",domain)
		hackStrings = hackStrings .. "server=/" .. domain .. "/" .. dnsProvider .. "#5353\n"
	end
	nixio.fs.writefile("/etc/dnsmasq.d/hacklist.conf",hackStrings)
	luci.sys.exec("/etc/init.d/dnsmasq restart")
end
	
function _domain_verify()
	tmpFile = "/tmp/verify.url"
	result = {} 
	target = luci.http.formvalue("target")
	nixio.fs.writefile("/tmp/debug.url.main",target)
	subDomain = string.match(target,"https?://([%w%.%-]+)")
	result[subDomain] = {target}

	if luci.sys.call("wget -q --timeout 5 -t 3 --spider --no-check-certificate "	.. target) == 0 then
		cmd = 'wget --timeout 5 -t 3 -qO- --no-check-certificate '..target..' | grep -o -E "((http|ftp|https)://)([a-zA-Z0-9\._-]+\.[a-zA-Z]{2,6})(:[0-9]{1,4})*(/[a-zA-Z0-9\&%_\./-~-]*)?"'
		tryUrls = luci.sys.exec(cmd)
		for url in string.gmatch(tryUrls,"https?://[%w%.%-&%?/=_]*") do
			subDomain = string.match(url,"https?://([%w%.%-]+)")
			if subDomain then
				if result[subDomain] then
					result[subDomain][#result[subDomain]+1] = url
				else
					result[subDomain] = {url}
				end
			end
		end
		nixio.fs.writefile("/tmp/debug.url.child",tryUrls)
	end
	luci.http.write_json(result)
end

function _url_verify()
	target = luci.http.formvalue("urlTarget")
	subCmd = "wget -q --timeout 5 -t 1 --spider --no-check-certificate " .. target
	subDomain = string.match(target,"https?://([%w%.%-]+)")
	retCode = luci.sys.call(subCmd)
	if retCode == 0 or retCode == 8 then
		luci.http.write(subDomain)
	else
		luci.dispatcher.error404(subDomain)
		nixio.fs.writefile("/tmp/debug.err."..subDomain,retCode.."\n"..target)
	end
end
