package net.floodlightcontroller.vlan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionPopVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionPushPbb;
import org.projectfloodlight.openflow.protocol.action.OFActionPushVlan;
import org.projectfloodlight.openflow.protocol.action.OFActionSetDlDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetField;
import org.projectfloodlight.openflow.protocol.action.OFActionSetNwDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetTpDst;
import org.projectfloodlight.openflow.protocol.action.OFActionSetVlanVid;
import org.projectfloodlight.openflow.protocol.action.OFActionStripVlan;
import org.projectfloodlight.openflow.protocol.action.OFActions;
import org.projectfloodlight.openflow.protocol.actionid.OFActionIdPushVlan;
import org.projectfloodlight.openflow.protocol.instruction.OFInstruction;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionApplyActions;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionGotoTable;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructionWriteMetadata;
import org.projectfloodlight.openflow.protocol.instruction.OFInstructions;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.oxm.OFOxms;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IPv4AddressWithMask;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFMetadata;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.OFVlanVidMatch;
import org.projectfloodlight.openflow.types.TableId;
import org.projectfloodlight.openflow.types.TransportPort;
import org.projectfloodlight.openflow.types.VlanVid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.packet.UDP;
import net.sf.json.JSONObject;

public class vlanAlloction implements IOFMessageListener, IFloodlightModule{
	private static IFloodlightProviderService floodlightProvider;
	private static IOFSwitchService switchService;
	private static Logger logger;
	
	public String getName() {
		// TODO Auto-generated method stub
		return "vlanAlloction";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		switchService = context.getServiceImpl(IOFSwitchService.class);
		logger = LoggerFactory.getLogger("vlanAlloction.class");
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		// TODO Auto-generated method stub
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public Command receive(IOFSwitch sw, OFMessage msg,FloodlightContext cntx) {
		// TODO Auto-generated method stub
		String switchId = sw.getId().toString();
	    switch (msg.getType()) {
	    case PACKET_IN:
	    	OFPacketIn myPacketIn = (OFPacketIn) msg;
	        Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
	        VlanVid vlanId = VlanVid.ofVlan(eth.getVlanID());
	        OFPort inPort = (myPacketIn.getVersion().compareTo(OFVersion.OF_12) < 0) ? myPacketIn.getInPort() : myPacketIn.getMatch().get(MatchField.IN_PORT);

	        OFBufferId bufferId = myPacketIn.getBufferId();
	        
	        if (eth.getEtherType() == EthType.IPv4) {
	            IPv4 ipv4 = (IPv4) eth.getPayload();           
	            IPv4Address srcIp = ipv4.getSourceAddress();
	            //int srcVlan = getVlan(srcIp.toString());
	            if(eth.getVlanID() > 0){
	            	//代表登陆用户，下发三条流表
	            	System.out.println("here" + eth.getVlanID());
//	            	createFlowMod1(switchId, srcIp, srcVlan);
	            	//createFlowMod3(switchId, srcIp, VlanVid.ofVlan(srcVlan), inPort);
	            	
	            }else{
	            	//用户未登陆，导向登陆界面
	            	//createFlowMod2(switchId, srcIp, bufferId);
	            }
	            
	        }
	        break;
	    default:
	        break;
	    }
	    return Command.CONTINUE;
	}
	
	//table2 :srcip push vlan goto table3
	public static void createFlowModPushVlan(String switchid, IPv4Address srcipv4, int vlan){
		IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of(switchid));
		OFFactory myFactory = mySwitch.getOFFactory();
		OFVersion myVersion = myFactory.getVersion();
		
		Match.Builder myMatchBuilder = myFactory.buildMatch();
		if (srcipv4 != null){
			myMatchBuilder.setExact(MatchField.IPV4_SRC, srcipv4);
		}
		Match myMatch = myMatchBuilder.build();
		
		switch (myVersion){
//			case OF_10:
//				ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
//				OFActions actions10 = myFactory.actions();
//				//push vlan
////				if(0 < vlan){
////					OFActionSetVlanVid setVlan = actions10.setVlanVid(VlanVid.ofVlan(vlan));
////					actionList10.add(setVlan);
////				}
//				
//			
//				
//				OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
//					    .setBufferId(OFBufferId.NO_BUFFER)
//					    .setHardTimeout(3600)
//					    .setIdleTimeout(3600)
//					    .setPriority(32768)
//					    .setMatch(myMatch)
//					    .setActions(actionList10)
//					    .build();
//				
//				mySwitch.write(flowAdd10);
//				System.out.println("here");
//				break;
			case OF_13:
				ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
				OFInstructions instructions13 = myFactory.instructions();
				OFActions actions13 = myFactory.actions();
				OFOxms oxms13 = myFactory.oxms();
				
				//push vlan
				if(0 < vlan){
					OFActionSetField setVlan = actions13.buildSetField()
						    .setField(
						        oxms13.buildVlanVid()
						        .setValue(OFVlanVidMatch.ofVlan(vlan))
						        .build()
						    )
						    .build();
					actionList13.add(setVlan);
				}
				OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
					    .setActions(actionList13)
					    .build();
				//goto table3
				OFInstructionGotoTable gotoTable13= instructions13.buildGotoTable().
						setTableId(TableId.of(2)).
						build();
				
				ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
				instructionList13.add(applyActions13);
				instructionList13.add(gotoTable13);
				
				OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
					    .setBufferId(OFBufferId.NO_BUFFER)
					    .setHardTimeout(3600)
					    .setIdleTimeout(3600)
					    .setPriority(30000)
					    .setMatch(myMatch)
					    .setInstructions(instructionList13)
					    .setTableId(TableId.of(1))
					    .build();
				
				mySwitch.write(flowAdd13);

				break;
			default:
				logger.error("Unsupported OFVersion: {}", myVersion.toString());
				break;
		}
	}
	
