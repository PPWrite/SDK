
// USBHelper.h : PROJECT_NAME Ӧ�ó������ͷ�ļ�
//

#pragma once

#ifndef __AFXWIN_H__
	#error "�ڰ������ļ�֮ǰ������stdafx.h�������� PCH �ļ�"
#endif

#include "resource.h"		// ������

#define APP_NAME _T("USBHelper")

// CUSBHelperApp:
// �йش����ʵ�֣������ USBHelper.cpp
//

class CUSBHelperApp : public CWinApp
{
public:
	CUSBHelperApp();

// ��д
public:
	virtual BOOL InitInstance();
	virtual int ExitInstance(); // return app exit code

// ʵ��
private:
	GdiplusStartupInput gdiplusStartupInput;
	ULONG_PTR gdiplusToken;

	DECLARE_MESSAGE_MAP()
};

extern CUSBHelperApp theApp;