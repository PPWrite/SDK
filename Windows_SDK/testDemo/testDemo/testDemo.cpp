// testDemo.cpp : 定义控制台应用程序的入口点。
//

#include "stdafx.h"
#include "UsbDevInterface.h"

void data(const char* p, const sPenInfo& penInfo)
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

int _tmain(int argc, _TCHAR* argv[])
{
    CUsbDevInterface* pusb = createObj();
    if (NULL == pusb)
        return 0;
    std::vector<std::string> vecPid;
    vecPid.push_back("7805");
    vecPid.push_back("7806");
    vecPid.push_back("7807");
    bool bOpen = pusb->_openSpecUsbDevByPid(vecPid, data);
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
	return 0;
}

