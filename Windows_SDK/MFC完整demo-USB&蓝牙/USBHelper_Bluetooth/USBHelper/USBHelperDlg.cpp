
// USBHelperDlg.cpp : 实现文件
//

#include "stdafx.h"
#include "USBHelper.h"
#include "USBHelperDlg.h"
#include "afxdialogex.h"
#include "devioctl.h"
#include "vndrlist.h"
#include <GdiplusGraphics.h>

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

#define BUFFERALLOCINCREMENT        8192
#define BUFFERMINFREESPACE          1024
TCHAR *TextBuffer = NULL;
int   TextBufferLen = 0;
int   TextBufferPos = 0;

//#define FIRMWARE_FILE	"E:\\Robot\\dongle_mcu_app.bin"

//#define FIRMWARE_BLUETOOTH	"E:\\Robot\\nrf51422_xxac_s120.bin"
// 用于应用程序“关于”菜单项的 CAboutDlg 对话框

void __cdecl AppendTextBuffer (LPCTSTR lpFormat,...)
{
	va_list arglist;

	va_start(arglist, lpFormat);

	// Make sure we have a healthy amount of space free in the buffer,
	// reallocating the buffer if necessary.
	//
	if (TextBufferLen - TextBufferPos < BUFFERMINFREESPACE)
	{
		TCHAR *TextBufferTmp;

		TextBufferTmp = (TCHAR *)REALLOC(TextBuffer, TextBufferLen+BUFFERALLOCINCREMENT);

		if (TextBufferTmp != NULL)
		{
			TextBuffer = TextBufferTmp;
			TextBufferLen += BUFFERALLOCINCREMENT;
		}
		else
		{
			// If GlobalReAlloc fails, the original memory is not freed,
			// and the original handle and pointer are still valid.
			//
			//OOPS();

			return;
		}
	}

	// Add the text to the end of the buffer, updating the buffer position.
	//
	TextBufferPos += wvsprintf(TextBuffer + TextBufferPos,
		lpFormat,
		arglist);
}

PCHAR WideStrToMultiStr (PWCHAR WideStr)
{
	ULONG nBytes;
	PCHAR MultiStr;

	// Get the length of the converted string
	//
	nBytes = WideCharToMultiByte(
		CP_ACP,
		0,
		WideStr,
		-1,
		NULL,
		0,
		NULL,
		NULL);

	if (nBytes == 0)
	{
		return NULL;
	}

	// Allocate space to hold the converted string
	//
	MultiStr = (PCHAR)ALLOC(nBytes);

	if (MultiStr == NULL)
	{
		return NULL;
	}

	// Convert the string
	//
	nBytes = WideCharToMultiByte(
		CP_ACP,
		0,
		WideStr,
		-1,
		MultiStr,
		nBytes,
		NULL,
		NULL);

	if (nBytes == 0)
	{
		FREE(MultiStr);
		return NULL;
	}

	return MultiStr;
}


static std::wstring MultiCharToWideChar( std::string str )
{
	int len = MultiByteToWideChar(CP_ACP, 0, str.c_str(), str.size(), NULL, 0);
	TCHAR* buffer = new TCHAR[len + 1];
	MultiByteToWideChar(CP_ACP, 0, str.c_str(), str.size(), buffer, len);
	buffer[len] = '\0';
	std::wstring return_value;
	return_value.append(buffer);
	delete [] buffer;
	return return_value;
}

class CAboutDlg : public CDialogEx
{
public:
	CAboutDlg();

	// 对话框数据
	enum { IDD = IDD_ABOUTBOX };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	// 实现
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialogEx(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialogEx)
END_MESSAGE_MAP()


// CUSBHelperDlg 对话框




CUSBHelperDlg::CUSBHelperDlg(CWnd* pParent /*=NULL*/)
	: CDialogEx(CUSBHelperDlg::IDD, pParent),
	m_bDrawing(FALSE),
	m_nPenStatus(0),
	m_nDevStatus(0),
	m_hDeviceNotify(NULL),
	m_strCurrentDevPath(_T("")),
	nCheckSum(0),
	bRawDataState(false),
	bEnd(false),
	bIsDFU(false),
	strUpdatePath(_T(""))
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CUSBHelperDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialogEx::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CUSBHelperDlg, CDialogEx)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDCANCEL, &CUSBHelperDlg::OnBnClickedCancel)
	ON_BN_CLICKED(IDC_BUTTON3_OPEN, &CUSBHelperDlg::OnBnClickedButton3Open)
	ON_BN_CLICKED(IDC_BUTTON1_CLR_RCV, &CUSBHelperDlg::OnBnClickedButton1ClrRcv)
	ON_BN_CLICKED(IDC_BUTTON4_CLOSE_DEVICE, &CUSBHelperDlg::OnBnClickedButton4CloseDevice)
	ON_BN_CLICKED(IDC_BUTTON5_SEND_DATA, &CUSBHelperDlg::OnBnClickedButton5SendData)
	ON_BN_CLICKED(IDC_BUTTON2_CLR_SEND, &CUSBHelperDlg::OnBnClickedButton2ClrSend)
	ON_NOTIFY(NM_CLICK, IDC_LIST_USB_DEVICE, &CUSBHelperDlg::OnNMClickListUsbDevice)
	ON_BN_CLICKED(IDC_BUTTON_SCAN, &CUSBHelperDlg::OnBnClickedButtonScan)
	ON_WM_MOUSEMOVE()
	ON_WM_LBUTTONDOWN()
	ON_WM_LBUTTONUP()
	ON_BN_CLICKED(IDC_BUTTON_SND_SCAN, &CUSBHelperDlg::OnBnClickedButtonSndScan)
	ON_BN_CLICKED(IDC_BUTTON_CONNECT, &CUSBHelperDlg::OnBnClickedButtonConnect)
	ON_BN_CLICKED(IDC_BUTTON_CLR_CANVAS, &CUSBHelperDlg::OnBnClickedButtonClrCanvas)
	ON_BN_CLICKED(IDC_BUTTON_CLS_CONNECT, &CUSBHelperDlg::OnBnClickedButtonClsConnect)
	ON_WM_DESTROY()
	ON_WM_DEVICECHANGE()					//--by zlp 2016/9/27
	ON_BN_CLICKED(IDC_BUTTON_UPDATE, &CUSBHelperDlg::OnBnClickedButtonUpdate)
END_MESSAGE_MAP()


// CUSBHelperDlg 消息处理程序

BOOL CUSBHelperDlg::OnInitDialog()
{
	CDialogEx::OnInitDialog();

	// 将“关于...”菜单项添加到系统菜单中。

	// IDM_ABOUTBOX 必须在系统命令范围内。
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		BOOL bNameValid;
		CString strAboutMenu;
		bNameValid = strAboutMenu.LoadString(IDS_ABOUTBOX);
		ASSERT(bNameValid);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// 设置此对话框的图标。当应用程序主窗口不是对话框时，框架将自动
	//  执行此操作
	SetIcon(m_hIcon, TRUE);			// 设置大图标
	SetIcon(m_hIcon, FALSE);		// 设置小图标

	// TODO: 在此添加额外的初始化代码
	initListCtrlInfo();
	std::vector<CString> vecDevPath;
	std::vector<CString> vecDevName;
	getAllDev(vecDevPath, vecDevName);
	insertUsbDevInfoToListContrl(vecDevPath, vecDevName);
	CreateTextBuffer();

	//register USB Changed					//--by zlp 2016/9/27
	DEV_BROADCAST_DEVICEINTERFACE Filter;  
	GUID WceusbshGUID = { 0x25dbce51, 0x6c8f, 0x4a72,0x8a,0x6d,0xb5,0x4c,0x2b,0x4f,0xc8,0x35 };
	ZeroMemory(&Filter,sizeof(Filter));  
	Filter.dbcc_size = sizeof(Filter);   // size gets set to 29 with 1-byte packing or 32 with 4- or 8-byte packing  
	Filter.dbcc_devicetype = DBT_DEVTYP_DEVICEINTERFACE;  
	Filter.dbcc_classguid = WceusbshGUID;  
	//DEVICE_NOTIFY_ALL_INTERFACE_CLASSES //关注所有设备事件  
	m_hDeviceNotify = RegisterDeviceNotification(this->m_hWnd,&Filter,DEVICE_NOTIFY_ALL_INTERFACE_CLASSES);  
	if(NULL == m_hDeviceNotify)  
		TRACE("RegisterDeviceNotification failed!!");  

	GetDlgItem(IDC_BUTTON_SCAN)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);
	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);
	GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);
	/*insertScanDevice(1, (unsigned char*)"num100");
	insertScanDevice(2, (unsigned char*)"num200");//*/			//--by zlp 2016/9/26
	return TRUE;  // 除非将焦点设置到控件，否则返回 TRUE
}

void CUSBHelperDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialogEx::OnSysCommand(nID, lParam);
	}
}

// 如果向对话框添加最小化按钮，则需要下面的代码
//  来绘制该图标。对于使用文档/视图模型的 MFC 应用程序，
//  这将由框架自动完成。

void CUSBHelperDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // 用于绘制的设备上下文

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// 使图标在工作区矩形中居中
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// 绘制图标
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		/*CRect rect;
		CWnd* pWid = GetDlgItem(IDC_STATIC_CANVAS);
		pWid->GetClientRect(rect);//把控件的长宽、坐标等信息保存在rect里
		TRACE("=====widt=%d====", rect.Width());
		TRACE("=====h=%d====", rect.Height());


		/*CDC* pdc = pWid->GetDC();
		pdc->Rectangle(&rect);
		////pdc->MoveTo(0, 0);
		//CPen penStroke(PS_SOLID,1,0x007700);
		//CPen *ppenPrevious = pdc->SelectObject(&penStroke);

		Graphics graphics( pdc->m_hDC );
		graphics.SetSmoothingMode(SmoothingModeAntiAlias);
		Pen pen(Color(255, 0, 0, 0), 3);
		graphics.DrawLine(&pen, 0,0,100,100);

		graphics.DrawLine(&pen, 10,0,500,100);*/
		CWnd* pWid = GetDlgItem(IDC_STATIC_CANVAS);
		CDC* pdc = pWid->GetDC();
		Graphics graphics( pdc->m_hDC );
		graphics.SetSmoothingMode(SmoothingModeAntiAlias);
		Pen pen(Color(255, 0, 0, 0), 3);
		for (std::list<sCanvasPointItem>::iterator it = m_listItems.begin(); 
			it != m_listItems.end(); ++it)
		{
			if (it->lstPoint.size() == 0)
			{
				continue;
			}
			int nSize = it->lstPoint.size() + 1;
			Point* pointSize = new Point[nSize];
			//PointF tmpPoint = item.beginPoint;
			//onbegin(ref tmpPoint);
			pointSize[0].X = it->beginPonit.x;
			pointSize[0].Y = it->beginPonit.y;
			int nCount = 0;
			for (std::list<CPoint>::iterator its = it->lstPoint.begin(); its != it->lstPoint.end(); ++its)
			{
				pointSize[nCount + 1].X = its->x;
				pointSize[nCount + 1].Y = its->y;
				++nCount;
			}

			graphics.DrawLines(&pen, pointSize, nSize);
			delete [] pointSize;
		}

		DeleteObject(pdc->m_hDC);
		CDialogEx::OnPaint();
	}
}

