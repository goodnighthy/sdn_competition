package net.floodlightcontroller.vlan;

import java.util.ArrayList;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.TransportPort;

import net.floodlightcontroller.core.IOFSwitch;

public class createFlowMod {
	
	public static void createFlowMods(IOFSwitch sw, String srcipv4, String dstipv4, 
			String protocol, String srcport, String dstport, int outport){
		IOFSwitch mySwitch = sw;
		OFFactory myFactory = mySwitch.getOFFactory();
		OFVersion myVersion = myFactory.getVersion();
		
		Match.Builder myMatchBuilder = myFactory.buildMatch();
		//myMatchBuilder.setExact(MatchField.IN_PORT, OFPort.of(1))
		myMatchBuilder.setExact(MatchField.ETH_TYPE, EthType.IPv4);
		if (srcipv4 != null)
			myMatchBuilder.setMasked(MatchField.IPV4_SRC, IPv4AddressWithMask.of(srcipv4));
		if (dstipv4 != null)
			myMatchBuilder.setMasked(MatchField.IPV4_DST, IPv4AddressWithMask.of(dstipv4));
		if (protocol != null){
			if (protocol.equalsIgnoreCase("tcp")){
				myMatchBuilder.setExact(MatchField.IP_PROTO, IpProtocol.TCP);
				if (srcport != null)
					myMatchBuilder.setExact(MatchField.TCP_SRC, TransportPort.of(Integer.parseInt(srcport)));
				if (dstport != null)
					myMatchBuilder.setExact(MatchField.TCP_DST, TransportPort.of(Integer.parseInt(dstport)));
			}		
			if (protocol.equalsIgnoreCase("udp")){
				myMatchBuilder.setExact(MatchField.IP_PROTO, IpProtocol.UDP);
				if (srcport != null)
					myMatchBuilder.setExact(MatchField.UDP_SRC, TransportPort.of(Integer.parseInt(srcport)));
				if (dstport != null)
					myMatchBuilder.setExact(MatchField.UDP_DST, TransportPort.of(Integer.parseInt(dstport)));
			}
		}
		
		
		Match myMatch = myMatchBuilder.build();
		
		switch (myVersion){
			case OF_10:
				ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
				OFActions actions10 = myFactory.actions();
				
				/*
				// Use builder to create OFAction.
				OFActionSetDlDst setDlDst10 = actions10.buildSetDlDst()
						.setDlAddr(MacAddress.of("ff:ff:ff:ff:ff:ff"))
						.build();
				actionList10.add(setDlDst10);
				
				// Create OFAction directly w/o use of builder. 
				OFActionSetNwDst setNwDst10 = actions10.buildSetNwDst()
						.setNwAddr(IPv4Address.of("255.255.255.255"))
						.build();
				actionList10.add(setNwDst10);
				
				*/
				 
				// Use builder again.
				OFActionOutput output = actions10.buildOutput()
				    .setMaxLen(0xFFffFFff)
				    .setPort(OFPort.of(outport))
				    .build();
				actionList10.add(output);
				
				
				OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
					    .setBufferId(OFBufferId.NO_BUFFER)
					    .setHardTimeout(3600)
					    .setIdleTimeout(3600)
					    .setPriority(32768)
					    .setMatch(myMatch)
					    .setActions(actionList10)
					    .setOutPort(OFPort.of(outport))
					    .build();
				
				mySwitch.write(flowAdd10);
				break;
			case OF_13:
				ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
				OFInstructions instructions13 = myFactory.instructions();
				OFActions actions13 = myFactory.actions();
				OFOxms oxms13 = myFactory.oxms();
				
				/*
				// Use OXM to modify data layer dest field.
				OFActionSetField setDlDst13 = actions13.buildSetField()
				    .setField(
				        oxms13.buildEthDst()
				        .setValue(MacAddress.of("ff:ff:ff:ff:ff:ff"))
				        .build()
				    )
				    .build();
				actionList13.add(setDlDst13);
				 
				// Use OXM to modify network layer dest field.
				OFActionSetField setNwDst13 = actions13.buildSetField()
				    .setField(
				        oxms13.buildIpv4Dst()
				        .setValue(IPv4Address.of("255.255.255.255"))
				        .build()
				    )
				    .build();
				actionList13.add(setNwDst13);
				
				*/
				 
				 
				// Output to a port is also an OFAction, not an OXM.
				OFActionOutput output13 = actions13.buildOutput()
				    .setMaxLen(0xFFffFFff)
				    .setPort(OFPort.of(outport))
				    .build();
				actionList13.add(output13);
				
				OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
					    .setActions(actionList13)
					    .build();
				
				ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
				instructionList13.add(applyActions13);
				
				OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
					    .setBufferId(OFBufferId.NO_BUFFER)
					    .setHardTimeout(3600)
					    .setIdleTimeout(3600)
					    .setPriority(32768)
					    .setMatch(myMatch)
					    .setInstructions(instructionList13)
					    .setOutPort(OFPort.of(outport))
					    .build();
				
				mySwitch.write(flowAdd13);
				break;
			default:
				//logger.error("Unsupported OFVersion: {}", myVersion.toString());
				break;
		}
	}
}
