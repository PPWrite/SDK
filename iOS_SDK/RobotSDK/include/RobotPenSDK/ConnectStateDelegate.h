//
//  ConnectStateDelegate.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/16.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Enums.h"
#import "DeviceObject.h"

@protocol ConnectStateDelegate<NSObject>

@required

-(void)stateChange:(ConnectState)state;

@end