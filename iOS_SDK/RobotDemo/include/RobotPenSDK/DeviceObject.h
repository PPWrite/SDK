//
//  DeviceObject.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/16.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreBluetooth/CoreBluetooth.h>
#import "Enums.h"

@interface DeviceObject : NSObject<NSCoding>
{
    NSString        *key;           //设备标识
    NSInteger       sceneWidth;
    NSInteger       sceneHeight;
    CBPeripheral    *peripheral;
}

@property (nonatomic,assign) SceneType sceneType;  //场景类型
@property (retain, nonatomic) CBPeripheral *peripheral;
@property (copy, nonatomic) NSString *uuID;
//获取设备名字
-(NSString *)getName;

//获取场景宽度
-(NSInteger)getSceneWidth;

//获取场景高度
-(NSInteger)getSceneHeight;

@end