//当用户拖动最小化窗口时系统调用此函数取得光标
//显示。
HCURSOR CUSBHelperDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


// 获取所有设备
void CUSBHelperDlg::getAllDev( std::vector<CString>& vecDevPath, std::vector<CString>& vecDevName )
{
	/*static*/ std::vector<USB_DEV_INFO> usbInfo;
	usbInfo.clear();
	CUsbDevInterface* pObj = createObj();
	if (pObj == NULL)
	{
		return;
	}
	pObj->_getAllDevice(usbInfo);


	CString csDevPath;
	CString csDevName;
	USES_CONVERSION;
	for (int i =0; i < usbInfo.size(); ++i)
	{
		csDevPath.Format(_T("%s"), A2T(usbInfo[i].strDevPath.c_str()));
		csDevName.Format(_T("%s"), A2T(usbInfo[i].strDevName.c_str()));
		if (csDevName.Compare(_T("ROBOTPEN_DONGLE")) == 0 )			//--by zlp 2016/9/26
		{
			vecDevName.push_back(csDevName);
			vecDevPath.push_back(csDevPath);
			m_mapHidPath2ProductId[csDevPath] = usbInfo[i].nProductNum;
		}
	}
	return;
}

// 
void CUSBHelperDlg::insertUsbDevInfoToListContrl( std::vector<CString>& vecDevPath, std::vector<CString>& vecDevName )
{
	CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_USB_DEVICE));
	if (NULL == pListView)
	{
		return;
	}
	for (int i = 0; i < vecDevName.size(); ++i)
	{
		pListView->InsertItem(i, vecDevName[i]);
		pListView->SetItemText(i, 1, vecDevPath[i]);
	}
	pListView->EnsureVisible(pListView->GetItemCount()-1,FALSE); 
}

// 扫描到的设备
void CUSBHelperDlg::insertScanDevice(const int& nDeviveNum, unsigned char* pDeviceName)
{
	CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_SCAN_DEV));
	if (NULL == pListView)
	{
		return;
	}
	CString csNum;
	csNum.Format(_T("%d"), nDeviveNum);

	std::wstring strName = MultiCharToWideChar(std::string((char*)pDeviceName));
	CString csName(strName.c_str());
	//csName.Format(_T("%s"), pDeviceName);
	int nItemCount = pListView->GetItemCount();
	pListView->InsertItem(nItemCount, csNum);
	pListView->SetItemText(nItemCount, 1, csName);
}

void CUSBHelperDlg::initListCtrlInfo()
{
	CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_USB_DEVICE));
	if (NULL == pListView)
	{
		return;
	}

	pListView->InsertColumn(0, _T("设备名称"), LVCFMT_LEFT, 160, 0);
	pListView->InsertColumn(1, _T("设备路径"), LVCFMT_LEFT, 565, 1);
	pListView->SetExtendedStyle (LVS_EX_FULLROWSELECT |LVS_EX_GRIDLINES );//设置扩展

	CListCtrl* pListView1 = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_SCAN_DEV));
	if (NULL == pListView1)
	{
		return;
	}
	pListView1->InsertColumn(0, _T("扫描到的设备NUM"), LVCFMT_LEFT, 110, 0);
	pListView1->InsertColumn(1, _T("扫描到的设备名称"), LVCFMT_LEFT, 155, 1);
	pListView1->SetExtendedStyle (LVS_EX_FULLROWSELECT |LVS_EX_GRIDLINES );//设置扩展
}



void CUSBHelperDlg::OnBnClickedCancel()
{
	// TODO: 在此添加控件通知处理程序代码
	DestroyTextBuffer();
	CDialogEx::OnCancel();//*/
}


// 打开设备
void CUSBHelperDlg::OnBnClickedButton3Open()
{
	// TODO: 在此添加控件通知处理程序代码
	CUsbDevInterface* pObj = createObj();
	if (NULL == pObj)
	{
		return;
	}

	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	if (csBtnTitle.Compare(_T("关闭设备")) == 0)
	{
		pObj->closeUsbDev();
		SetDlgItemText(IDC_BUTTON3_OPEN, _T("打开设备"));
		resetDevice();
		return;
	}

	//
	POSITION pos = ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetFirstSelectedItemPosition();
	if (pos == nullptr)
	{
		MessageBox(_T("请先选中设备!"), _T("提示"), IDOK);
		return;
	}

	int nItem = ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetNextSelectedItem(pos);
	CString csDevPath = ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetItemText(nItem, 1);

	std::vector<std::string> strVecDev;
	USES_CONVERSION;
	strVecDev.push_back(T2A(csDevPath));
	bool bOpenRes = pObj->_openSpecUsbDevByPid(strVecDev, getUsbData, (void*)this);
	if (!bOpenRes)
	{
		MessageBox(_T("设备打开失败!"));
		return;
	}

	// 显示设备已经打开
	SetDlgItemText(IDC_BUTTON3_OPEN, _T("关闭设备"));
	m_strCurrentDevPath = csDevPath;

	OnBnClickedButtonScan();				//--by zlp 2016/9/26
	Sleep(100);
	OnBnClickedButtonScan();
	/*Sleep(100);
	SendDongleCmd(DONGLE_VERSION);//*/

	return;
}

// 清空接收区
void CUSBHelperDlg::OnBnClickedButton1ClrRcv()
{
	// TODO: 在此添加控件通知处理程序代码
	SetDlgItemText(IDC_EDIT1_RECV, _T(""));
}

bool gParseUsbData(char* dest, unsigned char* src, int len, PEN_INFO& info)
{
	if(len > 320)
		return false;

	char* pTemp = dest;
	for (int i = 0; i < len; i++)
	{
		sprintf(dest, "%02X ", src[i]);
		dest += 3;
	}
	dest = pTemp;

	if(len >= 8)
	{
		info.nPens = src[1];
		info.nX = src[2];
		info.nX = ((int)src[3] << 8) | info.nX ;
		info.nY = src[4];
		info.nY = ((int)src[5] << 8) | info.nY;
		info.nPenP = src[6];
		info.nPenP = ((int)src[7] << 8) | info.nPenP;
	}
	else
		return false;
	return true;
}

void CALLBACK CUSBHelperDlg::getUsbData( const unsigned char* pUsbData, const sPenInfo& penInfo, void* context )
{
	CUSBHelperDlg* pDlg = static_cast<CUSBHelperDlg*>(context);
	// 解析数据结果
	if (NULL == pUsbData)
	{
		return;
	}

	int nIndentiFiter = pUsbData[1];
	if (nIndentiFiter == 128)
	{
		// 获取状态标识结果事件
		// 取状态
		int nStatus = pUsbData[4];
		int nDongleMode = pUsbData[5];
		if( nDongleMode == 1)
		{
			switch(nStatus)
			{
			case 0:
				{
					pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("待机"));
					pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);		//--by zlp 2016/9/26
					pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));
				}
				break;
			case 1:
				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("正在扫描..."));
				break;
			case 2:
				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("连接中"));
				break;
			case 3:
				{
					//--by zlp 2016/9/26
					POSITION pos = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetFirstSelectedItemPosition();
					if (pos == nullptr)
					{
						pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("已链接"));
					}
					else
					{
						int nItem = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetNextSelectedItem(pos);
						CString csNumId = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetItemText(nItem, 0);
						pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("已链接,设备Num：") + csNumId);
					}

					pDlg->GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(true);		//--by zlp 2016/9/26
					pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);		//--by zlp 2016/9/26
					pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));

				}
				break;
			case 4:
				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("正在断开链接"));
				break;
			case 5:
				break;
			case 7:
				{
					pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("蓝牙正在升级中..."));
					if(!pDlg->bIsDFU)
						pDlg->SendDongleCmd(BLUETOOTH_STOP_DFU);
				}
				break;
			default:
				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("获取设备状态出错"));
				break;
			}
			pDlg->setDeviceStatus(nStatus);
		}
		else if(nDongleMode == 2)
		{
			pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("DFU模式"));
		}
	}
	else if (nIndentiFiter == 130)
	{
		// 收到链接的设备

		pDlg->GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(true);

		// 获取设备id
		int nDevID = pUsbData[4];
		// 获取设备名称
		const unsigned char* pDevName  = pUsbData + 13;
		//Get Device addr
		unsigned char devAddr[6] = {0};		//--by zlp 2016/9/27
		memcpy(devAddr,pUsbData+7,6);

		unsigned char szBuf[50] = {0};
		sprintf((char*)szBuf,"%s(%02X%02X%02X%02X%02X%02X)",pDevName,devAddr[0],devAddr[1],devAddr[2],devAddr[3],devAddr[4],devAddr[5]);

		pDlg->insertScanDevice(nDevID, const_cast<UCHAR*>(szBuf));
		//pDlg->insertScanDevice(nDevID, const_cast<UCHAR*>(pDevName));
	}
	else if (nIndentiFiter == 0xb1)//134)
	{
		// 收到数据
		int nDataLenght = pUsbData[2];
		if (nDataLenght == 16)
		{
			unsigned char szArrayTemp[8] = {0};
			int nCount = 0;
			while(nCount < 8)
			{
				szArrayTemp[nCount] = pUsbData[nCount + 4];
				++nCount;
			}
			char* pSzData = new char[36];
			memset(pSzData, 0, 36);
			PEN_INFO info;
			gParseUsbData(pSzData, szArrayTemp, 8, info);
			//pDlg->setRecvData(pSzData);
			pDlg->onRecvData(info);

			nCount = 0;
			while(nCount < 8)
			{
				szArrayTemp[nCount] = pUsbData[nCount + 12];
				++nCount;
			}
			memset(pSzData, 0, 36);
			gParseUsbData(pSzData, szArrayTemp, 8, info);
			//pDlg->setRecvData(pSzData);
			pDlg->onRecvData(info);
			delete pSzData;
		}
		else
		{
			unsigned char szArrayTemp[8] = {0};
			int nCount = 0;
			while(nCount < 8)
			{
				szArrayTemp[nCount] = pUsbData[nCount + 4];
				++nCount;
			}
			char* pSzData = new char[36];
			memset(pSzData, 0, 36);
			PEN_INFO info;
			gParseUsbData(pSzData, szArrayTemp, 8, info);
			//pDlg->setRecvData(pSzData);
			pDlg->onRecvData(info);
			delete pSzData;
		}
	}
	else
		pDlg->ParseDFUData(pUsbData);
	return;
}

