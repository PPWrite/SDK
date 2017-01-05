
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
	bIsPress(false)
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
	ON_WM_ERASEBKGND()
	ON_WM_TIMER()
	ON_WM_SIZE()
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

	SetTimer(0,100,NULL);
	/*insertScanDevice(1, (unsigned char*)"num100");
	insertScanDevice(2, (unsigned char*)"num200");//*/			//--by zlp 2016/9/26
	OnBnClickedButton3Open();
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

		Redraw();

		/*CWnd* pWid = GetDlgItem(IDC_STATIC_CANVAS);
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

		DeleteObject(pdc->m_hDC);//*/
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
	/*usbInfo.clear();
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
	}//*/
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
	CDialogEx::OnCancel();
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
		//resetDevice();
		return;
	}

	//
	/*POSITION pos = ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetFirstSelectedItemPosition();
	if (pos == nullptr)
	{
	MessageBox(_T("请先选中设备!"), _T("提示"), IDOK);
	return;
	}

	int nItem = ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetNextSelectedItem(pos);//*/
	CString csDevPath = _T("7805");//= ((CListCtrl*)GetDlgItem(IDC_LIST_USB_DEVICE))->GetItemText(nItem, 1);

	std::vector<std::string> strVecDev;
	USES_CONVERSION;
	strVecDev.push_back(T2A(_T("7805")));
	strVecDev.push_back(T2A(_T("7806")));
	strVecDev.push_back(T2A(_T("7807")));
	strVecDev.push_back(T2A(_T("7808")));
	bool bOpenRes = pObj->_openSpecUsbDevByPid(strVecDev, getUsbData, (void*)this);
	if (!bOpenRes)
	{
		MessageBox(_T("设备打开失败!"));
		return;
	}

	// 显示设备已经打开
	SetDlgItemText(IDC_BUTTON3_OPEN, _T("关闭设备"));
	/*m_strCurrentDevPath = csDevPath;//*/

	//OnBnClickedButtonScan();				//--by zlp 2016/9/26

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