	//table2 : go to auth server
	public static void createFlowMod2AuthServer(String switchid, IPv4Address srcipv4,OFBufferId bufferId){
		IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of(switchid));
		OFFactory myFactory = mySwitch.getOFFactory();
		OFVersion myVersion = myFactory.getVersion();
		String desip = "127.0.0.1";
		Match.Builder myMatchBuilder = myFactory.buildMatch();
		if (srcipv4 != null)
			myMatchBuilder.setExact(MatchField.IPV4_SRC, srcipv4);
		Match myMatch = myMatchBuilder.build();
		
		switch (myVersion){
//			case OF_10:
//				ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
//				OFActions actions10 = myFactory.actions();
//
//				//change des ip
//				OFActionSetNwDst setNwDst = actions10.setNwDst(srcipv4);
//				actionList10.add(setNwDst);
//				
//				//change des port
//				OFActionSetTpDst setTpDet = actions10.setTpDst(TransportPort.of(80));
//				actionList10.add(setTpDet);
//				
//				OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
//					    .setBufferId(OFBufferId.NO_BUFFER)
//					    .setHardTimeout(3600)
//					    .setIdleTimeout(3600)
//					    .setPriority(2000)
//					    .setMatch(myMatch)
//					    .setActions(actionList10)
//					    .setOutPort(OFPort.NORMAL)
//					    .build();
//				
//				mySwitch.write(flowAdd10);
//				break;
		case OF_13:
			ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
			OFInstructions instructions13 = myFactory.instructions();
			OFActions actions13 = myFactory.actions();
			OFOxms oxms13 = myFactory.oxms();
			
			//change des ip
			OFActionSetField setDlDst = actions13.buildSetField()
					.setField(
						oxms13.buildIpv4Dst()
						.setValue(IPv4Address.of(desip))
						.build()
						)
					.build();
			actionList13.add(setDlDst);
			
			//change des port
			OFActionSetField setTransportPort = actions13.buildSetField()
					.setField(
						oxms13.buildTcpDst()
						.setValue(TransportPort.of(80))
						.build()
						)
					.build();
			actionList13.add(setTransportPort);
			
			OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
				    .setActions(actionList13)
				    .build();
			
			ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
			instructionList13.add(applyActions13);
			
			OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
				    .setBufferId(bufferId)
				    .setHardTimeout(10)
				    .setIdleTimeout(10)
				    .setPriority(20000)
				    .setMatch(myMatch)
				    .setInstructions(instructionList13)
				    .setTableId(TableId.of(2))
				    .setOutPort(OFPort.NORMAL)
				    .build();
			
			mySwitch.write(flowAdd13);
			break;
		default:
			logger.error("Unsupported OFVersion: {}", myVersion.toString());
			break;
		}
	}
	
	//table3 :desip pop vlan outputport
	public static void createFlowModPopVlan(String switchid, IPv4Address srcipv4, VlanVid vlan, OFPort outPort){
		IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of(switchid));
		OFFactory myFactory = mySwitch.getOFFactory();
		OFVersion myVersion = myFactory.getVersion();
		
		Match.Builder myMatchBuilder = myFactory.buildMatch();
		if (srcipv4 != null){
			myMatchBuilder.setExact(MatchField.IPV4_DST, srcipv4);
		}
