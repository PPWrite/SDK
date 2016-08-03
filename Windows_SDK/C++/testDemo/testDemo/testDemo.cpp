// testDemo.cpp : 定义控制台应用程序的入口点。
//

#include "stdafx.h"
#include "UsbDevInterface.h"

void __stdcall data(const char* p, const sPenInfo& penInfo, void* context)
{
    printf("%s\n", p);

    if(penInfo.nPens == 0)
        printf("pen leave\n");
    else if(penInfo.nPens == 16)
        printf("pen detected\n");
    else if(penInfo.nPens == 17)
        printf("write down\n");

    printf("X point %d\n", penInfo.nX);
    printf("Y point %d\n", penInfo.nY);
    printf("Pen pressure %d\n", penInfo.nPenP);
}

// 动态加载  用最后一组接口 _extern_openSpecUsbDevByPid与_extern_CloseUsbDev
int dynamicLoadingDllOne()
{
	HMODULE hm = LoadLibraryA("usbDevModule.dll");
	if (NULL == hm)
	{
		return 0;
	}
	bool ( *pFunPro)(char* pAarryPid[], unsigned int nAarrySize, void* pFunc) = (bool(*)(char* pAarryPid[], unsigned int nAarrySize, void* pFunc))GetProcAddress(hm, "_extern_openSpecUsbDevByPid");
	if (pFunPro == NULL)
	{
		FreeLibrary(hm);
		return 0;
	}
	char *arry[2] = {"7806", "7805"};
	bool bOpenRes = pFunPro(arry, 2, data);
	if (!bOpenRes)
	{
		FreeLibrary(hm);
		return 0;
	}
	// 等待数据
	system("pause");

	// 获取关闭设备的接口
	bool (*pFunPro1)() = (bool (*)())GetProcAddress(hm, "_extern_CloseUsbDev");
	if (*pFunPro1 == NULL)
	{
		FreeLibrary(hm);
		return 0;
	}
	pFunPro1();
	FreeLibrary(hm);
	return 0;
}

// 动态加载  用第一组接口 createObj与deleteObj
int dynamicLoadingDllTwo()
{
	HMODULE hm = LoadLibraryA("usbDevModule.dll");
	if (NULL == hm)
	{
		return 0;
	}
	CUsbDevInterface* ( *pFunPro)() = (CUsbDevInterface* (*)())GetProcAddress(hm, "createObj");
	if (pFunPro == NULL)
	{
		FreeLibrary(hm);
		return 0;
	}
	char *arry[2] = {"7806", "7805"};
	CUsbDevInterface* pDev = pFunPro();
	if (NULL == pDev)
	{
		FreeLibrary(hm);
		return 0;
	}

	bool bOpenDevRes = pDev->_openSpecUsbDevByPid(arry, 2, data);
	if (!bOpenDevRes)
	{
		printf("open device failure\r\n");
		system("pause");
		FreeLibrary(hm);
		return 0;
	}
	// 等待数据
	system("pause");
	pDev->closeUsbDev();
	// 获取关闭设备的接口
	void (*pFunPro1)(void*) = (void (*)(void*))GetProcAddress(hm, "deleteObj");
	if (*pFunPro1 == NULL)
	{
		FreeLibrary(hm);
		return 0;
	}
	pFunPro1(pDev);
	FreeLibrary(hm);
	return 0;
}

// 静态加载
int staticLoadingDll()
{
	CUsbDevInterface* pusb = createObj();
	if (NULL == pusb)
		return 0;
	std::vector<std::string> vecPid;
	vecPid.push_back("7805");
	vecPid.push_back("7806");
	vecPid.push_back("7807");

	char *arry[2] = {"7806", "7805"};
	bool bOpen = pusb->_openSpecUsbDevByPid(arry, 2, data);
	if (!bOpen)
	{
		printf("open dev failed\n");
		system("pause");
		return 0;
	}

	system("pause");
	pusb->closeUsbDev();
	deleteObj(pusb);
	return 0;

	/*MSG msg = {0};
	while(msg.message != WM_QUIT) {
	if(PeekMessage(&msg, 0, 0, 0, PM_REMOVE)) {
	TranslateMessage(&msg);
	DispatchMessage(&msg);
	}else {
	//TODO, do this
	//printf("123\n");
	}
	}*/
	//m_pUsbDev->_openSpecUsbDevByPid("7805", getUsbData);
	//system("pause");
	/*if (cobj._openSpecUsbDevByPid("7805", data))
	{
	printf("open dev  success\n");
	system("pause");
	cobj.close();
	}
	else
	{
	printf("open dev  fail\n");
	system("pause");
	}*/
}

int  _tmain(int argc, _TCHAR* argv[])
{
	// 客户代码可根据自己需求选择相应的加载方式
	// 静态加载dll
	return staticLoadingDll();

	// 动态加载dll
	return dynamicLoadingDllOne();

	// 动态加载dll
	return dynamicLoadingDllTwo();
}