void CUSBHelperDlg::setRecvData(const char* pData)
{

	USES_CONVERSION;
	CString csData = A2T(pData);
	//CUSBHelperDlg* pDlg = static_cast<CUSBHelperDlg*>(context);
	CString csOldData;
	GetDlgItemText(IDC_EDIT1_RECV, csOldData);
	csOldData += ( csData + _T("\r\n"));
	SetDlgItemText(IDC_EDIT1_RECV, csOldData);
	//pDlg->UpdateData(FALSE);
	((CEdit*)(GetDlgItem(IDC_EDIT1_RECV)))->LineScroll(((CEdit*)(GetDlgItem(IDC_EDIT1_RECV)))->GetLineCount()-1,0);
}

void CUSBHelperDlg::onRecvData(PEN_INFO& pendataInfo)
{
	static int nFlags = 0;
	CPoint point(pendataInfo.nX, pendataInfo.nY);
	if (pointIsInvalid(pendataInfo.nPens, point))
	{
		if (pendataInfo.nPens == 17 && nFlags == 0)  // 笔接触到板子
		{
			nFlags = 1;
			compressPoint(point);
			onbegin(point);
		}
		else if (pendataInfo.nPens == 17 && nFlags == 1)
		{
			compressPoint(point);
			onDrawing(point);
			moveCursor(point);
		}
		else if ((pendataInfo.nPens == 16 || pendataInfo.nPens == 0) && nFlags == 1)   // 笔离开板子
		{
			//if (nFlags == 1)
			endTrack(true);
			//else
			//endTrack(false);
			nFlags = 0;
		}


		if (pendataInfo.nPens == 16)
		{
			compressPoint(point);
			moveCursor(point);
		}
	}
}

void CUSBHelperDlg::moveCursor(CPoint& pos)
{
	//CPoint
	/*::ClientToScreen(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &pos);
	::SetCursorPos(pos.x, pos.y);//*/			//by zlp 2016/09/28
}

double nCompress = (double)(14335 / 868);
//8191*14335

void CUSBHelperDlg::compressPoint(CPoint& point)
{
	int nx = (point.x / nCompress);
	int ny = (point.y / nCompress);
	point.x = nx;
	point.y = ny;
}

bool CUSBHelperDlg::pointIsInvalid( int nPenStatus, CPoint& pointValue )
{
	if ((m_point == pointValue ) && (m_nPenStatus == nPenStatus))
		return false;
	m_point = pointValue;
	m_nPenStatus = nPenStatus;
	return true;
}

void CUSBHelperDlg::OnBnClickedButton4CloseDevice()
{
	// TODO: 在此添加控件通知处理程序代码
	CUsbDevInterface* pObj = createObj();
	if(NULL == pObj)
		return;
	pObj->closeUsbDev();
	return;
}

// 数据
void CUSBHelperDlg::OnBnClickedButton5SendData()
{
	// TODO: 在此添加控件通知处理程序代码
	// 获取将要发送的数据
	CString csSendData;
	GetDlgItemText(IDC_EDIT2_SEND_DATA, csSendData);
	if (csSendData.IsEmpty())
	{
		MessageBox(_T("请先填写要发送的数据"));
		return;
	}
	USES_CONVERSION;
	char* pSendData = T2A(csSendData);
	CUsbDevInterface* pObj = createObj();
	//pObj->sendDataToDevice(pSendData);
}


void CUSBHelperDlg::OnBnClickedButton2ClrSend()
{
	// TODO: 在此添加控件通知处理程序代码
	SetDlgItemText(IDC_EDIT2_SEND_DATA, _T(""));
}

void CUSBHelperDlg::OnNMClickListUsbDevice(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMITEMACTIVATE pNMItemActivate = reinterpret_cast<LPNMITEMACTIVATE>(pNMHDR);
	// TODO: 在此添加控件通知处理程序代码
	int nSelectItem = pNMItemActivate->iItem;
	// 获取设备路径
	CListCtrl* pListCtrl = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_USB_DEVICE));
	if (NULL == pListCtrl)
	{
		return;
	}

	CString csDeviceTemp = pListCtrl->GetItemText(nSelectItem, 1);
	int nPid = m_mapHidPath2ProductId[csDeviceTemp];
	/*HANDLE hDev = CreateFile(csDeviceTemp, GENERIC_WRITE, FILE_SHARE_WRITE, NULL, OPEN_EXISTING, 0, NULL);
	if (INVALID_HANDLE_VALUE == hDev)
	{
	return;
	}
	PCHAR rootHubName =GetRootHubName(hDev);*/
	PUSBDEVICEINFO info = EnumerateHostControllers(nPid);
	ResetTextBuffer();
	if (info == NULL)
	{
		return;
	}

	DisplayConnectionInfo(info->ConnectionInfo);
	SetDlgItemText(IDC_EDIT3_SHOW, TextBuffer);
	*pResult = 0;
}

PCHAR CUSBHelperDlg::GetRootHubName( HANDLE HostController )
{
	BOOL                success;
	ULONG               nBytes;
	USB_ROOT_HUB_NAME   rootHubName;
	PUSB_ROOT_HUB_NAME  rootHubNameW;
	PCHAR               rootHubNameA;

	rootHubNameW = NULL;
	rootHubNameA = NULL;

	// Get the length of the name of the Root Hub attached to the
	// Host Controller
	//
	success = DeviceIoControl(HostController,
		IOCTL_USB_GET_ROOT_HUB_NAME,
		0,
		0,
		&rootHubName,
		sizeof(rootHubName),
		&nBytes,
		NULL);

	if (!success)
	{
		//OOPS();
		DWORD dwErrorCode = GetLastError();
		goto GetRootHubNameError;
	}

	// Allocate space to hold the Root Hub name
	//
	nBytes = rootHubName.ActualLength;

	rootHubNameW = (PUSB_ROOT_HUB_NAME)ALLOC(nBytes);

	if (rootHubNameW == NULL)
	{
		//OOPS();
		goto GetRootHubNameError;
	}

	// Get the name of the Root Hub attached to the Host Controller
	//
	success = DeviceIoControl(HostController,
		IOCTL_USB_GET_ROOT_HUB_NAME,
		NULL,
		0,
		rootHubNameW,
		nBytes,
		&nBytes,
		NULL);

	if (!success)
	{
		//OOPS();
		goto GetRootHubNameError;
	}

	// Convert the Root Hub name
	//
	//rootHubNameA = WideStrToMultiStr(rootHubNameW->RootHubName);
	rootHubNameA = WideStrToMultiStr(rootHubNameW->RootHubName);

	// All done, free the uncoverted Root Hub name and return the
	// converted Root Hub name
	//
	FREE(rootHubNameW);

	return rootHubNameA;


GetRootHubNameError:
	// There was an error, free anything that was allocated
	//
	if (rootHubNameW != NULL)
	{
		FREE(rootHubNameW);
		rootHubNameW = NULL;
	}

	return NULL;
}

PUSBDEVICEINFO CUSBHelperDlg::EnumerateHostControllers(const int& nPid)
{
	TCHAR        HCName[16];
	int         HCNum;
	HANDLE      hHCDev;
	HTREEITEM   hHCItem;
	PCHAR       rootHubName;
	wchar_t*       leafName;
	PUSBDEVICEINFO info = NULL;

	// Iterate over some Host Controller names and try to open them.
	//
	USES_CONVERSION;
	for (int HCNum = 0; HCNum < 10; HCNum++)
	{
		wsprintf(HCName, TEXT("\\\\.\\HCD%d"), HCNum);

		hHCDev = CreateFile(HCName,
			GENERIC_WRITE,
			FILE_SHARE_WRITE,
			NULL,
			OPEN_EXISTING,
			0,
			NULL);

		// If the handle is valid, then we've successfully opened a Host
		// Controller.  Display some info about the Host Controller itself,
		// then enumerate the Root Hub attached to the Host Controller.
		//

		if (hHCDev == INVALID_HANDLE_VALUE)
		{
			DWORD dwErrorCode = GetLastError();
			dwErrorCode = dwErrorCode;
		}

		if (hHCDev != INVALID_HANDLE_VALUE)
		{
			wchar_t*  driverKeyName, deviceDesc;

			driverKeyName = A2T(GetHCDDriverKeyName(hHCDev));

			leafName = HCName + sizeof("\\\\.\\") - sizeof("");

			/*if (driverKeyName)
			{
			deviceDesc = DriverNameToDeviceDesc(driverKeyName);

			if (deviceDesc)
			{
			leafName = deviceDesc;
			}

			FREE(driverKeyName);
			}*/
			rootHubName = GetRootHubName(hHCDev);
			if (rootHubName != NULL)
			{
				int nRes = 0;
				info = EnumerateHub(nPid, rootHubName,NULL, NULL,"RootHub", nRes);
				if (nRes == 1)
				{
					CloseHandle(hHCDev);
					break;
				}
			}
			CloseHandle(hHCDev);
		}
	}
	return info;
	//*DevicesConnected = TotalDevicesConnected;
}