void CALLBACK CUSBHelperDlg::getUsbData(const char* pUsbData, const sPenInfo& penInfo, void* context)
{
	CUSBHelperDlg* pDlg = static_cast<CUSBHelperDlg*>(context);
	// 解析数据结果
	if (NULL == pUsbData)
	{
		return;
	}
	pDlg->onRecvData((PEN_INFO)penInfo);

	return;

	///*int nIndentiFiter = pUsbData[1];
	//if (nIndentiFiter == 128)
	//{
	//	// 获取状态标识结果事件
	//	// 取状态
	//	/*int nStatus = pUsbData[4];
	//	switch(nStatus)
	//	{
	//	case 0:
	//		{
	//			pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("待机"));
	//			pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);		//--by zlp 2016/9/26
	//		}
	//		break;
	//	case 1:
	//		pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("正在扫描"));
	//		break;
	//	case 2:
	//		pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("连接中"));
	//		break;
	//	case 3:
	//		{
	//			//--by zlp 2016/9/26
	//			POSITION pos = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetFirstSelectedItemPosition();
	//			if (pos == nullptr)
	//			{
	//				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("已链接"));
	//			}
	//			else
	//			{
	//				int nItem = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetNextSelectedItem(pos);
	//				CString csNumId = ((CListCtrl*)(pDlg->GetDlgItem(IDC_LIST_SCAN_DEV)))->GetItemText(nItem, 0);
	//				pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("已链接,设备Num：") + csNumId);
	//			}

	//			pDlg->GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(true);
	//			pDlg->GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(true);		//--by zlp 2016/9/26

	//		}
	//		break;
	//	case 4:
	//		pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("正在断开链接"));
	//		break;
	//	case 5:
	//		break;
	//	default:
	//		pDlg->SetDlgItemText(IDC_STATIC_SCANTIP, _T("获取设备状态出错"));
	//		break;
	//	}
	//	pDlg->setDeviceStatus(nStatus);//*/
	//}
	//else if (nIndentiFiter == 130)
	//{
	//	// 收到链接的设备

	//	/*pDlg->GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(true);

	//	// 获取设备id
	//	int nDevID = pUsbData[4];
	//	// 获取设备名称
	//	const unsigned char* pDevName  = pUsbData + 13;
	//	//Get Device addr
	//	unsigned char devAddr[6] = {0};		//--by zlp 2016/9/27
	//	memcpy(devAddr,pUsbData+7,6);
	//	
	//	unsigned char szBuf[50] = {0};
	//	sprintf((char*)szBuf,"%s(%x%x%x%x%x%x)",pDevName,devAddr[0],devAddr[1],devAddr[2],devAddr[3],devAddr[4],devAddr[5]);

	//	pDlg->insertScanDevice(nDevID, const_cast<UCHAR*>(szBuf));//*/
	//	//pDlg->insertScanDevice(nDevID, const_cast<UCHAR*>(pDevName));
	//}

	//else if (nIndentiFiter == 134)
	//{
	//	// 收到数据
	//	//pDlg->onRecvData((PEN_INFO)penInfo);

	//	/*int nDataLenght = pUsbData[2];
	//	if (nDataLenght == 16)
	//	{
	//		unsigned char szArrayTemp[8] = {0};
	//		int nCount = 0;
	//		while(nCount < 8)
	//		{
	//			szArrayTemp[nCount] = pUsbData[nCount + 4];
	//			++nCount;
	//		}
	//		char* pSzData = new char[36];
	//		memset(pSzData, 0, 36);
	//		PEN_INFO info;
	//		gParseUsbData(pSzData, szArrayTemp, 8, info);
	//		//pDlg->setRecvData(pSzData);
	//		pDlg->onRecvData(info);

	//		nCount = 0;
	//		while(nCount < 8)
	//		{
	//			szArrayTemp[nCount] = pUsbData[nCount + 12];
	//			++nCount;
	//		}
	//		memset(pSzData, 0, 36);
	//		gParseUsbData(pSzData, szArrayTemp, 8, info);
	//		//pDlg->setRecvData(pSzData);
	//		pDlg->onRecvData(info);
	//		delete pSzData;
	//	}
	//	else
	//	{
	//		unsigned char szArrayTemp[8] = {0};
	//		int nCount = 0;
	//		while(nCount < 8)
	//		{
	//			szArrayTemp[nCount] = pUsbData[nCount + 4];
	//			++nCount;
	//		}
	//		char* pSzData = new char[36];
	//		memset(pSzData, 0, 36);
	//		PEN_INFO info;
	//		gParseUsbData(pSzData, szArrayTemp, 8, info);
	//		//pDlg->setRecvData(pSzData);
	//		pDlg->onRecvData(info);
	//		delete pSzData;
	//	}//*/
	//}//*/
	//return;
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
	::ClientToScreen(GetDlgItem(IDC_STATIC_CANVAS)->GetSafeHwnd(), &pos);
	//::SetCursorPos(pos.x, pos.y);
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
	/*CUsbDevInterface* pObj = createObj();
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

	pObj->sendDataToDevice(tag);//*/
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
		if(bIsPress)
			onDrawing(point1);
	}//*/

	/*if(bIsPress)
	{
	if (rect.PtInRect(point1))  // 点是否在该矩形区域中
	{
	if(m_bDrawing)
	onDrawing(point1);
	else
	onbegin(point1);
	}
	else
	{
	if(m_bDrawing)
	endTrack();
	}
	}//*/

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
		bIsPress = true;
		onbegin(point1);
		//beginTrack(point1);
	}
	CDialogEx::OnLButtonDown(nFlags, point);
}


void CUSBHelperDlg::OnLButtonUp(UINT nFlags, CPoint point)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值
	bIsPress = false;
	endTrack();
	/*endTrack(point);
	TRACE("==================================\n");//*/

	CDialogEx::OnLButtonUp(nFlags, point);
}

void CUSBHelperDlg::onbegin( const CPoint& pos )
{
	m_bDrawing = true;
	m_lastPoint = pos;
	m_currentItem.beginPonit = pos;
	m_currentItem.lstPoint.clear();

	m_lastPoints.clear();
	m_lastPoints.push_back(pos);
}

void CUSBHelperDlg::onDrawing( const CPoint& pos )
{
	if (!m_bDrawing)
		return;
	//doDrawing(pos);
	doDrawingBezier(pos);
	m_currentItem.lstPoint.push_back(pos);
}

