//
//  ScanDeviceDelegate.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/16.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DeviceObject.h"

@protocol ScanDeviceDelegate<NSObject>

@required

-(void)find:(DeviceObject *)deviceObject;

@end