//		if(0 < vlan.getVlan()){
//			myMatchBuilder.setExact(MatchField.VLAN_VID, vlan);
//		}
		Match myMatch = myMatchBuilder.build();
		
		switch (myVersion){
//			case OF_10:
//				ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
//				OFActions actions10 = myFactory.actions();
//	
//				//pop vlan
//				OFActionStripVlan popVlan10 = actions10.stripVlan();
//				actionList10.add(popVlan10);
//				
//				//set output
//				//set outport
//				OFActionOutput output10 =actions10.buildOutput()
//						.setMaxLen(0xFFffFFff)
//						.setPort(outPort)
//						.build();
//				actionList10.add(output10);
//				
//				OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
//				    .setBufferId(OFBufferId.NO_BUFFER)
//				    .setHardTimeout(3600)
//				    .setIdleTimeout(3600)
//				    .setPriority(2000)
//				    .setMatch(myMatch)
//				    .setActions(actionList10)
////				    .setTableId(TableId.of(3))
//				    .build();
//			
//				mySwitch.write(flowAdd10);
//				System.out.println("here flow3");
//				break;
			case OF_13:
				ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
				OFInstructions instructions13 = myFactory.instructions();
				OFActions actions13 = myFactory.actions();
				
				//pop vlan
				OFActionPopVlan popVlan13 = actions13.popVlan();
				actionList13.add(popVlan13);
				
				//set outport
				OFActionOutput output =actions13.buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(outPort)
						.build();
				actionList13.add(output);
				
				OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
					    .setActions(actionList13)
					    .build();
				
				ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
				instructionList13.add(applyActions13);
				
				OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
					    .setBufferId(OFBufferId.NO_BUFFER)
					    .setHardTimeout(3600)
					    .setIdleTimeout(3600)
					    .setPriority(20000)
					    .setMatch(myMatch)
					    .setInstructions(instructionList13)
					    .setTableId(TableId.of(3))
					    .build();
				
				mySwitch.write(flowAdd13);
				
				break;
			default:
				logger.error("Unsupported OFVersion: {}", myVersion.toString());
				break;
		}
	}
	
	//table3 :srcip normal
	public static void createFlowMod4(String switchid, IPv4Address srcipv4, VlanVid vlan, OFPort outPort){
		IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of(switchid));
		OFFactory myFactory = mySwitch.getOFFactory();
		OFVersion myVersion = myFactory.getVersion();
		
		Match.Builder myMatchBuilder = myFactory.buildMatch();
		if (srcipv4 != null){
			myMatchBuilder.setExact(MatchField.IPV4_SRC, srcipv4);
		}
