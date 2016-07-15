//
//  PointObject.h
//  SmartPenCore
//
//  Created by Xiaoz on 15/7/22.
//  Copyright (c) 2015年 Xiaoz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Enums.h"

@interface PointObject : NSObject

@property (nonatomic,assign) short originalX;
@property (nonatomic,assign) short originalY;
@property (nonatomic,assign) float Pressure;
@property (nonatomic,assign) short width;
@property (nonatomic,assign) short height;
@property (nonatomic,assign) BOOL isRoute;          //是否是笔迹
@property (nonatomic,assign) BOOL isSw1;            //是否按键1被按下
@property (nonatomic,assign) BOOL isMove;           //是否正在移动
@property (nonatomic,assign) BatteryState battery;  //电量信息
@property (nonatomic,assign) SceneType sceneType;  //场景类型

-(NSString *)toString;

/**
 获取场景x值
 **/
-(short)getSceneX;
/**
 获取showWidth等比缩放后的x值
 **/
-(float)getSceneX:(int)showWidth;

/**
 获取场景y值
 **/
-(short)getSceneY;
/**
 获取showHeight等比缩放后的y值
 **/
-(float)getSceneY:(int)showHeight;

@end
