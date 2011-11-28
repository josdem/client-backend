package com.all.tracker.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class InstallerMetrics {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String OS;
	private String OSVERSION;
	private String OSLANGUAGE;
	private String NUMCPU;
	private String CPUARCHITECTURE;
	private String CPUMODEL;
	private String RAM;
	private String HD;
	private String JAVAVERSION;
	@Column(length=500)
	private String NETWORKADAPTER;
	private String STATUS;
	private String MAC;
	private String INSTALLERTYPE;
	private String CLIENTVERSION;
	private long timestamp;
	
	public InstallerMetrics() {
		timestamp = System.currentTimeMillis();
	}
	
	public InstallerMetrics(String os, String os_version, String os_language, String num_cpu, String cpu_arch, String cpu_model, String ram, String hd, String java, String network, String status, String mac, String INSTALLERTYPE, String clientVersion){
		OS = os;
		OSVERSION = os_version;
		OSLANGUAGE = os_language;
		NUMCPU = num_cpu;
		CPUARCHITECTURE = cpu_arch;
		CPUMODEL = cpu_model;
		RAM = ram;
		HD = hd;
		JAVAVERSION = java;
		NETWORKADAPTER = network;
		CLIENTVERSION = clientVersion;
		setINSTALLERTYPE(INSTALLERTYPE);
		setSTATUS(status);
		MAC = mac;
		timestamp = System.currentTimeMillis();
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOS() {
		return OS;
	}
	public void setOS(String oS) {
		OS = oS;
	}
	public String getOSVERSION() {
		return OSVERSION;
	}
	public void setOSVERSION(String oSVERSION) {
		OSVERSION = oSVERSION;
	}
	public String getOSLANGUAGE() {
		return OSLANGUAGE;
	}
	public void setOSLANGUAGE(String oSLANGUAGE) {
		OSLANGUAGE = oSLANGUAGE;
	}
	public String getNUMCPU() {
		return NUMCPU;
	}
	public void setNUMCPU(String nUMCPU) {
		NUMCPU = nUMCPU;
	}
	public String getCPUARCHITECTURE() {
		return CPUARCHITECTURE;
	}
	public void setCPUARCHITECTURE(String cPUARCHITECTURE) {
		CPUARCHITECTURE = cPUARCHITECTURE;
	}
	public String getCPUMODEL() {
		return CPUMODEL;
	}
	public void setCPUMODEL(String cPUMODEL) {
		CPUMODEL = cPUMODEL;
	}
	public String getRAM() {
		return RAM;
	}
	public void setRAM(String rAM) {
		RAM = rAM;
	}
	public String getHD() {
		return HD;
	}
	public void setHD(String hD) {
		HD = hD;
	}
	public void setNETWORKADAPTER(String nETWORKADAPTER) {
		NETWORKADAPTER = nETWORKADAPTER;
	}

	public String getNETWORKADAPTER() {
		return NETWORKADAPTER;
	}

	public void setJAVAVERSION(String jAVAVERSION) {
		JAVAVERSION = jAVAVERSION;
	}

	public String getJAVAVERSION() {
		return JAVAVERSION;
	}

	public void setMAC(String mAC) {
		MAC = mAC;
	}

	public String getMAC() {
		return MAC;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getSTATUS() {
		return STATUS;
	}

	public void setINSTALLERTYPE(String iNSTALLERTYPE) {
		INSTALLERTYPE = iNSTALLERTYPE;
	}

	public String getINSTALLERTYPE() {
		return INSTALLERTYPE;
	}

	public void setCLIENTVERSION(String cLIENTVERSION) {
		CLIENTVERSION = cLIENTVERSION;
	}

	public String getCLIENTVERSION() {
		return CLIENTVERSION;
	}
}
