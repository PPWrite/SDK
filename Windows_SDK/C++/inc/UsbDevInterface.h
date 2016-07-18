#pragma once

#ifdef DEFINE_EXPORT
#define DLL_PORT_TYPE __declspec(dllexport)
#else
#define DLL_PORT_TYPE __declspec(dllimport)
#endif

#include <string>
#include <Windows.h>
#include <vector>

// usb设备信息
typedef  struct sUsbDevInfo
{
    std::string strDevName;             //  设备名称
    std::string strDevGuid;             //  设备GUID
    unsigned short nProductNum;         
    unsigned short nVersionNum;         
    unsigned short nVendorNum;          
    std::string strDevPath;             // 设备路径
}USB_DEV_INFO;

// 笔数据信息
typedef struct sPenInfo
{
    int nPens;  // 笔状态
    int nX;     // 笔x轴坐标
    int nY;     // 笔y轴坐标
    int nPenP;  // 笔压力
}PEN_INFO;

// 返回usb数据回调类型
typedef void (*usbDataCallBack)(const char* pUsbData, const sPenInfo& penInfo);
// 返回usb设备的插拔状态
typedef void (*usbStatusCallBack)(int nDevstatus, void* context);

class CUsbDevInterface
{
public:

    // ********************************************************
    // summary: 根据usb设备Pid打开指定的usb设备
    // fullname: _openSpecUsbDevByPid
    // access: pubilc
    // parameter: [IN] arryPid usb设备Pid的数组
    // parameter: [IN] pFunc 指定返回数据的回调 默认为null
    // return: bool 设备是否打开成功
    // ********************************************************
    virtual bool _openSpecUsbDevByPid(IN char* pAarryPid[], unsigned int nAarrySize, IN usbDataCallBack pFunc = NULL) = 0;

    // ********************************************************
    // summary: 根据usb设备Pid打开指定的usb设备
    // fullname: _openSpecUsbDevByPid
    // access: pubilc
    // parameter: [IN] vecPid设备Pid容器
    // parameter: [IN] pFunc 指定返回数据的回调 默认为null
    // return: bool 设备是否打开成功
    // ********************************************************
    virtual bool _openSpecUsbDevByPid(IN const std::vector<std::string>& vecPid, IN usbDataCallBack pFunc = NULL) = 0;

    // *******************************************
    // summary: 关闭设备资源
    // fullname: closeUsbDev
    // access: public
    // return: 是否关闭成功
    // *******************************************
    virtual bool closeUsbDev() = 0;

    // **********************************************
    // summary: 设置usb插拔状态回调
    // fullname: setDevStatusHandler
    // access: public
    // parameter: [IN] newHandler 回调地址
    // parameter: [IN] pUsbDevStatusContext 上下文指针
    // return: 上次回调地址
    // **********************************************
    virtual usbStatusCallBack setDevStatusHandler(usbStatusCallBack newHandler, void* pUsbDevStatusContext = NULL) = 0;
};

extern "C" DLL_PORT_TYPE CUsbDevInterface* WINAPI createObj();
extern "C" DLL_PORT_TYPE void WINAPI deleteObj(void* p);

//////////////////////////////////////////////////////////////////////////
/*打开设备接口*/
//////////////////////////////////////////////////////////////////////////
extern "C" DLL_PORT_TYPE bool WINAPI _extern_openSpecUsbDevByPid(char* pAarryPid[], unsigned int nAarrySize, usbDataCallBack pFunc);

//////////////////////////////////////////////////////////////////////////
/*关闭设备接口*/
//////////////////////////////////////////////////////////////////////////
extern "C" DLL_PORT_TYPE bool WINAPI _extern_CloseUsbDev();
 
