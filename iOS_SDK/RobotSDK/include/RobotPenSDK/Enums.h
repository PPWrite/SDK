//
//  Enums.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/16.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//


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
    PEN_INIT_COMPLETE
}ConnectState;

//设备版本
typedef NS_ENUM(NSInteger,DeviceVersion) {
    XN680 = 1,
    XN680T = 2,
};

//电量状态
typedef NS_ENUM(NSInteger,BatteryState) {
    /**没有状态**/
    NOT = 0,
    /**电量低**/
    LOW = 1,
    /**良好**/
    GOOD = 2
};

typedef NS_ENUM(NSInteger, SceneType) {
    A4 = 1,
    A5 = 2,
    A4_horizontal = 3,
    A5_horizontal = 4,
    SIZE_10 = 5
};