PCHAR CUSBHelperDlg::GetHCDDriverKeyName( HANDLE HCD )
{

	BOOL                    success;
	ULONG                   nBytes;
	USB_HCD_DRIVERKEY_NAME  driverKeyName;
	PUSB_HCD_DRIVERKEY_NAME driverKeyNameW;
	PCHAR                   driverKeyNameA;

	driverKeyNameW = NULL;
	driverKeyNameA = NULL;

	// Get the length of the name of the driver key of the HCD
	//
	success = DeviceIoControl(HCD,
		IOCTL_GET_HCD_DRIVERKEY_NAME,
		&driverKeyName,
		sizeof(driverKeyName),
		&driverKeyName,
		sizeof(driverKeyName),
		&nBytes,
		NULL);

	if (!success)
	{
		//OOPS();
		goto GetHCDDriverKeyNameError;
	}

	// Allocate space to hold the driver key name
	//
	nBytes = driverKeyName.ActualLength;

	if (nBytes <= sizeof(driverKeyName))
	{
		//OOPS();
		goto GetHCDDriverKeyNameError;
	}

	driverKeyNameW = (PUSB_HCD_DRIVERKEY_NAME)ALLOC(nBytes);

	if (driverKeyNameW == NULL)
	{
		//OOPS();
		goto GetHCDDriverKeyNameError;
	}

	// Get the name of the driver key of the device attached to
	// the specified port.
	//
	success = DeviceIoControl(HCD,
		IOCTL_GET_HCD_DRIVERKEY_NAME,
		driverKeyNameW,
		nBytes,
		driverKeyNameW,
		nBytes,
		&nBytes,
		NULL);

	if (!success)
	{
		//OOPS();
		goto GetHCDDriverKeyNameError;
	}

	// Convert the driver key name
	//
	//driverKeyNameA = WideStrToMultiStr(driverKeyNameW->DriverKeyName);
	USES_CONVERSION;
	driverKeyNameA = T2A(driverKeyNameW->DriverKeyName);

	// All done, free the uncoverted driver key name and return the
	// converted driver key name
	//
	FREE(driverKeyNameW);

	return driverKeyNameA;


GetHCDDriverKeyNameError:
	// There was an error, free anything that was allocated
	//
	if (driverKeyNameW != NULL)
	{
		FREE(driverKeyNameW);
		driverKeyNameW = NULL;
	}

	return NULL;
}

PUSBDEVICEINFO CUSBHelperDlg::EnumerateHub(const int& nPid, PCHAR HubName,PUSB_NODE_CONNECTION_INFORMATION ConnectionInfo, PUSB_DESCRIPTOR_REQUEST ConfigDesc,PCHAR DeviceDesc,int& nRes )
{
	HANDLE          hHubDevice;
	PCHAR           deviceName;
	BOOL            success;
	ULONG           nBytes;
	PUSBDEVICEINFO  info;
	CHAR            leafName[512]; // XXXXX how big does this have to be?

	// Initialize locals to not allocated state so the error cleanup routine
	// only tries to cleanup things that were successfully allocated.
	//
	info        = NULL;
	hHubDevice  = INVALID_HANDLE_VALUE;
	CString csDeviceName;
	// Allocate some space for a USBDEVICEINFO structure to hold the
	// hub info, hub name, and connection info pointers.  GPTR zero
	// initializes the structure for us.
	//
	info = (PUSBDEVICEINFO) ALLOC(sizeof(USBDEVICEINFO));

	if (info == NULL)
	{
		//OOPS();
		goto EnumerateHubError;
	}

	// Keep copies of the Hub Name, Connection Info, and Configuration
	// Descriptor pointers
	//
	info->HubName = HubName;

	info->ConnectionInfo = ConnectionInfo;

	info->ConfigDesc = ConfigDesc;

	//info->StringDescs = StringDescs;


	// Allocate some space for a USB_NODE_INFORMATION structure for this Hub,
	//
	info->HubInfo = (PUSB_NODE_INFORMATION)ALLOC(sizeof(USB_NODE_INFORMATION));

	if (info->HubInfo == NULL)
	{
		//OOPS();
		goto EnumerateHubError;
	}

	// Allocate a temp buffer for the full hub device name.
	//
	deviceName = (PCHAR)ALLOC(strlen(HubName) + sizeof("\\\\.\\"));

	if (deviceName == NULL)
	{
		//OOPS();
		goto EnumerateHubError;
	}

	// Create the full hub device name
	//
	strcpy(deviceName, "\\\\.\\");
	strcpy(deviceName + sizeof("\\\\.\\") - 1, info->HubName);

	USES_CONVERSION;
	csDeviceName = A2T(deviceName);
	// Try to hub the open device
	//
	hHubDevice = CreateFile(csDeviceName,
		GENERIC_WRITE,
		FILE_SHARE_WRITE,
		NULL,
		OPEN_EXISTING,
		0,
		NULL);

	// Done with temp buffer for full hub device name
	//
	FREE(deviceName);

	if (hHubDevice == INVALID_HANDLE_VALUE)
	{
		//OOPS();
		goto EnumerateHubError;
	}

	//
	// Now query USBHUB for the USB_NODE_INFORMATION structure for this hub.
	// This will tell us the number of downstream ports to enumerate, among
	// other things.
	//
	success = DeviceIoControl(hHubDevice,
		IOCTL_USB_GET_NODE_INFORMATION,
		info->HubInfo,
		sizeof(USB_NODE_INFORMATION),
		info->HubInfo,
		sizeof(USB_NODE_INFORMATION),
		&nBytes,
		NULL);

	if (!success)
	{
		//OOPS();
		goto EnumerateHubError;
	}

	// Build the leaf name from the port number and the device description
	//
	if (ConnectionInfo)
	{
		//wsprintf(leafName, "[Port%d] ", ConnectionInfo->ConnectionIndex);
		//strcat(leafName, ConnectionStatuses[ConnectionInfo->ConnectionStatus]);
		//strcat(leafName, " :  ");
	}
	else
	{
		leafName[0] = 0;
	}

	if (DeviceDesc)
	{

		strcat(leafName, DeviceDesc);
	}
	else
	{
		strcat(leafName, info->HubName);
	}

	// Now add an item to the TreeView with the PUSBDEVICEINFO pointer info
	// as the LPARAM reference value containing everything we know about the
	// hub.
	//

	// Now recursively enumrate the ports of this hub.
	//
	bool bRes = EnumerateHubPorts(nPid, hHubDevice, info);
	CloseHandle(hHubDevice);
	nRes = bRes ? 1 : 0;
	return info;

EnumerateHubError:
	//
	// Clean up any stuff that got allocated
	//

	if (hHubDevice != INVALID_HANDLE_VALUE)
	{
		CloseHandle(hHubDevice);
		hHubDevice = INVALID_HANDLE_VALUE;
	}

	if (info != NULL)
	{
		if (info->HubName != NULL)
		{
			FREE(info->HubName);
			info->HubName = NULL;
		}

		if (info->HubInfo != NULL)
		{
			FREE(info->HubInfo);
			info->HubInfo;
		}

		FREE(info);
		info = NULL;
	}

	if (ConnectionInfo)
	{
		FREE(ConnectionInfo);
	}

	if (ConfigDesc)
	{
		FREE(ConfigDesc);
	}

	/*if (StringDescs != NULL)
	{
	PSTRING_DESCRIPTOR_NODE Next;

	do {

	Next = StringDescs->Next;
	FREE(StringDescs);
	StringDescs = Next;

	} while (StringDescs != NULL);
	}*/
}

bool CUSBHelperDlg::EnumerateHubPorts(const int& nPid, HANDLE hHubDevice, PUSBDEVICEINFO info )
{
	ULONG       index;
	BOOL        success;

	PUSB_NODE_CONNECTION_INFORMATION    connectionInfo;
	PUSB_DESCRIPTOR_REQUEST             configDesc;
	//PSTRING_DESCRIPTOR_NODE             stringDescs;
	//PUSBDEVICEINFO                      info;

	PCHAR driverKeyName;
	PCHAR deviceDesc;
	CHAR  leafName[512]; // XXXXX how big does this have to be?


	// Loop over all ports of the hub.
	//
	// Port indices are 1 based, not 0 based.
	//
	for (index=1; index <= info->HubInfo->u.HubInformation.HubDescriptor.bNumberOfPorts; index++)
	{
		ULONG nBytes;

		// Allocate space to hold the connection info for this port.
		// For now, allocate it big enough to hold info for 30 pipes.
		//
		// Endpoint numbers are 0-15.  Endpoint number 0 is the standard
		// control endpoint which is not explicitly listed in the Configuration
		// Descriptor.  There can be an IN endpoint and an OUT endpoint at
		// endpoint numbers 1-15 so there can be a maximum of 30 endpoints
		// per device configuration.
		//
		// Should probably size this dynamically at some point.
		//
		nBytes = sizeof(USB_NODE_CONNECTION_INFORMATION) +
			sizeof(USB_PIPE_INFO) * 30;

		connectionInfo = (PUSB_NODE_CONNECTION_INFORMATION)ALLOC(nBytes);

		if (connectionInfo == NULL)
		{
			//OOPS();
			break;
		}

		//
		// Now query USBHUB for the USB_NODE_CONNECTION_INFORMATION structure
		// for this port.  This will tell us if a device is attached to this
		// port, among other things.
		//
		connectionInfo->ConnectionIndex = index;

		success = DeviceIoControl(hHubDevice,
			IOCTL_USB_GET_NODE_CONNECTION_INFORMATION,
			connectionInfo,
			nBytes,
			connectionInfo,
			nBytes,
			&nBytes,
			NULL);

		if (!success)
		{
			FREE(connectionInfo);
			continue;
		}

		// Update the count of connected devices
		//
		if (connectionInfo->ConnectionStatus == DeviceConnected)
		{
			//TotalDevicesConnected++;
		}

		if (connectionInfo->DeviceIsHub)
		{
			//TotalHubs++;
		}

		// If there is a device connected, get the Device Description
		//
		deviceDesc = NULL;
		/*if (connectionInfo->ConnectionStatus != NoDeviceConnected)
		{
		driverKeyName = GetDriverKeyName(hHubDevice,
		index);

		if (driverKeyName)
		{
		deviceDesc = DriverNameToDeviceDesc(driverKeyName);

		FREE(driverKeyName);
		}
		}*/

		// If there is a device connected to the port, try to retrieve the
		// Configuration Descriptor from the device.
		//
		/*if (gDoConfigDesc &&
		connectionInfo->ConnectionStatus == DeviceConnected)
		{
		configDesc = GetConfigDescriptor(hHubDevice,
		index,
		0);
		}
		else
		{
		configDesc = NULL;
		}

		if (configDesc != NULL &&
		AreThereStringDescriptors(&connectionInfo->DeviceDescriptor,
		(PUSB_CONFIGURATION_DESCRIPTOR)(configDesc+1)))
		{
		stringDescs = GetAllStringDescriptors(
		hHubDevice,
		index,
		&connectionInfo->DeviceDescriptor,
		(PUSB_CONFIGURATION_DESCRIPTOR)(configDesc+1));
		}
		else
		{
		stringDescs = NULL;
		}

		// If the device connected to the port is an external hub, get the
		// name of the external hub and recursively enumerate it.
		//
		if (connectionInfo->DeviceIsHub)
		{
		PCHAR extHubName;

		extHubName = GetExternalHubName(hHubDevice,
		index);

		if (extHubName != NULL)
		{
		EnumerateHub(hTreeParent, //hPortItem,
		extHubName,
		connectionInfo,
		configDesc,
		stringDescs,
		deviceDesc);

		// On to the next port
		//
		continue;
		}
		}
		*/
		// Allocate some space for a USBDEVICEINFO structure to hold the
		// hub info, hub name, and connection info pointers.  GPTR zero
		// initializes the structure for us.
		//
		//info = (PUSBDEVICEINFO) ALLOC(sizeof(USBDEVICEINFO));

		if (info == NULL)
		{
			//OOPS();
			if (configDesc != NULL)
			{
				FREE(configDesc);
			}
			FREE(connectionInfo);
			break;
		}

		info->ConnectionInfo = connectionInfo;

		//info->ConfigDesc = configDesc;

		//info->StringDescs = stringDescs;

		//wsprintf(leafName, "[Port%d] ", index);

		//strcat(leafName, ConnectionStatuses[connectionInfo->ConnectionStatus]);

		if (deviceDesc)
		{
			strcat(leafName, " :  ");
			strcat(leafName, deviceDesc);
		}
		if (connectionInfo->DeviceDescriptor.idProduct == nPid)
		{
			return true;
		}
	}
	return false;
}