void CUSBHelperDlg::onEnd()
{
	m_bDrawing = false;

	////////////////////////////////////////////////////////////////////////
	//CRect rect;
	//CWnd* pWid = GetDlgItem(IDC_STATIC_CANVAS);
	//CDC* pdc = pWid->GetDC();
	//if (pdc == NULL)
	//{
	//	DWORD dw = ::GetLastError();
	//	dw = dw;
	//	return;
	//}

	//Graphics graphics( pdc->m_hDC );
	//graphics.SetSmoothingMode(SmoothingModeAntiAlias);
	//Pen pen(Color(255, 255, 0, 0), 3);

	//Point points[2000];
	//for(int i=0;i<2000;i++)
	//{
	//	points[i].X = 0;
	//	points[i].Y = 0;
	//}
	///*points[1].X = m_currentItem.beginPonit.x;
	//points[1].Y = m_currentItem.beginPonit.y;//*/

	//CPoint m_lastPoint = m_currentItem.beginPonit;
	//m_lastPoints.clear();
	//points[0].X = m_currentItem.beginPonit.x;
	//points[0].Y = m_currentItem.beginPonit.y;
	//int count = 1;
	//for (std::list<CPoint>::iterator its = m_currentItem.lstPoint.begin(); its != m_currentItem.lstPoint.end(); ++its)
	//{
	//	CPoint pt;
	//	pt.x = its->x;
	//	pt.y = its->y;

	//	int width = 3;
	//	double speed = sqrt(pow((double)(pt.x - m_lastPoint.x), 2) + pow((double)(pt.y - m_lastPoint.y), 2));
	//	if (speed > width) 
	//	{
	//		m_lastPoint = pt;

	//		m_lastPoints.push_back(pt);

	//		points[count].X = its->x;
	//		points[count].Y = its->y;

	//		++count;
	//	}
	//}
	//for(int i=3;i<count;i++)
	//{
	//	graphics.DrawBezier(&pen,points[i-3],points[i-2],points[i-1],points[i]);
	//	/*std::vector<CPoint> cs(2);
	//	calculateCurveControlPoints(m_lastPoints[i-3], m_lastPoints[i-2], m_lastPoints[i-1], cs);
	//	CPoint c2 = cs[1];
	//	calculateCurveControlPoints(m_lastPoints[i-2], m_lastPoints[i-1], m_lastPoints[i], cs);
	//	CPoint c3 = cs[0];
	//	drawBezier(m_lastPoints[i-3], c2, c3, m_lastPoints[i-1]);//*/
	//}//*/
	////graphics.DrawBeziers(&pen,points,count);
	////delete pdc;
	//DeleteObject(pdc->m_hDC);//*/
	//////////////////////////////////////////////////////////////////////////////////////////
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

	Invalidate();
	/*UpdateWindow();
	Redraw();//*/
}

// 发送扫描命令
void CUSBHelperDlg::OnBnClickedButtonSndScan()
{
	// TODO: 在此添加控件通知处理程序代码
	/*CUsbDevInterface* pObj = createObj();
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

	pObj->sendDataToDevice(tag);//*/

	GetDlgItem(IDC_BUTTON_SND_SCAN)->EnableWindow(false);		//--by zlp 2016/9/26
}

// 设备连接
void CUSBHelperDlg::OnBnClickedButtonConnect()
{
	// TODO: 在此添加控件通知处理程序代码
	/*CUsbDevInterface* pObj = createObj();
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

	pObj->sendDataToDevice(tag);//*/

	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);		//--by zlp 2016/9/26
}

// 清空画布
void CUSBHelperDlg::OnBnClickedButtonClrCanvas()
{
	// TODO: 在此添加控件通知处理程序代码
	SetBkColor();
	m_listItems.clear();
	Invalidate();
}

// 断开连接
void CUSBHelperDlg::OnBnClickedButtonClsConnect()
{
	// TODO: 在此添加控件通知处理程序代码
	/*CUsbDevInterface* pObj = createObj();
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

	GetDlgItem(IDC_BUTTON_CONNECT)->EnableWindow(false);			//--by zlp 2016/9/26
	GetDlgItem(IDC_BUTTON_CLS_CONNECT)->EnableWindow(false);//*/
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

}

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

