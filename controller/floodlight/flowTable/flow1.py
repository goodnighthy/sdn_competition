import httplib  
import json  
  
class StaticFlowPusher(object):  
  
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
        path = '/wm/staticflowentrypusher/json'  
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

pusher = StaticFlowPusher('172.16.173.131') #controller ip
  
flow1 = {  
    'switch':"00:00:00:00:00:00:00:01",  
    "name":"flow-mod-1",  
    "cookie":"0",  
    "priority":"32768",  
    "in_port":"1",  
    "active":"true",  
    "actions":"output=1"  
    }  
      
flow2 = {  
    'switch':"00:00:00:00:00:00:00:01",  
    "name":"flow-mod-2",  
    "cookie":"0",  
    "priority":"32768",  
    "in_port":"2",  
    "active":"true",  
    "actions":"output=flood"  
    }  
  
flow3 = {  
    'switch':"00:00:00:00:00:00:00:01",  
    "name":"flow-mod-3",  
    "cookie":"0",  
    "priority":"32768",  
    "in_port":"3",  
    "active":"true",  
    "actions":"output=flood"  
    }  
#add flow table flow1,flow2,flow3  
pusher.set(flow1)     
pusher.set(flow2)  
pusher.set(flow3)
