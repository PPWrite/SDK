
// USBHelper.h : PROJECT_NAME 应用程序的主头文件
//

#pragma once

#ifndef __AFXWIN_H__
	#error "在包含此文件之前包含“stdafx.h”以生成 PCH 文件"
#endif

#include "resource.h"		// 主符号

#define APP_NAME _T("USBHelper")

// CUSBHelperApp:
// 有关此类的实现，请参阅 USBHelper.cpp
//

class CUSBHelperApp : public CWinApp
{
public:
	CUSBHelperApp();

// 重写
public:
	virtual BOOL InitInstance();
	virtual int ExitInstance(); // return app exit code

// 实现
private:
	GdiplusStartupInput gdiplusStartupInput;
	ULONG_PTR gdiplusToken;

	DECLARE_MESSAGE_MAP()
};

extern CUSBHelperApp theApp;