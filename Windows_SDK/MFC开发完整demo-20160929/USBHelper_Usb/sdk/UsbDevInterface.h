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
typedef void ( CALLBACK *usbDataCallBack)(const char* pUsbData, const sPenInfo& penInfo, void* context);
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
    virtual bool _openSpecUsbDevByPid(IN const std::vector<std::string>& vecPid, IN usbDataCallBack pFunc = NULL, IN void* context = NULL) = 0;

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

// createObj deleteObj说明
// createObj函数提供获取设备接口类的功能, 获取到设备对象后可调用设备相关接口
// deleteObj函数提供程序退出时释放相关资源
extern "C" DLL_PORT_TYPE CUsbDevInterface*  createObj();
extern "C" DLL_PORT_TYPE void  deleteObj(void* p);

// _extern_openSpecUsbDevByPid _extern_CloseUsbDev说明
// _extern_openSpecUsbDevByPid函数提供根据设备pid来打开设备, 无需使用上面的对象来调用相关接口
// _extern_CloseUsbDev函数提供关闭设备的功能
extern "C" DLL_PORT_TYPE bool  _extern_openSpecUsbDevByPid(char* pAarryPid[], unsigned int nAarrySize, usbDataCallBack pFunc);
extern "C" DLL_PORT_TYPE bool  _extern_CloseUsbDev();

// _extern_setDevStatusHandler 设备状态回调<插入或拔出>
extern "C" DLL_PORT_TYPE usbStatusCallBack _extern_setDevStatusHandler(usbStatusCallBack newHandler, void* pUsbDevStatusContext);
 
