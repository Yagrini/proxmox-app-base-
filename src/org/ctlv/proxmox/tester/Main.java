package org.ctlv.proxmox.tester;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.ctlv.proxmox.api.Constants;
import org.ctlv.proxmox.api.ProxmoxAPI;
import org.ctlv.proxmox.api.data.LXC;
import org.json.JSONException;


public class Main {

	static ProxmoxAPI proxmoxAPI = new ProxmoxAPI();

	public static void CTB5Generator() throws LoginException, JSONException, IOException {
		double maxMemoryOnServer1 = proxmoxAPI.getNode(Constants.SERVER1).getMemory_total() * 0.16;
		double maxMemoryOnServer2 = proxmoxAPI.getNode(Constants.SERVER2).getMemory_total() * 0.16;
		double usedMemoryServer1 = getUsedMemoryByUs(Constants.SERVER1);
		double usedMemoryServer2 = getUsedMemoryByUs(Constants.SERVER2);
		int id = getLowerPossibleID();
		if(id == -1) {
			System.out.println("Range of ids is exceeded");
		} else {
			System.out.println("Lower id available is : " + id);
			while ((id<3000) && ((usedMemoryServer1 < maxMemoryOnServer1) || (usedMemoryServer2 < maxMemoryOnServer2))) {
				Random generator = new Random();
				if ((usedMemoryServer2 >= maxMemoryOnServer2) || ((generator.nextInt(100) <= 66) && (usedMemoryServer1 < maxMemoryOnServer1))) {
					usedMemoryServer1 = createCTOnServer(Constants.SERVER1, Integer.toString(id), usedMemoryServer1);
				} else if(usedMemoryServer2 < maxMemoryOnServer2){
					usedMemoryServer2 = createCTOnServer(Constants.SERVER2, Integer.toString(id), usedMemoryServer2);
				}
				id++;
			}
		}
		usedMemoryServer1 = getUsedMemoryByUs(Constants.SERVER1);
		usedMemoryServer2 = getUsedMemoryByUs(Constants.SERVER2);
		System.out.println("Finished Creation of CT with an id = " + getLowerPossibleID());
		System.out.println("Used Memory By B5 on Server1 is : " + usedMemoryServer1 + " with percentage of : "  + usedMemoryServer1/proxmoxAPI.getNode(Constants.SERVER1).getMemory_total());
		System.out.println("Used Memory By B5 on Server2 is : " + usedMemoryServer2 + " with percentage of : " + usedMemoryServer2/proxmoxAPI.getNode(Constants.SERVER1).getMemory_total());
	}
	public static long getUsedMemoryByUs(String serverName) throws LoginException, IOException, JSONException {
		long userMemoryByUs = 0;
		for(String ct : proxmoxAPI.getCTList(serverName)){
			int myId = Integer.parseInt(ct);
			if((myId>100) && (myId%2500 < 100)){
				userMemoryByUs += proxmoxAPI.getCT(serverName,ct).getMaxmem();
			}
		}
		return userMemoryByUs;
	}
	public static double createCTOnServer(String serverName, String idCT, double usedMemory) throws LoginException, IOException, JSONException {
		proxmoxAPI.createCT(serverName, idCT, Constants.CT_BASE_NAME + idCT, 512);
		LXC createdCT = proxmoxAPI.getCT(serverName, idCT);
		return usedMemory + createdCT.getMaxmem();
	}
	public static int getLowerPossibleID() throws LoginException, IOException, JSONException {
		List<String> containersID = proxmoxAPI.getCTList(Constants.SERVER1);
		containersID.addAll(proxmoxAPI.getCTList(Constants.SERVER2));
		containersID.sort(String::compareTo);
		int possibleId = 0;
		for(String id : containersID){
			int myId = Integer.parseInt(id);
			if((myId>100) && (myId%2500 < 100)) {
				possibleId = myId + 1;
				if (!containersID.contains(Integer.toString(possibleId))) {
					return possibleId;
				}
			} else if (myId > 2599) {
				return -1;
			}
		}
		return 2500;
	}
	public static void main(String[] args) throws LoginException, JSONException, IOException {
		Main.CTB5Generator();
	}

}