void CUSBHelperDlg::Redraw()
{
	RedrawMem();
	return;//*/

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
		int nCount = 1;
		CPoint m_lastPoint = it->beginPonit;
		for (std::list<CPoint>::iterator its = it->lstPoint.begin(); its != it->lstPoint.end(); ++its)
		{
			int width = 3;
			CPoint pt;
			pt.x = its->x;
			pt.y = its->y;
			double speed = sqrt(pow((double)(pt.x - m_lastPoint.x), 2) + pow((double)(pt.y - m_lastPoint.y), 2));
			if (speed > width) 
			{
				m_lastPoint = pt;

				pointSize[nCount].X = its->x;
				pointSize[nCount].Y = its->y;
				++nCount;
			}
		}
		for(int i=3;i<nCount;i++)
		{
			graphics.DrawBezier(&pen,pointSize[i-3],pointSize[i-2],pointSize[i-1],pointSize[i]);
			/*std::vector<CPoint> cs(2);
			calculateCurveControlPoints(m_lastPoints[i-3], m_lastPoints[i-2], m_lastPoints[i-1], cs);
			CPoint c2 = cs[1];
			calculateCurveControlPoints(m_lastPoints[i-2], m_lastPoints[i-1], m_lastPoints[i], cs);
			CPoint c3 = cs[0];
			drawBezier(m_lastPoints[i-3], c2, c3, m_lastPoints[i-1]);//*/
		}

		//graphics.DrawLines(&pen, pointSize, nSize);
		delete [] pointSize;
	}

	DeleteObject(pdc->m_hDC);//*/
}

void CUSBHelperDlg::doDrawingBezier(const CPoint& pos)
{
	m_lastPoints.push_back(pos);

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

	//graphics.DrawLine(&pen, m_lastPoint.x, m_lastPoint.y, pos.x, pos.y);


	if (m_lastPoints.size() > 3) 
	{
		//TRACE("DrawBezier\n");
		graphics.DrawBezier(&pen,m_lastPoints[0].x,m_lastPoints[0].y,m_lastPoints[1].x,m_lastPoints[1].y,m_lastPoints[2].x,m_lastPoints[2].y, m_lastPoints[3].x,m_lastPoints[3].y);
		m_lastPoints.erase(m_lastPoints.begin());
	}
	else if(m_lastPoints.size() == 2)
	{
		//TRACE("DrawLine\n");
		graphics.DrawLine(&pen,m_lastPoints[0].x,m_lastPoints[0].y,m_lastPoints[1].x,m_lastPoints[1].y);
	}
	else if(m_lastPoints.size() == 3)
	{
		//TRACE("DrawLine2\n");
		graphics.DrawLine(&pen,m_lastPoints[0].x,m_lastPoints[0].y,m_lastPoints[1].x,m_lastPoints[1].y);
		graphics.DrawLine(&pen,m_lastPoints[1].x,m_lastPoints[1].y,m_lastPoints[2].x,m_lastPoints[2].y);
	}

	m_lastPoint = pos;
	//delete pdc;
	DeleteObject(pdc->m_hDC);
}

