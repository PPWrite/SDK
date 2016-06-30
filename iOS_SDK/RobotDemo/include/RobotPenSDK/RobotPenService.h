//
//  SmartPenService.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/16.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "ScanDeviceDelegate.h"
#import "ConnectStateDelegate.h"
#import "PointChangeDelegate.h"
#import "DeviceObject.h"

@interface RobotPenService : NSObject<CBCentralManagerDelegate,CBPeripheralDelegate>{
    NSMutableData *_lastData;
    CBPeripheral *curCBPeripheral;              //当前连接的设备
    CBCentralManager *bluetoothManager;         //蓝牙管理模块
    NSMutableDictionary *foundPeripherals;      //发现的外围设备
    NSMutableDictionary *characteristicDict;
    
    BOOL isBluetoothReady;      //蓝牙是否准备完成
    BOOL isScanning;            //是否正在扫描
    BOOL isConnected;           //是否已连接成功
    float perX,perY;            //上一个点
}
@property (assign) id scanDeviceDelegate;
@property (assign) id connectStateDelegate;
@property (assign) id pointChangeDelegate;
@property (nonatomic,strong) NSMutableData *lastData;
@property (nonatomic,strong) NSMutableDictionary *foundPeripherals;//发现的外围设备
@property (nonatomic,strong) NSMutableDictionary *characteristicDict;
@property (nonatomic,retain) DeviceObject *currConnectDevice;

+(id)sharePenService;

+(NSString *)test;
/**
 扫描设备
 **/
-(void)scanDevice:(id<ScanDeviceDelegate>)delegate;

/**
 停止扫描
 **/
-(void)stopScanDevice;

/**
 连连接蓝牙设备
 **/
-(void)connectDevice:(DeviceObject *)device delegate:(id<ConnectStateDelegate>)delegate;

/**
 断开当前连接设备
 **/
-(void)disconnectDevice;

/**
 获取当前连接的设备
 **/
-(DeviceObject *)getCurrDevice;
@end
