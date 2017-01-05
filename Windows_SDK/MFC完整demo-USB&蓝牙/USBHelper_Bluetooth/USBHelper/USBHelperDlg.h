
// USBHelperDlg.h : 头文件
//

#pragma once
#include <vector>
#include "UsbDevInterface.h"
#include "usbioctl.h"
#include <map>
#include <list>
#include <Dbt.h>

enum DONGLE_TYPE
{
	DONGLE_STATUS = 0x80,
	STOP_SCAN = 0x87,
	START_DFU = 0x90,
	OTA_INFO = 0x91,
	RAW_DATA = 0x92,
	CHECK_SUM = 0x93,
	STOP_DFU = 0x94,
	FIRMWARE_SWITCH = 0x95,
	DONGLE_VERSION = 0x85,
	BLUETOOTH_ENTER = 0xa0,
	BLUETOOTH_INFO = 0xa1,
	BLUETOOTH_RAW_DATA = 0xa2,
	BLUETOOTH_CHECK_SUM = 0xa3,
	BLUETOOTH_STOP_DFU = 0xa4,
	BLUETOOTH_RESULT = 0xa5
};


typedef struct
{
	PUSB_NODE_INFORMATION               HubInfo;        // NULL if not a HUB

	PCHAR                               HubName;        // NULL if not a HUB

	PUSB_NODE_CONNECTION_INFORMATION    ConnectionInfo; // NULL if root HUB

	PUSB_DESCRIPTOR_REQUEST             ConfigDesc;     // NULL if root HUB

	//PSTRING_DESCRIPTOR_NODE             StringDescs;

} USBDEVICEINFO, *PUSBDEVICEINFO;


#if DBG

#define ALLOC(dwBytes) MyAlloc(__FILE__, __LINE__, (dwBytes))

#define REALLOC(hMem, dwBytes) MyReAlloc((hMem), (dwBytes))

#define FREE(hMem)  MyFree((hMem))

#define CHECKFORLEAKS() MyCheckForLeaks()

#else

#define ALLOC(dwBytes) GlobalAlloc(GPTR,(dwBytes))

#define REALLOC(hMem, dwBytes) GlobalReAlloc((hMem), (dwBytes), (GMEM_MOVEABLE|GMEM_ZEROINIT))

#define FREE(hMem)  GlobalFree((hMem))

#define CHECKFORLEAKS()

#endif


struct sCanvasPointItem
{
	CPoint beginPonit;
	std::list<CPoint> lstPoint;
};

struct Firmware_Info
{
	CString sDeviceName;
	CString sType;
	int nVesion;
	int nVesion2;
	int nVesion3;
	int nVesion4;
};

// CUSBHelperDlg 对话框
class CUSBHelperDlg : public CDialogEx
{
// 构造
public:
	CUSBHelperDlg(CWnd* pParent = NULL);	// 标准构造函数

// 对话框数据
	enum { IDD = IDD_USBHELPER_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV 支持


// 实现
protected:
	HICON m_hIcon;

	// 生成的消息映射函数
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

private:
	void getAllDev(std::vector<CString>& vecDevPath, std::vector<CString>& vecDevName);
	void initListCtrlInfo();
	void insertUsbDevInfoToListContrl(std::vector<CString>& vecDevPath, std::vector<CString>& vecDevName);
	PCHAR GetRootHubName (HANDLE HostController);
	void insertScanDevice(const int& nDeviveNum, unsigned char* pDeviceName);

	// 枚举主机控制器
	PUSBDEVICEINFO EnumerateHostControllers(const int& nPid);
	PCHAR GetHCDDriverKeyName (HANDLE  HCD);
	PUSBDEVICEINFO EnumerateHub (const int& nPid, PCHAR HubName,PUSB_NODE_CONNECTION_INFORMATION ConnectionInfo, PUSB_DESCRIPTOR_REQUEST ConfigDesc,PCHAR DeviceDesc, int& nRes);
	bool EnumerateHubPorts (const int& nPid, HANDLE hHubDevice, PUSBDEVICEINFO info);


	void DisplayConnectionInfo (PUSB_NODE_CONNECTION_INFORMATION ConnectInfo);
	void DisplayPipeInfo (ULONG NumPipes, USB_PIPE_INFO  *PipeInfo);
	void DisplayEndpointDescriptor (PUSB_ENDPOINT_DESCRIPTOR EndpointDesc);
	PCHAR GetVendorString (USHORT idVendor);
	BOOL CreateTextBuffer();
	void DestroyTextBuffer();
	BOOL ResetTextBuffer();

	//
	void setRecvData(const char* pData);
	void onRecvData(PEN_INFO& pendataInfo);
	bool pointIsInvalid( int nPenStatus, CPoint& pointValue );
	void compressPoint(CPoint& point);
	void moveCursor( CPoint& pos);
	void setDeviceStatus(const int& nStatus){m_nDevStatus = nStatus;}


public:
	afx_msg void OnBnClickedCancel();
	afx_msg void OnBnClickedButton3Open();
	afx_msg void OnBnClickedButton1ClrRcv();


	// 数据回调函数
	static void CALLBACK getUsbData(const unsigned char* pUsbData, const sPenInfo& penInfo, void* context);
	afx_msg void OnBnClickedButton4CloseDevice();
	afx_msg void OnBnClickedButton5SendData();
	afx_msg void OnBnClickedButton2ClrSend();
	afx_msg void OnNMClickListUsbDevice(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnMouseMove(UINT nFlags, CPoint point);
	afx_msg void OnLButtonDown(UINT nFlags, CPoint point);
	afx_msg void OnLButtonUp(UINT nFlags, CPoint point);
	afx_msg void OnBnClickedButtonScan();

private:
	std::map<CString, int> m_mapHidPath2ProductId;
	bool m_bDrawing;
	std::list<sCanvasPointItem> m_listItems;
	sCanvasPointItem m_currentItem;
	CPoint m_lastPoint;
	int m_nPenStatus;
	CPoint m_point;

	int m_nDevStatus;
protected:
	CRectTracker m_Tracker;

	void onbegin(const CPoint& pos);
	void onDrawing(const CPoint& pos);
	void onEnd();
	void doDrawing(const CPoint& pos);
	void endTrack(bool bSave = true);
public:
	afx_msg void OnBnClickedButtonSndScan();
	afx_msg void OnBnClickedButtonConnect();
	afx_msg void OnBnClickedButtonClrCanvas();
	afx_msg void OnBnClickedButtonClsConnect();
	afx_msg void OnDestroy();

	void resetDevice();			//--by zlp 2016/9/26
protected:  
	HDEVNOTIFY m_hDeviceNotify;
	CString m_strCurrentDevPath;
    afx_msg BOOL OnDeviceChange(UINT nEventType, DWORD dwData);  //--by zlp 2016/9/27
private:
	//add dfu
	void SendDongleCmd(DONGLE_TYPE type,int nLength = 0,const unsigned char* pBuf = NULL);
	void ParseDFUData(const unsigned char* pUsbData);
	int GetFileSize(CString strFilePath);
	int nCheckSum;
	void UpdateFirmware();
	void UpdateBluetooth();
	bool SpiltString(CString strFileName,Firmware_Info &firmwareinfo);
	void GetFirmware();
	void Split(CString source, CStringArray& dest, CString division);

	Firmware_Info m_McuFirmware;
	Firmware_Info m_BleFirmware;
	CString strUpdatePath;
public:
	afx_msg void OnBnClickedButtonUpdate();
private:
	FILE *fp;
	int nIndex;
	int nSize;
	int nCount;
	bool bEnd;
	bool bRawDataState;
	bool bIsDFU;
};
