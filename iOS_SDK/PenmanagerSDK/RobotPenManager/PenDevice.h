//
//  PenDevice.h
//  RobotPenManager
//
//  Created by chong gao on 2016/11/15.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PenHeader.h"
#import <CoreBluetooth/CoreBluetooth.h>

@interface PenDevice : NSObject
@property (nonatomic, assign) DeviceType deviceType;//设备类型

@property (retain, nonatomic) CBPeripheral *peripheral;

@property (copy, nonatomic) NSString *uuID;

@property ( nonatomic) int Mac;

@property (nonatomic, copy) NSString *HWStr;

@property (nonatomic, copy) NSString *SWStr;

- (NSString *)getName;
@end
