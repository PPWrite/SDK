//
//  PointChangeDelegate.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/22.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PointObject.h"

@protocol PointChangeDelegate<NSObject>
@required

-(void)change:(PointObject *)point;
@end