//			if(0 < vlan.getVlan()){
//				myMatchBuilder.setExact(MatchField.VLAN_VID, vlan);
//			}
		Match myMatch = myMatchBuilder.build();
		
		switch (myVersion){
			case OF_10:
				ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
				OFActions actions10 = myFactory.actions();
	
				//pop vlan
				OFActionStripVlan popVlan10 = actions10.stripVlan();
				actionList10.add(popVlan10);
				
				//set output
				//set outport
				OFActionOutput output10 =actions10.buildOutput()
					.setMaxLen(0xFFffFFff)
					.setPort(outPort)
					.build();
			actionList10.add(output10);
			
			OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
			    .setBufferId(OFBufferId.NO_BUFFER)
			    .setHardTimeout(3600)
			    .setIdleTimeout(3600)
			    .setPriority(2000)
			    .setMatch(myMatch)
			    .setActions(actionList10)
//			    .setTableId(TableId.of(3))
			    .build();
		
			mySwitch.write(flowAdd10);
			System.out.println("here flow3");
			break;
			case OF_13:
				ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
				OFInstructions instructions13 = myFactory.instructions();
				OFActions actions13 = myFactory.actions();
				
				//set outport normal
				OFActionOutput output =actions13.buildOutput()
						.setMaxLen(0xFFffFFff)
						.setPort(OFPort.NORMAL)
						.build();
				actionList13.add(output);
				
				OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
					    .setActions(actionList13)
					    .build();
				
				ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
				instructionList13.add(applyActions13);
				
				OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
					    .setBufferId(OFBufferId.NO_BUFFER)
					    .setHardTimeout(3600)
					    .setIdleTimeout(3600)
					    .setPriority(10000)
					    .setMatch(myMatch)
					    .setInstructions(instructionList13)
					    .setTableId(TableId.of(3))
					    .build();
				
				mySwitch.write(flowAdd13);
				
				break;
			default:
				logger.error("Unsupported OFVersion: {}", myVersion.toString());
				break;
		}
	}
	
	//table3 :srcip normal
		public static void createFlowMod5(String switchid, IPv4Address srcipv4, VlanVid vlan, OFPort outPort){
			IOFSwitch mySwitch = switchService.getSwitch(DatapathId.of(switchid));
			OFFactory myFactory = mySwitch.getOFFactory();
			OFVersion myVersion = myFactory.getVersion();
			
			Match.Builder myMatchBuilder = myFactory.buildMatch();
			if (srcipv4 != null){
				myMatchBuilder.setExact(MatchField.IPV4_SRC, srcipv4);
			}

			
			Match myMatch = myMatchBuilder
					.setExact(MatchField.VLAN_VID, OFVlanVidMatch.ofVlanVid(vlan))
					.build();
			
			switch (myVersion){
//				case OF_10:
//					ArrayList<OFAction> actionList10 = new ArrayList<OFAction>();
//					OFActions actions10 = myFactory.actions();
//		
//					//pop vlan
//					OFActionStripVlan popVlan10 = actions10.stripVlan();
//					actionList10.add(popVlan10);
//					
//					//set output
//					//set outport
//					OFActionOutput output10 =actions10.buildOutput()
//						.setMaxLen(0xFFffFFff)
//						.setPort(outPort)
//						.build();
//				actionList10.add(output10);
//				
//				OFFlowAdd flowAdd10 = myFactory.buildFlowAdd()
//				    .setBufferId(OFBufferId.NO_BUFFER)
//				    .setHardTimeout(3600)
//				    .setIdleTimeout(3600)
//				    .setPriority(2000)
//				    .setMatch(myMatch)
//				    .setActions(actionList10)
////				    .setTableId(TableId.of(3))
//				    .build();
//			
//				mySwitch.write(flowAdd10);
//				System.out.println("here flow3");
//				break;
				case OF_13:
					ArrayList<OFAction> actionList13 = new ArrayList<OFAction>();
					OFInstructions instructions13 = myFactory.instructions();
					OFActions actions13 = myFactory.actions();
					
					//set outport normal
					OFActionOutput output =actions13.buildOutput()
							.setMaxLen(0xFFffFFff)
							.setPort(OFPort.NORMAL)
							.build();
					actionList13.add(output);
					
					OFInstructionApplyActions applyActions13 = instructions13.buildApplyActions()
						    .setActions(actionList13)
						    .build();
					
					ArrayList<OFInstruction> instructionList13 = new ArrayList<OFInstruction>();
					instructionList13.add(applyActions13);
					
					OFFlowAdd flowAdd13 = myFactory.buildFlowAdd()
						    .setBufferId(OFBufferId.NO_BUFFER)
						    .setHardTimeout(3600)
						    .setIdleTimeout(3600)
						    .setPriority(10000)
						    .setMatch(myMatch)
						    .setInstructions(instructionList13)
						    .setTableId(TableId.of(3))
						    .build();
					
					mySwitch.write(flowAdd13);
					
					break;
				default:
					logger.error("Unsupported OFVersion: {}", myVersion.toString());
					break;
			}
		}
		
	public static int getVlan(String ip){
		String url = "http://localhost/api.php";
		Map<String,String> map = new HashMap<String,String>();
		map.put("ip_address", ip);
		map.put("action", "query");
		JSONObject responseContent = HttpClientUtil.getInstance()  
                .sendHttpPost(url,map); 
		return Integer.parseInt(responseContent.getString("vlan"));
	}
	
	public static void main(String[] args) {
		System.out.println(getVlan("127.0.0.1"));
	}
}