void CUSBHelperDlg::DisplayConnectionInfo( PUSB_NODE_CONNECTION_INFORMATION ConnectInfo )
{
	if (ConnectInfo->ConnectionStatus == NoDeviceConnected)
	{
		AppendTextBuffer(_T("ConnectionStatus: NoDeviceConnected\r\n"));
	}
	else
	{
		PCHAR VendorString;

		AppendTextBuffer(_T("Device Descriptor:\r\n"));

		AppendTextBuffer(_T("bcdUSB:             0x%04X\r\n"),
			ConnectInfo->DeviceDescriptor.bcdUSB);

		AppendTextBuffer(_T("bDeviceClass:         0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.bDeviceClass);

		AppendTextBuffer(_T("bDeviceSubClass:      0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.bDeviceSubClass);

		AppendTextBuffer(_T("bDeviceProtocol:      0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.bDeviceProtocol);

		AppendTextBuffer(_T("bMaxPacketSize0:      0x%02X (%d)\r\n"),
			ConnectInfo->DeviceDescriptor.bMaxPacketSize0,
			ConnectInfo->DeviceDescriptor.bMaxPacketSize0);

		VendorString = GetVendorString(ConnectInfo->DeviceDescriptor.idVendor);

		if (VendorString != NULL)
		{
			AppendTextBuffer(_T("idVendor:           0x%04X (%s)\r\n"),
				ConnectInfo->DeviceDescriptor.idVendor,
				VendorString);
		}
		else
		{
			AppendTextBuffer(_T("idVendor:           0x%04X\r\n"),
				ConnectInfo->DeviceDescriptor.idVendor);
		}

		AppendTextBuffer(_T("idProduct:          0x%04X\r\n"),
			ConnectInfo->DeviceDescriptor.idProduct);

		AppendTextBuffer(_T("bcdDevice:          0x%04X\r\n"),
			ConnectInfo->DeviceDescriptor.bcdDevice);

		AppendTextBuffer(_T("iManufacturer:        0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.iManufacturer);

		if (ConnectInfo->DeviceDescriptor.iManufacturer)
		{
			/*DisplayStringDescriptor(ConnectInfo->DeviceDescriptor.iManufacturer,
			StringDescs);*/
		}

		AppendTextBuffer(_T("iProduct:             0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.iProduct);

		if (ConnectInfo->DeviceDescriptor.iProduct)
		{
			/*DisplayStringDescriptor(ConnectInfo->DeviceDescriptor.iProduct,
			StringDescs);*/
		}

		AppendTextBuffer(_T("iSerialNumber:        0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.iSerialNumber);

		if (ConnectInfo->DeviceDescriptor.iSerialNumber)
		{
			/*DisplayStringDescriptor(ConnectInfo->DeviceDescriptor.iSerialNumber,
			StringDescs);*/
		}

		AppendTextBuffer(_T("bNumConfigurations:   0x%02X\r\n"),
			ConnectInfo->DeviceDescriptor.bNumConfigurations);

		/*AppendTextBuffer("\r\nConnectionStatus: %s\r\n",
		ConnectionStatuses[ConnectInfo->ConnectionStatus]);*/

		AppendTextBuffer(_T("Current Config Value: 0x%02X\r\n"),
			ConnectInfo->CurrentConfigurationValue);

		if (ConnectInfo->LowSpeed)
		{
			AppendTextBuffer(_T("Device Bus Speed:      Low\r\n"));
		}
		else
		{
			AppendTextBuffer(_T("Device Bus Speed:     Full\r\n"));
		}

		AppendTextBuffer(_T("Device Address:       0x%02X\r\n"),
			ConnectInfo->DeviceAddress);

		AppendTextBuffer(_T("Open Pipes:             %2d\r\n"),
			ConnectInfo->NumberOfOpenPipes);

		if (ConnectInfo->NumberOfOpenPipes)
		{
			DisplayPipeInfo(ConnectInfo->NumberOfOpenPipes,
				ConnectInfo->PipeList);
		}
	}
}

BOOL CUSBHelperDlg::CreateTextBuffer()
{
	TextBuffer = (TCHAR*)ALLOC(BUFFERALLOCINCREMENT);

	if (TextBuffer == NULL)
	{
		//OOPS();

		return FALSE;
	}

	TextBufferLen = BUFFERALLOCINCREMENT;

	// Reset the buffer position and terminate the buffer
	//
	*TextBuffer = 0;
	TextBufferPos = 0;

	return TRUE;
}

void CUSBHelperDlg::DestroyTextBuffer()
{
	if (TextBuffer != NULL)
	{
		FREE(TextBuffer);

		TextBuffer = NULL;
	}
}

BOOL CUSBHelperDlg::ResetTextBuffer()
{
	// Fail if the text buffer has not been allocated
	//
	if (TextBuffer == NULL)
	{
		//OOPS();

		return FALSE;
	}

	// Reset the buffer position and terminate the buffer
	//
	*TextBuffer = 0;
	TextBufferPos = 0;

	return TRUE;
}

void CUSBHelperDlg::DisplayPipeInfo( ULONG NumPipes, USB_PIPE_INFO *PipeInfo )
{
	ULONG i;

	for (i=0; i<NumPipes; i++)
	{
		DisplayEndpointDescriptor(&PipeInfo[i].EndpointDescriptor);
	}
}

void CUSBHelperDlg::DisplayEndpointDescriptor (PUSB_ENDPOINT_DESCRIPTOR EndpointDesc)
{

	AppendTextBuffer(_T("\r\nEndpoint Descriptor:\r\n"));

	AppendTextBuffer(_T("bEndpointAddress:     0x%02X\r\n"),
		EndpointDesc->bEndpointAddress);

	switch (EndpointDesc->bmAttributes & 0x03)
	{
	case 0x00:
		AppendTextBuffer(_T("Transfer Type:     Control\r\n"));
		break;

	case 0x01:
		AppendTextBuffer(_T("Transfer Type: Isochronous\r\n"));
		break;

	case 0x02:
		AppendTextBuffer(_T("Transfer Type:        Bulk\r\n"));
		break;

	case 0x03:
		AppendTextBuffer(_T("Transfer Type:   Interrupt\r\n"));
		break;

	}

	AppendTextBuffer(_T("wMaxPacketSize:     0x%04X (%d)\r\n"),
		EndpointDesc->wMaxPacketSize,
		EndpointDesc->wMaxPacketSize);

	if (EndpointDesc->bLength == sizeof(USB_ENDPOINT_DESCRIPTOR))
	{
		AppendTextBuffer(_T("bInterval:            0x%02X\r\n"),
			EndpointDesc->bInterval);
	}
	else
	{
		/*PUSB_ENDPOINT_DESCRIPTOR2 endpointDesc2;

		endpointDesc2 = (PUSB_ENDPOINT_DESCRIPTOR2)EndpointDesc;

		AppendTextBuffer("wInterval:          0x%04X\r\n",
		endpointDesc2->wInterval);

		AppendTextBuffer("bSyncAddress:         0x%02X\r\n",
		endpointDesc2->bSyncAddress);*/
	}

}

PCHAR CUSBHelperDlg::GetVendorString( USHORT idVendor )
{
	PUSBVENDORID vendorID;

	if (idVendor != 0x0000)
	{
		vendorID = USBVendorIDs;

		while (vendorID->usVendorID != 0x0000)
		{
			if (vendorID->usVendorID == idVendor)
			{
				return WideStrToMultiStr((vendorID->szVendor));
			}
			vendorID++;
		}
	}

	return NULL;
}

// 扫描设备
void CUSBHelperDlg::OnBnClickedButtonScan()
{
	// TODO: 在此添加控件通知处理程序代码
	CUsbDevInterface* pObj = createObj();
	if (NULL == pObj)
	{
		return;
	}

	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	if (csBtnTitle.Compare(_T("关闭设备")) != 0)
	{
		MessageBox(_T("请先链接设备!"));
		return;
	}

	unsigned char* pData = new unsigned char[5];
	pData[0] = 0xAA;
	pData[1] = 0x80;
	pData[2] = 0;
	pData[3] = 0;

	sSendDataTag tag;
	tag.szData = pData;
	tag.nDataSize = 4;

	pObj->sendDataToDevice(tag);
}


void CUSBHelperDlg::OnMouseMove(UINT nFlags, CPoint point)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值
	CPoint point1; 
	GetCursorPos(&point1); 

	CRect rect; 
	::GetClientRect(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &rect); 

	//然后把当前鼠标坐标转为相对于rect的坐标。 
	::ScreenToClient(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &point1); 


	if (rect.PtInRect(point1))  // 点是否在该矩形区域中
	{
		//MessageBox(_T("yes"));
		onDrawing(point1);
	}

	CDialogEx::OnMouseMove(nFlags, point);
}


void CUSBHelperDlg::OnLButtonDown(UINT nFlags, CPoint point)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值
	//
	//CRect rect;
	//((CWnd*)GetDlgItem(IDC_STATIC_CANVAS))->GetClientRect(rect);
	CPoint point1; 
	GetCursorPos(&point1); 

	CRect rect; 
	::GetClientRect(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &rect); 

	//然后把当前鼠标坐标转为相对于rect的坐标。 
	::ScreenToClient(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &point1); 


	if (rect.PtInRect(point1))  // 点是否在该矩形区域中
	{
		//MessageBox(_T("yes"));
		onbegin(point1);
	}
	CDialogEx::OnLButtonDown(nFlags, point);
}


void CUSBHelperDlg::OnLButtonUp(UINT nFlags, CPoint point)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值
	endTrack();
	CDialogEx::OnLButtonUp(nFlags, point);
}

void CUSBHelperDlg::onbegin( const CPoint& pos )
{
	m_bDrawing = true;
	m_lastPoint = pos;
	m_currentItem.beginPonit = pos;
	m_currentItem.lstPoint.clear();
}

void CUSBHelperDlg::onDrawing( const CPoint& pos )
{
	if (!m_bDrawing)
		return;
	doDrawing(pos);
	m_currentItem.lstPoint.push_back(pos);
}

void CUSBHelperDlg::onEnd()
{
	m_bDrawing = false;
}

void CUSBHelperDlg::doDrawing( const CPoint& pos )
{
	CRect rect;
	CWnd* pWid = GetDlgItem(IDC_STATIC_CANVAS);
	CDC* pdc = pWid->GetDC();
	if (pdc == NULL)
	{
		DWORD dw = ::GetLastError();
		dw = dw;
		return;
	}

	Graphics graphics( pdc->m_hDC );
	graphics.SetSmoothingMode(SmoothingModeAntiAlias);
	Pen pen(Color(255, 0, 0, 0), 3);

	graphics.DrawLine(&pen, m_lastPoint.x, m_lastPoint.y, pos.x, pos.y);
	m_lastPoint = pos;
	//delete pdc;
	DeleteObject(pdc->m_hDC);
}

void CUSBHelperDlg::endTrack( bool bSave /*= true*/ )
{
	onEnd();
	//QVector<QSharedPointer<CanvasItem>> items;
	//items.push_back(m_currentItem);
	//addItems(items);
	//m_currentItem.clear();
	m_listItems.push_back(m_currentItem);
}

// 发送扫描命令
void CUSBHelperDlg::OnBnClickedButtonSndScan()
{
	// TODO: 在此添加控件通知处理程序代码
	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON_SND_SCAN,csBtnTitle);
	if (csBtnTitle.Compare(_T("停止扫描")) == 0)
	{
		GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));
		SendDongleCmd(DONGLE_TYPE::STOP_SCAN);
	}
	else
	{
		CUsbDevInterface* pObj = createObj();
		if (NULL == pObj)
		{
			return;
		}

		//CString csBtnTitle;
		GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
		if (csBtnTitle.Compare(_T("关闭设备")) != 0)
		{
			MessageBox(_T("请先链接设备!"));
			return;
		}

		CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_SCAN_DEV));
		if (NULL == pListView)
		{
			return;
		}
		pListView->DeleteAllItems();

		unsigned char* pData = new unsigned char[5];
		pData[0] = 0xAA;
		pData[1] = 0x81;
		pData[2] = 0;
		pData[3] = 0;

		sSendDataTag tag;
		tag.szData = pData;
		tag.nDataSize = 4;

		pObj->sendDataToDevice(tag);

		GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("停止扫描"));
		//GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);		//--by zlp 2016/9/26
	}
}

// 设备连接
void CUSBHelperDlg::OnBnClickedButtonConnect()
{
	// TODO: 在此添加控件通知处理程序代码
	CUsbDevInterface* pObj = createObj();
	if (NULL == pObj)
	{
		return;
	}

	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	if (csBtnTitle.Compare(_T("关闭设备")) != 0)
	{
		MessageBox(_T("请先链接设备!"));
		return;
	}

	POSITION pos = ((CListCtrl*)GetDlgItem(IDC_LIST_SCAN_DEV))->GetFirstSelectedItemPosition();
	if (pos == nullptr)
	{
		MessageBox(_T("请先选中设备!"), _T("提示"), IDOK);
		return;
	}

	if (m_nDevStatus == 3)
	{
		MessageBox(_T("无效的连接!"), _T("提示"), IDOK);
		return;
	}

	int nItem = ((CListCtrl*)GetDlgItem(IDC_LIST_SCAN_DEV))->GetNextSelectedItem(pos);
	CString csNumId = ((CListCtrl*)GetDlgItem(IDC_LIST_SCAN_DEV))->GetItemText(nItem, 0);
	int nId =_ttoi(csNumId);

	unsigned char* pData = new unsigned char[5];
	pData[0] = 0xAA;
	pData[1] = 0x83;
	pData[2] = 1;   // 包数据长度
	pData[3] = 0;
	pData[4] = nId;

	sSendDataTag tag;
	tag.szData = pData;
	tag.nDataSize = 5;

	pObj->sendDataToDevice(tag);

	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);		//--by zlp 2016/9/26
}

// 清空画布
void CUSBHelperDlg::OnBnClickedButtonClrCanvas()
{
	// TODO: 在此添加控件通知处理程序代码
	m_listItems.clear();
	Invalidate();
}

// 断开连接
void CUSBHelperDlg::OnBnClickedButtonClsConnect()
{
	// TODO: 在此添加控件通知处理程序代码
	CUsbDevInterface* pObj = createObj();
	if (NULL == pObj)
	{
		return;
	}

	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	if (csBtnTitle.Compare(_T("关闭设备")) != 0 || m_nDevStatus != 3)
	{
		MessageBox(_T("无效的断开!"));
		return;
	}

	CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_SCAN_DEV));
	if (NULL == pListView)
	{
		return;
	}
	pListView->DeleteAllItems();

	unsigned char* pData = new unsigned char[5];
	pData[0] = 0xAA;
	pData[1] = 0x84;
	pData[2] = 0;   // 包数据长度
	pData[3] = 0;
	pData[4] = 0;

	sSendDataTag tag;
	tag.szData = pData;
	tag.nDataSize = 4;

	pObj->sendDataToDevice(tag);

	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(true);			//--by zlp 2016/9/26
	GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);
	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);		//--by zlp 2016/9/26
}


