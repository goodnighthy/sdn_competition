import httplib
import json
 
class StaticEntryPusher(object):
 
    def __init__(self, server):
        self.server = server
 
    def get(self, data):
        ret = self.rest_call({}, 'GET')
        return json.loads(ret[2])
 
    def set(self, data):
        ret = self.rest_call(data, 'POST')
        return ret[0] == 200
 
    def remove(self, objtype, data):
        ret = self.rest_call(data, 'DELETE')
        return ret[0] == 200
 
    def rest_call(self, data, action):
        path = '/wm/staticentrypusher/json'
        headers = {
            'Content-type': 'application/json',
            'Accept': 'application/json',
            }
        body = json.dumps(data)
        conn = httplib.HTTPConnection(self.server, 8080)
        conn.request(action, path, body, headers)
        response = conn.getresponse()
        ret = (response.status, response.reason, response.read())
        print ret
        conn.close()
        return ret
 
pusher = StaticEntryPusher('192.168.56.1')

# table 0:no vlan -> go to table 1 
flow1 = {
	"table":"0",
	"switch":"00:00:00:00:00:00:00:01",
	"name":"flow_mod_1",
	"cookie":"0",
    	"priority":"40000",
    	"active":"true",
    	"eth_vlan_vid":"0",
	"instruction_goto_table":"1"
    }
# table 0:vlan -> go to table 2 
flow2 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_2",
        "cookie":"0",
        "priority":"30000",
        "active":"true",
        "eth_vlan_vid":"*",
        "instruction_goto_table":"2"
    }

# table 1: no vlan -> go to controller  
flow3 = {
        "table":"1",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_3",
        "cookie":"0",
        "priority":"20000",
        "active":"true",
	"eth_vlan_vid":"0",
        "instruction_apply_actions":"output=controller"
    }

# table 1: any -> normal  
flow4 = {
        "table":"1",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_4",
        "cookie":"0",
        "priority":"10000",
        "active":"true",
	"instruction_apply_actions":"output=normal"
    }
# table 0:no vlan -> go to table 1 
flow5 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_5",
        "cookie":"0",
        "priority":"40000",
        "active":"true",
        "eth_vlan_vid":"0",
        "instruction_goto_table":"1"
    }
# table 0:vlan -> go to table 2 
flow6 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_6",
        "cookie":"0",
        "priority":"30000",
        "active":"true",
        "eth_vlan_vid":"*",
        "instruction_goto_table":"2"
    }

# table 1: no vlan -> go to controller  
flow7 = {
        "table":"1",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_7",
        "cookie":"0",
        "priority":"20000",
        "active":"true",
        "eth_vlan_vid":"0",
        "instruction_apply_actions":"output=controller"
    }

# table 1: any -> normal  
flow8 = {
        "table":"1",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_8",
        "cookie":"0",
        "priority":"10000",
        "active":"true",
        "instruction_apply_actions":"output=normal"
    }
# test flowmod 
# O -> NO VLAN
flow9 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_9",
        "cookie":"0",
        "priority":"10000",
        "active":"true",
        "eth_vlan_vid":"0",
	"instruction_apply_actions":"output=normal"
    }
flow10 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_10",
        "cookie":"0",
        "priority":"50000",
        "active":"true",
        "eth_type":"0x8100",
	"eth_vlan_vid":"1",
        "instruction_apply_actions":"pop_vlan,output=normal"
    }
flow11 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_11",
        "cookie":"0",
        "priority":"50000",
        "active":"true",
	"eth_type":"0x0800",
	"instruction_apply_actions":"push_vlan=0x8100,set_field=eth_vlan_vid->1,output=normal"
    }
flow12 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:01",
        "name":"flow_mod_12",
        "cookie":"0",
        "priority":"50000",
        "active":"true",
        "eth_type":"0x0806",
        "instruction_apply_actions":"output=flood"
    }
flow13 = {
        "table":"0",
        "switch":"00:00:00:00:00:00:00:02",
        "name":"flow_mod_13",
        "cookie":"0",
        "priority":"50000",
        "active":"true",
        "eth_type":"0x0806",
        "instruction_apply_actions":"output=flood"
    } 

#pusher.set(flow1)
#pusher.set(flow2)
#pusher.set(flow3)
#pusher.set(flow4)
#pusher.set(flow5)
#pusher.set(flow6)

pusher.set(flow10)
pusher.set(flow11)
pusher.set(flow12)
pusher.set(flow13)
