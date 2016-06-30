//
//  BlePenUtil.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/9/8.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DeviceObject.h"

@interface BlePenUtil : NSObject{
    BOOL lastPointRoute;
}

//获取数码笔点对象
-(NSMutableArray *)getPointList:(DeviceObject *)device bleData:(NSData *)bleData;

@end