void CUSBHelperDlg::OnDestroy()		//--by zlp 2016/9/26
{
	CDialogEx::OnDestroy();

	// TODO: 在此处添加消息处理程序代码
	if(NULL != m_hDeviceNotify)
	{
		UnregisterDeviceNotification(m_hDeviceNotify);
		m_hDeviceNotify = NULL;
	}

	resetDevice();

	Sleep(100);
}

void CUSBHelperDlg::resetDevice()
{
	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	if (!(csBtnTitle.Compare(_T("关闭设备")) != 0 || m_nDevStatus != 3))
	{
		OnBnClickedButtonClsConnect();
	}
	else
	{
		CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_SCAN_DEV));
		if (NULL == pListView)
		{
			return;
		}
		pListView->DeleteAllItems();
	}
	if (csBtnTitle.Compare(_T("关闭设备")) == 0 )
	{
		OnBnClickedButton3Open();
	}

	GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowTextW(_T(""));

	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);		//--by zlp 2016/9/26

	m_strCurrentDevPath = _T("");

	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);
	GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);

	GetDlgItemText(IDC_BUTTON_UPDATE,csBtnTitle);
	if (csBtnTitle.Compare(_T("取消更新")) == 0)
	{
		OnBnClickedButtonUpdate();
		//SetDlgItemText(IDC_BUTTON_UPDATE, _T("更新"));
	}

	nIndex = 0;
	nCount = 0;
	bEnd = false;
	bRawDataState = false;

	GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));

}
//热拔插事件
BOOL CUSBHelperDlg::OnDeviceChange(UINT nEventType,DWORD dwData)    //--by zlp 2016/9/27
{    
	//DEV_BROADCAST_DEVICEINTERFACE* dbd = (DEV_BROADCAST_DEVICEINTERFACE*) dwData;  
	PDEV_BROADCAST_HDR devHdr;  
	PDEV_BROADCAST_DEVICEINTERFACE devInterface;  
	switch (nEventType)    
	{    
	case DBT_DEVICEREMOVECOMPLETE://移除设备 
	case DBT_DEVICEARRIVAL://添加设备    
		{
			//restet listctrl
			CListCtrl* pListView = static_cast<CListCtrl*>(GetDlgItem(IDC_LIST_USB_DEVICE));
			if (NULL != pListView)
				pListView->DeleteAllItems();
			//get all device 
			std::vector<CString> vecDevPath;
			std::vector<CString> vecDevName;
			getAllDev(vecDevPath, vecDevName);
			insertUsbDevInfoToListContrl(vecDevPath, vecDevName);
			//当设备拔出的时候需要做判断
			CString csBtnTitle;
			GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
			if(nEventType == DBT_DEVICEREMOVECOMPLETE && csBtnTitle.Compare(_T("关闭设备")) == 0)
			{
				bool bNeedReset = true;
				for (int i = 0; i < vecDevName.size(); ++i)
				{
					if (vecDevPath[i] == m_strCurrentDevPath)
					{
						bNeedReset = false;
						break;
					}
				}
				if(bNeedReset)
					OnBnClickedButton3Open();
			}
		}
		break;    

	default:    
		break;    
	}    

	return TRUE;    
}  
//发送命令
void CUSBHelperDlg::SendDongleCmd(DONGLE_TYPE type,int nLength,const unsigned char* pBuf)
{
	CUsbDevInterface* pObj = createObj();
	if (NULL == pObj)
	{
		return;
	}

	unsigned char* pData = new unsigned char[4+nLength];
	pData[0] = 0xAA;
	pData[1] = type;
	pData[2] = nLength;   // 包数据长度
	pData[3] = 0;

	if(nLength > 0)
	{
		memcpy(pData+4,pBuf,nLength);
	}

	sSendDataTag tag;
	tag.szData = pData;
	tag.nDataSize = 4 + nLength;

	pObj->sendDataToDevice(tag);
}
//解析命令
void CUSBHelperDlg::ParseDFUData(const unsigned char* pUsbData)
{
	if (NULL == pUsbData)
	{
		return;
	}

	int nIndentiFiter = pUsbData[1];

	switch(nIndentiFiter)
	{
	case DONGLE_TYPE::START_DFU:
		{
			int nMode = pUsbData[5];
			TRACE("Mode:%d",nMode);
		}
		break;
	case DONGLE_TYPE::OTA_INFO:
		{
			//break;
			/*CUsbDevInterface* pObj = createObj();
			if (NULL == pObj)
			{
			return;
			}

			unsigned char* pData = new unsigned char[15];
			pData[0] = 0xAA;
			pData[1] = OTA_INFO;
			pData[2] = 8;   // 包数据长度
			pData[3] = 0;
			pData[4] = 0;
			pData[5] = 1;
			pData[6] = 0;
			pData[7] = 0;

			int size = GetFileSize(_T("E:\\Robot\\dongle_mcu_app.bin"));

			sprintf((char*)pData+8,"%d",size);

			sSendDataTag tag;
			tag.szData = pData;
			tag.nDataSize = 4 + 8;

			pObj->sendDataToDevice(tag);//*/

			int max_length = 8;
			unsigned char *buf = new unsigned char[max_length];
			memset(buf,0,max_length);
			buf[0] = m_McuFirmware.nVesion4;
			buf[1] = m_McuFirmware.nVesion3;
			buf[2] = m_McuFirmware.nVesion2;
			buf[3] = m_McuFirmware.nVesion;
			int size = GetFileSize(strUpdatePath);
			//sprintf((char*)buf+4,"%d",size);
			memcpy(buf+4,&size,4);
			this->SendDongleCmd(OTA_INFO,max_length,buf);
		}
		break;
	case DONGLE_TYPE::RAW_DATA:
		{
			TRACE(_T("RAW_DATA\n"));

			CString str;
			GetDlgItem(IDC_BUTTON_UPDATE)->GetWindowText(str);
			if(str == "更新")
			{
				Sleep(100);
				SendDongleCmd(DONGLE_TYPE::STOP_DFU);
				Sleep(100);
				SendDongleCmd(DONGLE_STATUS);
			}

			if(!bRawDataState)
			{
				if(bEnd)
				{
					//bRawDataState = false;
					fclose(fp);
					this->SendDongleCmd(RAW_DATA);
					nIndex = 0;
					nCount = 0;
					bEnd = false;
				}
				else
				{
					bRawDataState = true;
					fp = fopen(WideStrToMultiStr(strUpdatePath.GetBuffer()),"rb");
					nIndex = 0;
					nSize = GetFileSize(strUpdatePath);
					int max_length = 60;
					nCount = nSize/max_length;
					nCheckSum = 0;
				}
			}

			if(bRawDataState)
			{
				if(fp)
				{
					int max_length = 60;
					if(nIndex < nCount)
					{
						//TRACE("---%d---\n",nIndex);
						nIndex++;
					}
					else if(nIndex == nCount)
					{
						bRawDataState = false;
						max_length = nSize%max_length;
						bEnd = true;
					}
					unsigned char *buf = new unsigned char[max_length];
					fread(buf,sizeof(unsigned char),max_length,fp);
					for(int j=0;j<max_length;j++)
					{
						nCheckSum += buf[j];
					}

					this->SendDongleCmd(RAW_DATA,max_length,buf);

					CString str;
					int per = nIndex*100/nCount;
					str.Format(_T("正在升级mcu,已完成%d%%"),per);
					GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(str);
				}
			}

			/*int size = GetFileSize(_T(FIRMWARE_FILE));
			FILE *fp = fopen(FIRMWARE_FILE,"rb");
			int max_length = 60;
			if(fp)
			{
			unsigned char *buf = new unsigned char[max_length];
			int count = size/max_length;
			int last_size = size%max_length;
			for(int i=0;i<=count;i++)
			{
			if(i==count)
			max_length = last_size;
			fread(buf,sizeof(unsigned char),max_length,fp);
			for(int j=0;j<max_length;j++)
			{
			nCheckSum += buf[j];
			}
			this->SendDongleCmd(RAW_DATA,max_length,buf);
			}
			//send finish
			this->SendDongleCmd(RAW_DATA);

			fclose(fp);
			}//*/
		}
		break;
	case DONGLE_TYPE::CHECK_SUM:
		{
			TRACE(_T("CHECK_SUM\n"));
			/*CUsbDevInterface* pObj = createObj();
			if (NULL == pObj)
			{
			return;
			}

			unsigned char* pData = new unsigned char[15];
			pData[0] = 0xAA;
			pData[1] = CHECK_SUM;
			pData[2] = 4;   // 包数据长度
			pData[3] = 0;

			sprintf((char*)pData+4,"%d",nCheckSum);

			sSendDataTag tag;
			tag.szData = pData;
			tag.nDataSize = 4 + 4;

			pObj->sendDataToDevice(tag);//*/
			int max_length = 4;
			unsigned char *buf = new unsigned char[max_length];
			memset(buf,0,max_length);
			//sprintf((char*)buf,"%d",nCheckSum);
			memcpy(buf,&nCheckSum,4);
			this->SendDongleCmd(CHECK_SUM,max_length,buf);
			GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("正在校验．．．"));
		}
		break;
	case DONGLE_TYPE::STOP_DFU:
		{
			TRACE(_T("STOP_DFU\n"));
		}
		break;
	case DONGLE_TYPE::FIRMWARE_SWITCH:
		{
			TRACE(_T("FIRMWARE_SWITCH\n"));
			int nFirmwareStatus = pUsbData[4];
			if(nFirmwareStatus)
				GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("MCU升级成功！"));
			else
				GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("MCU升级失败！"));
			SetDlgItemText(IDC_BUTTON_UPDATE, _T("更新"));
			strUpdatePath = _T("");
			Sleep(200);
		}
		break;
	case DONGLE_TYPE::DONGLE_VERSION:
		{
			TRACE(_T("DONGLE_VERSION\n"));
			m_BleFirmware.sType = "ble";
			m_BleFirmware.nVesion4 = pUsbData[4];
			m_BleFirmware.nVesion3 = pUsbData[5];
			m_BleFirmware.nVesion2 = pUsbData[6];
			m_BleFirmware.nVesion = pUsbData[7];

			m_McuFirmware.sType = "mcu";
			m_McuFirmware.nVesion4 = pUsbData[8];
			m_McuFirmware.nVesion3 = pUsbData[9];
			m_McuFirmware.nVesion2 = pUsbData[10];
			m_McuFirmware.nVesion = pUsbData[11];

			GetFirmware();

			/*CString strVersion;
			strVersion.Format(_T("Bluetooth Version:%02X%02X%02X%02X,MCU Version:%02X%02X%02X%02X"),pUsbData[4],pUsbData[5],pUsbData[6],pUsbData[7],pUsbData[8],pUsbData[9],pUsbData[10],pUsbData[11]);
			GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(strVersion);

			strVersion += _T(",是否更新？");
			if(MessageBox(strVersion,_T("提示"),MB_ICONEXCLAMATION|MB_OKCANCEL)==IDOK)
			{
				bIsDFU = true;
				UpdateBluetooth();
				//UpdateFirmware();
				GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));
			}//*/
		}
		break;
	case BLUETOOTH_INFO:
		{
			int max_length = 8;
			unsigned char *buf = new unsigned char[max_length];
			memset(buf,0,max_length);
			buf[0] = m_BleFirmware.nVesion4;
			buf[1] = m_BleFirmware.nVesion3;
			buf[2] = m_BleFirmware.nVesion2;
			buf[3] = m_BleFirmware.nVesion;
			int size = GetFileSize(strUpdatePath);
			//sprintf((char*)buf+4,"%d",size);
			memcpy(buf+4,&size,4);
			this->SendDongleCmd(BLUETOOTH_INFO,max_length,buf);
		}
		break;
	case BLUETOOTH_RAW_DATA:
		{
			TRACE(_T("BLUETOOTH_RAW_DATA\n"));

			CString str;
			GetDlgItem(IDC_BUTTON_UPDATE)->GetWindowText(str);
			if(str == "更新")
			{
				Sleep(100);
				SendDongleCmd(DONGLE_TYPE::BLUETOOTH_STOP_DFU);
				Sleep(100);
				SendDongleCmd(DONGLE_STATUS);
			}

			if(!bRawDataState)
			{
				if(bEnd)
				{
					//bRawDataState = false;
					fclose(fp);
					this->SendDongleCmd(BLUETOOTH_RAW_DATA);
					nIndex = 0;
					nCount = 0;
					bEnd = false;
				}
				else
				{
					bRawDataState = true;
					fp = fopen(WideStrToMultiStr(strUpdatePath.GetBuffer()),"rb");
					nIndex = 0;
					nSize = GetFileSize(strUpdatePath);
					int max_length = 60;
					nCount = nSize/max_length;
					nCheckSum = 0;
				}
			}

			if(bRawDataState)
			{
				if(fp)
				{
					int max_length = 60;
					if(nIndex < nCount)
					{
						//TRACE("---%d---\n",nIndex);
						nIndex++;
					}
					else if(nIndex == nCount)
					{
						bRawDataState = false;
						max_length = nSize%max_length;
						bEnd = true;
					}
					unsigned char *buf = new unsigned char[max_length];
					fread(buf,sizeof(unsigned char),max_length,fp);
					for(int j=0;j<max_length;j++)
					{
						nCheckSum += buf[j];
					}

					this->SendDongleCmd(BLUETOOTH_RAW_DATA,max_length,buf);

					CString str;
					int per = nIndex*100/nCount;
					str.Format(_T("正在升级Bluetooth,已完成%d%%"),per);
					GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(str);
				}
			}
		}
		break;
	case BLUETOOTH_CHECK_SUM:
		{
			TRACE(_T("CHECK_SUM\n"));
			int max_length = 4;
			unsigned char *buf = new unsigned char[max_length];
			memset(buf,0,max_length);
			//sprintf((char*)buf,"%d",nCheckSum);
			memcpy(buf,&nCheckSum,4);
			this->SendDongleCmd(BLUETOOTH_CHECK_SUM,max_length,buf);
			GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("正在校验．．．"));
		}
		break;
	case BLUETOOTH_STOP_DFU:
		break;
	case BLUETOOTH_RESULT:
		{
			TRACE(_T("BLUETOOTH_RESULT\n"));
			int nFirmwareStatus = pUsbData[4];
			if(nFirmwareStatus)
				GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("蓝牙升级成功！"));
			else
				GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(_T("蓝牙升级失败！"));
			SetDlgItemText(IDC_BUTTON_UPDATE, _T("更新"));
			strUpdatePath = _T("");
			Sleep(200);
		}
		break;
	default:
		break;
	}

}
//获取文件大小
int CUSBHelperDlg::GetFileSize(CString strFilePath)
{
	HANDLE handle = CreateFile(strFilePath, FILE_READ_EA, FILE_SHARE_READ, 0, OPEN_EXISTING, 0, 0);
	if (handle != INVALID_HANDLE_VALUE)
	{
		int size = ::GetFileSize(handle, NULL);
		CloseHandle(handle);
		return size;
	}
	return 0;
}
//更新按钮
void CUSBHelperDlg::OnBnClickedButtonUpdate()
{
	// TODO: 在此添加控件通知处理程序代码

	CString csBtnTitle;
	GetDlgItemText(IDC_BUTTON3_OPEN,csBtnTitle);
	/*if (csBtnTitle.Compare(_T("关闭设备")) != 0)
	{
	MessageBox(_T("请先链接设备!"));
	return;
	}//*/
	if(GetDlgItem(IDC_BUTTON_CLS_CONNECT)->IsWindowEnabled())
	{
		MessageBox(_T("请先断开设备!"));
		return;
	}


	GetDlgItemText(IDC_BUTTON_UPDATE,csBtnTitle);
	if (csBtnTitle.Compare(_T("取消更新")) == 0)
	{
		Sleep(100);
		SendDongleCmd(DONGLE_TYPE::STOP_DFU);
		Sleep(100);
		SendDongleCmd(DONGLE_STATUS);
		SetDlgItemText(IDC_BUTTON_UPDATE, _T("更新"));
		Sleep(100);
		bRawDataState = false;
		bEnd = true;
		bIsDFU = false;
		return;
	}
	else
	{
		SendDongleCmd(DONGLE_VERSION);
	}
}
//升级mcu
void CUSBHelperDlg::UpdateFirmware()
{
	if(strUpdatePath.IsEmpty())
	{
		MessageBox(_T("升级文件为空！"));
		return;
	}
	SendDongleCmd(DONGLE_TYPE::START_DFU);
	SetDlgItemText(IDC_BUTTON_UPDATE, _T("取消更新"));
	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);
}
//升级bluetooth
void CUSBHelperDlg::UpdateBluetooth()
{
	if(strUpdatePath.IsEmpty())
	{
		MessageBox(_T("升级文件为空！"));
		return;
	}
	SendDongleCmd(DONGLE_TYPE::BLUETOOTH_ENTER);
	SetDlgItemText(IDC_BUTTON_UPDATE, _T("取消更新"));
	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);
}