void CUSBHelperDlg::RedrawMem()
{
	//RedrawBk();
	CWnd* pWnd = GetDlgItem(IDC_STATIC_CANVAS);
	CRect rc; // 定义一个矩形区域变量
	pWnd->GetClientRect(rc);
	int nWidth = rc.Width();
	int nHeight = rc.Height();

	CDC *pDC = pWnd->GetDC(); // 定义设备上下文
	CDC MemDC; // 定义一个内存显示设备对象
	CBitmap MemBitmap; // 定义一个位图对象

	//建立与屏幕显示兼容的内存显示设备
	MemDC.CreateCompatibleDC(pDC);
	//建立一个与屏幕显示兼容的位图，位图的大小可选用窗口客户区的大小
	MemBitmap.CreateCompatibleBitmap(pDC,nWidth,nHeight);
	//将位图选入到内存显示设备中，只有选入了位图的内存显示设备才有地方绘图，画到指定的位图上
	CBitmap *pOldBit = MemDC.SelectObject(&MemBitmap);
	//先用背景色将位图清除干净，否则是黑色。这里用的是白色作为背景
	MemDC.FillSolidRect(0,0,nWidth,nHeight,RGB(255,255,255));

	Graphics graphics(MemDC.GetSafeHdc());
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
		int nCount = 1;
		CPoint m_lastPoint = it->beginPonit;

		for (std::list<CPoint>::iterator its = it->lstPoint.begin(); its != it->lstPoint.end(); ++its)
		{
			int width = 0;//zlp set width 0
			CPoint pt;
			pt.x = its->x;
			pt.y = its->y;
			double speed = sqrt(pow((double)(pt.x - m_lastPoint.x), 2) + pow((double)(pt.y - m_lastPoint.y), 2));
			if (speed > width) 
			{
				m_lastPoint = pt;

				pointSize[nCount].X = its->x;
				pointSize[nCount].Y = its->y;
				++nCount;
			}
		}
		for(int i=3;i<nCount;i++)
		{
			graphics.DrawBezier(&pen,pointSize[i-3],pointSize[i-2],pointSize[i-1],pointSize[i]);
			//std::vector<CPoint> cs(2);
			//calculateCurveControlPoints(m_lastPoints[i-3], m_lastPoints[i-2], m_lastPoints[i-1], cs);
			//CPoint c2 = cs[1];
			//calculateCurveControlPoints(m_lastPoints[i-2], m_lastPoints[i-1], m_lastPoints[i], cs);
			//CPoint c3 = cs[0];
			//drawBezier(m_lastPoints[i-3], c2, c3, m_lastPoints[i-1]);
		}//*/

		/*if (nCount > 3) 
		{
			for(int i=3;i<nCount;i++)
			{
				graphics.DrawBezier(&pen,m_lastPoints[i-3].x,m_lastPoints[i-3].y,m_lastPoints[i-2].x,m_lastPoints[i-2].y,m_lastPoints[i-1].x,m_lastPoints[i-1].y, m_lastPoints[i].x,m_lastPoints[i].y);
			}
		}
		else if(nCount == 2)
		{
			graphics.DrawLine(&pen,m_lastPoints[0].x,m_lastPoints[0].y,m_lastPoints[1].x,m_lastPoints[1].y);
		}
		else if(nCount == 3)
		{
			graphics.DrawLine(&pen,m_lastPoints[0].x,m_lastPoints[0].y,m_lastPoints[1].x,m_lastPoints[1].y);
			graphics.DrawLine(&pen,m_lastPoints[1].x,m_lastPoints[1].y,m_lastPoints[2].x,m_lastPoints[2].y);
		}//*/

		//graphics.DrawLines(&pen, pointSize, nSize);
		delete [] pointSize;
	}

	//DeleteObject(pdc->m_hDC);//*/
	//绘图操作等在这里实现
	/*MemDC.MoveTo(……);
	MemDC.LineTo(……);
	MemDC.Ellipse(……);//*/

	//将内存中的图拷贝到屏幕上进行显示
	pDC->BitBlt(0,0,nWidth,nHeight,&MemDC,0,0,SRCCOPY);

	//绘图完成后的清理
	MemDC.SelectObject(pOldBit);
	MemBitmap.DeleteObject();

	DeleteObject(pDC->m_hDC);//*/
}

BOOL CUSBHelperDlg::OnEraseBkgnd(CDC* pDC)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	return TRUE;

	return CDialogEx::OnEraseBkgnd(pDC);
}

void CUSBHelperDlg::SetBkColor()
{
	CRect rect;
	GetClientRect(rect);  
	CDC* pDC = this->GetDC();
	pDC->FillSolidRect(rect,RGB(240,240,240));   
}


void CUSBHelperDlg::OnTimer(UINT_PTR nIDEvent)
{
	// TODO: 在此添加消息处理程序代码和/或调用默认值

	this->OnBnClickedButtonClrCanvas();
	KillTimer(0);

	CDialogEx::OnTimer(nIDEvent);
}


void CUSBHelperDlg::OnSize(UINT nType, int cx, int cy)
{
	CDialogEx::OnSize(nType, cx, cy);

	this->SetBkColor();

	// TODO: 在此处添加消息处理程序代码
}
