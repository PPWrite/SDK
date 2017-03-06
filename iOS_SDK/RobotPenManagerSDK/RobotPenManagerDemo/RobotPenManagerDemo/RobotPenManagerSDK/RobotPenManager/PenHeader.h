
#ifdef DEBUG

#define RobotLog(fmt, ...) NSLog((@"RobotBLE Log :   %s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);

#else

#define RobotLog(...)

#endif

/**USB P1纵向 宽度**/
#define VALUE_P1_WIDTH  17407.0f
/**USB P1纵向 高度**/
#define VALUE_P1_HEIGHT 10751.0f

/**BLE P7纵向 宽度**/
#define VALUE_P7_WIDTH 14335.0f
/**BLE P7纵向 高度**/
#define VALUE_P7_HEIGHT  8191.0f

/**BLE ELITE纵向 宽度**/
#define VALUE_ELITE_WIDTH  14335.0f
/**BLE ELITE纵向 高度**/
#define VALUE_ELITE_HEIGHT  8191.0f

/**BLE ELITE PLUS纵向 宽度**/
#define VALUE_ELITE_PLUS_WIDTH  22015.0f
/**BLE ELITE PLUS纵向 高度**/
#define VALUE_ELITE_PLUS_HEIGHT  15359.0f

/**BLE ELITE 好写纵向 宽度**/
#define VALUE_ELITE_XY_WIDTH  14300.0f
/**BLE ELITE 好写纵向 高度**/
#define VALUE_ELITE_XY_HEIGHT  7950.0f

/**BLE ELITE PLUS J0 纵向 宽度**/
#define VALUE_ELITE_PLUS_J0_WIDTH  14435.0f
/**BLE ELITE PLUS J0 纵向 高度**/
#define VALUE_ELITE_PLUS_J0_HEIGHT  8191.0f
//OTA状态
typedef enum {
    OTA_ERROR,
    OTA_DATA,
    OTA_UPDATE,
    OTA_SUCCESS,
    OTA_RESET,
    
}OTAState;



//同步笔记状态
typedef enum {
    SYNC_ERROR,
    SYNC_NOTE,
    SYNC_NO_NOTE,
    SYNC_SUCCESS,
    SYNC_START,
    SYNC_STOP,
    SYNC_COMPLETE,
    
}SYNCState;

//设备类型
typedef enum {
    
    UnKnown = 0,
    
    RobotPen_P7  = 1,
    
    Elite ,
    
    Elite_Plus ,
    
    RobotPen_P1 ,
    
    Elite_Plus_New ,
    
    Elite_XY = 7,
    
    Elite_Plus_J0,
    
} DeviceType;

//电量状态
typedef enum {
    /**电量低**/
    LOW = 0,
    /**良好**/
    GOOD
    
} BatteryState;

//连接状态
typedef enum {
    /**没有找到设备**/
    NOTHING,
    /**正在连接**/
    CONNECTING,
    /**连接成功**/
    CONNECTED,
    /**连接错误**/
    CONNECT_FAIL,
    /**正在断开**/
    DISCONNECTING,
    /**已断开**/
    DISCONNECTED,
    /**开始发现服务**/
    SERVICES_START,
    /**服务准备完成**/
    SERVICES_READY,
    /**发现服务失败**/
    SERVICES_FAIL,
    /**笔准备完成**/
    PEN_READY,
    /**笔初始化完成**/
    PEN_INIT_COMPLETE,
    /**设备信息获取**/
    DEVICE_INFO_END,
    /**设备可以更新**/
    DEVICE_UPDATE
    
}DeviceState;


typedef enum{
    DeviceEvent_CLick = 0,
    DeviceEvent_Double_CLick ,
    DeviceEvent_Front ,
    DeviceEvent_Next ,
    DeviceEvent_NewPage
}DeviceEventType;

