void CUSBHelperDlg::GetFirmware()
{
	TCHAR szDir[MAX_PATH] = {0};
	GetModuleFileName(NULL, szDir, sizeof(szDir));
	CString sPath = szDir;
	int nPos; 
	nPos=sPath.ReverseFind('\\'); 
	sPath=sPath.Left(nPos); 

	CFileFind finder;
	bool bFind = finder.FindFile(sPath + _T("\\Firmware\\*.bin"));
	while(bFind)
	{
		bFind = finder.FindNextFile();
		CString sFirmwarePath = finder.GetFileName();
		Firmware_Info firmwareinfo;
		if(SpiltString(sFirmwarePath.Left(sFirmwarePath.GetLength()-4),firmwareinfo))
		{
			strUpdatePath = finder.GetFilePath();
			CString str;
			if(firmwareinfo.sType == "ble")
				str.Format(_T("类型：%s,当前版本：%d.%d.%d.%d,最新版本：%d.%d.%d.%d,是否升级？"),firmwareinfo.sType,m_BleFirmware.nVesion,m_BleFirmware.nVesion2,m_BleFirmware.nVesion3,m_BleFirmware.nVesion4
				,firmwareinfo.nVesion,firmwareinfo.nVesion2,firmwareinfo.nVesion3,firmwareinfo.nVesion4);
			else
				str.Format(_T("类型：%s,当前版本：%d.%d.%d.%d,最新版本：%d.%d.%d.%d,是否升级？"),firmwareinfo.sType,m_McuFirmware.nVesion,m_McuFirmware.nVesion2,m_McuFirmware.nVesion3,m_McuFirmware.nVesion4
				,firmwareinfo.nVesion,firmwareinfo.nVesion2,firmwareinfo.nVesion3,firmwareinfo.nVesion4);

			if(MessageBox(str,_T("提示"),MB_ICONEXCLAMATION|MB_OKCANCEL)==IDOK)
			{
				bIsDFU = true;
				if(firmwareinfo.sType == "ble")
				{
					m_BleFirmware.nVesion = firmwareinfo.nVesion;
					m_BleFirmware.nVesion2 = firmwareinfo.nVesion2;
					m_BleFirmware.nVesion3 = firmwareinfo.nVesion3;
					m_BleFirmware.nVesion4 = firmwareinfo.nVesion4;
					UpdateBluetooth();
				}
				else
				{	
					m_McuFirmware.nVesion = firmwareinfo.nVesion;
					m_McuFirmware.nVesion2 = firmwareinfo.nVesion2;
					m_McuFirmware.nVesion3 = firmwareinfo.nVesion3;
					m_McuFirmware.nVesion4 = firmwareinfo.nVesion4;
					UpdateFirmware();
				}
				GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);
				GetDlgItem(IDC_BUTTON_SND_SCAN)->SetWindowText(_T("开始扫描"));
			}

			break;
		}
		else
		{
			CString str;
			if(firmwareinfo.sType == "ble")
				str.Format(_T("类型：%s,当前版本：%d.%d.%d.%d,最新版本：%d.%d.%d.%d,无需更新"),firmwareinfo.sType,m_BleFirmware.nVesion,m_BleFirmware.nVesion2,m_BleFirmware.nVesion3,m_BleFirmware.nVesion4
				,firmwareinfo.nVesion,firmwareinfo.nVesion2,firmwareinfo.nVesion3,firmwareinfo.nVesion4);
			else
				str.Format(_T("类型：%s,当前版本：%d.%d.%d.%d,最新版本：%d.%d.%d.%d,无需更新"),firmwareinfo.sType,m_McuFirmware.nVesion,m_McuFirmware.nVesion2,m_McuFirmware.nVesion3,m_McuFirmware.nVesion4
				,firmwareinfo.nVesion,firmwareinfo.nVesion2,firmwareinfo.nVesion3,firmwareinfo.nVesion4);
			//MessageBox(str);
			GetDlgItem(IDC_STATIC_SCANTIP)->SetWindowText(str);

		}

	}
}

