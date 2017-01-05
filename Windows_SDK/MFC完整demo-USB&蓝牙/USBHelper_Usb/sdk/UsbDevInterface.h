#pragma once

#ifdef DEFINE_EXPORT
#define DLL_PORT_TYPE __declspec(dllexport)
#else
#define DLL_PORT_TYPE __declspec(dllimport)
#endif

#include <string>
#include <Windows.h>
#include <vector>

// usb�豸��Ϣ
typedef  struct sUsbDevInfo
{
    std::string strDevName;             //  �豸����
    std::string strDevGuid;             //  �豸GUID
    unsigned short nProductNum;         
    unsigned short nVersionNum;         
    unsigned short nVendorNum;          
    std::string strDevPath;             // �豸·��
}USB_DEV_INFO;

// ��������Ϣ
typedef struct sPenInfo
{
    int nPens;  // ��״̬
    int nX;     // ��x������
    int nY;     // ��y������
    int nPenP;  // ��ѹ��
}PEN_INFO;

// ����usb���ݻص�����
typedef void ( CALLBACK *usbDataCallBack)(const char* pUsbData, const sPenInfo& penInfo, void* context);
// ����usb�豸�Ĳ��״̬
typedef void (*usbStatusCallBack)(int nDevstatus, void* context);

class CUsbDevInterface
{
public:

    // ********************************************************
    // summary: ����usb�豸Pid��ָ����usb�豸
    // fullname: _openSpecUsbDevByPid
    // access: pubilc
    // parameter: [IN] arryPid usb�豸Pid������
    // parameter: [IN] pFunc ָ���������ݵĻص� Ĭ��Ϊnull
    // return: bool �豸�Ƿ�򿪳ɹ�
    // ********************************************************
    virtual bool _openSpecUsbDevByPid(IN char* pAarryPid[], unsigned int nAarrySize, IN usbDataCallBack pFunc = NULL) = 0;

    // ********************************************************
    // summary: ����usb�豸Pid��ָ����usb�豸
    // fullname: _openSpecUsbDevByPid
    // access: pubilc
    // parameter: [IN] vecPid�豸Pid����
    // parameter: [IN] pFunc ָ���������ݵĻص� Ĭ��Ϊnull
    // return: bool �豸�Ƿ�򿪳ɹ�
    // ********************************************************
    virtual bool _openSpecUsbDevByPid(IN const std::vector<std::string>& vecPid, IN usbDataCallBack pFunc = NULL, IN void* context = NULL) = 0;

    // *******************************************
    // summary: �ر��豸��Դ
    // fullname: closeUsbDev
    // access: public
    // return: �Ƿ�رճɹ�
    // *******************************************
    virtual bool closeUsbDev() = 0;

    // **********************************************
    // summary: ����usb���״̬�ص�
    // fullname: setDevStatusHandler
    // access: public
    // parameter: [IN] newHandler �ص���ַ
    // parameter: [IN] pUsbDevStatusContext ������ָ��
    // return: �ϴλص���ַ
    // **********************************************
    virtual usbStatusCallBack setDevStatusHandler(usbStatusCallBack newHandler, void* pUsbDevStatusContext = NULL) = 0;
};

// createObj deleteObj˵��
// createObj�����ṩ��ȡ�豸�ӿ���Ĺ���, ��ȡ���豸�����ɵ����豸��ؽӿ�
// deleteObj�����ṩ�����˳�ʱ�ͷ������Դ
extern "C" DLL_PORT_TYPE CUsbDevInterface*  createObj();
extern "C" DLL_PORT_TYPE void  deleteObj(void* p);

// _extern_openSpecUsbDevByPid _extern_CloseUsbDev˵��
// _extern_openSpecUsbDevByPid�����ṩ�����豸pid�����豸, ����ʹ������Ķ�����������ؽӿ�
// _extern_CloseUsbDev�����ṩ�ر��豸�Ĺ���
extern "C" DLL_PORT_TYPE bool  _extern_openSpecUsbDevByPid(char* pAarryPid[], unsigned int nAarrySize, usbDataCallBack pFunc);
extern "C" DLL_PORT_TYPE bool  _extern_CloseUsbDev();

// _extern_setDevStatusHandler �豸״̬�ص�<�����γ�>
extern "C" DLL_PORT_TYPE usbStatusCallBack _extern_setDevStatusHandler(usbStatusCallBack newHandler, void* pUsbDevStatusContext);
 
