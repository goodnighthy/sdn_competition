"""Custom topology example

Two directly connected switches plus a host for each switch:

   host --- switch --- switch --- host


"""

from mininet.topo import Topo

class MyTopo( Topo ):
    "Simple topology example."

    def __init__( self ):
        "Create custom topo."

        # Initialize topology
        Topo.__init__( self )

        # Add hosts and switches
        Host1 = self.addHost( 'h1' )
        Host2 = self.addHost( 'h2' )
        Host3 = self.addHost( 'h3' )
	Switch1 = self.addSwitch( 's1' ,protocal="OpenFlow13")
        Switch2 = self.addSwitch( 's2' ,protocal="OpenFlow13")
        Host4 = self.addHost( 'h4' )
	Host5 = self.addHost( 'h5' )
	Host6 = self.addHost( 'h6' )
	Host7 = self.addHost( 'h7' )
	
	# Add links
        self.addLink( Host1, Switch1 )
        self.addLink( Host2, Switch1 )
	self.addLink( Host3, Switch1 )
	self.addLink( Switch1, Switch2 )
	self.addLink( Host4, Switch2 )
	self.addLink( Host5, Switch2 )
	self.addLink( Host6, Switch2 )
	self.addLink( Host7, Switch2 )

topos = { 'mytopo': ( lambda: MyTopo() ) }