void CUSBHelperDlg::Split(CString source, CStringArray& dest, CString division)
{
	dest.RemoveAll();
	int pos = 0;
	pos = source.GetLength();
	pos = 0;
	int pre_pos = 0;
	while( -1 != pos )
	{
		pre_pos = pos;
		pos = source.Find(division, pos+1);
		CString str;
		if(pos > 0)
			str = source.Mid(pre_pos,(pos-pre_pos));
		else
			str = source.Mid(pre_pos,(source.GetLength()-pre_pos));
		str.Replace(division,NULL);
		dest.Add(str);
	}
}

bool CUSBHelperDlg::SpiltString(CString strFileName,Firmware_Info &firmwareinfo)
{
	CStringArray dest;
	Split(strFileName,dest,_T("_"));

	firmwareinfo.sDeviceName = dest[0];
	firmwareinfo.sType = dest[1];

	CStringArray dest2;
	Split(dest[2],dest2,_T("."));
	firmwareinfo.nVesion = _ttoi(dest2[0].Right(1).GetBuffer());
	firmwareinfo.nVesion2 = _ttoi(dest2[1].GetBuffer());
	firmwareinfo.nVesion3 = _ttoi(dest2[2].GetBuffer());
	firmwareinfo.nVesion4 = _ttoi(dest2[3].GetBuffer());

	//return true;

	if(firmwareinfo.sType == _T("ble"))
	{
		if( firmwareinfo.nVesion*10 + firmwareinfo.nVesion2 > m_BleFirmware.nVesion*10 + m_BleFirmware.nVesion2)
			return true;
		else if( firmwareinfo.nVesion*10 + firmwareinfo.nVesion2 == m_BleFirmware.nVesion*10 + m_BleFirmware.nVesion2)
		{
			if( firmwareinfo.nVesion3*10 + firmwareinfo.nVesion4 > m_BleFirmware.nVesion3*10 + m_BleFirmware.nVesion4)
				return true;
		}
		else
			return false;
	}
	else
	{
		if( firmwareinfo.nVesion*10 + firmwareinfo.nVesion2 > m_McuFirmware.nVesion*10 + m_McuFirmware.nVesion2)
			return true;
		else if( firmwareinfo.nVesion*10 + firmwareinfo.nVesion2 == m_McuFirmware.nVesion*10 + m_McuFirmware.nVesion2)
		{
			if( firmwareinfo.nVesion3*10 + firmwareinfo.nVesion4 > m_McuFirmware.nVesion3*10 + m_McuFirmware.nVesion4)
				return true;
		}
		else
			return false;
	}

	return false;
}
