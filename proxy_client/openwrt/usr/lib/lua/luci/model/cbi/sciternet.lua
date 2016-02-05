--[[
openwrt-dist-luci: SciTernet
]]--

local m, s, o, e, a
local fs = require("nixio.fs")
local sys = require("luci.sys")
local http = require("luci.http")
local uci = require("luci.model.uci")

local token = nil

local MAIN_SCRIPT = "/usr/bin/sciternet_sync.sh"
local RESULT_FILE='/tmp/sciternet.status'

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
		end
	else
		result_explain = "SciTernet was shutdown"
        end

end

m = Map("sciternet", translate("SciTernet"), translate(result_explain))


fs.writefile("/tmp/debug.load","Map create complete")

s = m:section(NamedSection, "system", translate("System"))
s.anonymous = true

s:tab("account",translate("Account"))
o = s:taboption("account",Value, "email", translate("Email"))
o.rmempty = true
o.validate = function(self, value, sectionid)
        local user = value
        local pwd = self.map:formvalue("cbid."..self.map.config.."."..sectionid..".password")
        local server = self.map:get("system", "server")
        fs.writefile("/tmp/debug.validate","email:"..user.." pwd:"..pwd)
        token = sys.httpget("http://"..server.."/request_token/"..user.."/"..pwd)
        fs.writefile("/tmp/debug.validate","Token"..token)
        if token == "None" or token == nil then
                return nil, translate("Invalied email or password,apply failed")
        else
                return value
        end
end

o = s:taboption("account",Value, "password", translate("Password"))
o.password = true
o.rmempty = true
o.validate = function(self, value, sectionid)
        local user = self.map:formvalue("cbid."..self.map.config.."."..sectionid..".email")
        local pwd = value
        local server = self.map:get("system", "server")
        fs.writefile("/tmp/debug.validate","email:"..user.." pwd:"..pwd)
        token = sys.httpget("http://"..server.."/request_token/"..user.."/"..pwd)
        fs.writefile("/tmp/debug.validate","Token"..token)
        if token == "None" or token == nil then
                return nil, translate("Invalied email or password,apply failed")
        else
                return value
        end
end

o = s:taboption("account",Value, "service_switch")
o.template = "sciternet/service_switch"



s:tab("run",translate("Basic setting"))
o = s:taboption("run",Flag, "auto_start", translate("Auto start on boot"))
o.default = true
o.rmempty = false

s:tab("upgrade",translate("Upgrade setting"))
cur_ver = luci.sys.exec(MAIN_SCRIPT.." ver")
o = s:taboption("upgrade",ListValue, "", translate("Current version"))
o:value(cur_ver, cur_ver)
function o.validate(self,value)
        return value
end

fs.writefile("/tmp/debug.load","Section upgrade option current version create complete")

o = s:taboption("upgrade",ListValue, "", translate("Available version"))
function o.validate(self,value)
        return value
end

newest_ver = luci.sys.exec(MAIN_SCRIPT.." new_ver_check")
fs.writefile("/tmp/debug.load","Section upgrade option target version query complete")

if #newest_ver >0 then
        o:value(newest_ver, newest_ver)
else
        o:value("NA", translate("Not available"))
end

fs.writefile("/tmp/debug.load","Section upgrade option target version create complete")


o = s:taboption("upgrade",Flag, "auto_upgrade",translate("Auto upgrade enable"))
o.default = 1

fs.writefile("/tmp/debug.load","Section upgrade create complete")


s = m:section(NamedSection, "ui", translate("User Guide"))
s.anonymous = true

s:tab("data_usage",translate("Data Usage"))
o = s:taboption("data_usage",Value, "", translate("Daily Usage"))
o.template = "sciternet/usage"

s:tab("net_diag",translate("Network Diagnosite"))
o = s:taboption("net_diag",Value, "", translate("Diag web site"))
o.template = "sciternet/http_diag"


m.on_before_commit = function(self)
        fs.writefile("/tmp/debug.bef_cmt","nothing")
end

m.on_after_commit = function(self)
        fs.writefile("/tmp/debug.aft_cmt","nothing")
        if not token then
                m:set("sciternet","system","auto_start","0")
                fs.writefile("/tmp/debug.aft_cmt","None token")
        else
                manual_uci = 'uci set sciternet.system.token="'..token..'"'
                luci.sys.call(manual_uci)
                luci.sys.call("uci commit sciternet")
                m:set("sciternet","system","token",token)
                fs.writefile("/tmp/debug.aft_cmt","Token:"..token)
        end
end

m.on_before_apply = function(self)
        fs.writefile("/tmp/debug.bef_apl","nothing")
end

m.on_after_apply = function(self)
        fs.writefile("/tmp/debug.aft_apl","nothing")
        --luci.sys.call("/etc/init.d/sciternet enable")
end

fs.writefile("/tmp/debug.load","Map load complete")

return m

