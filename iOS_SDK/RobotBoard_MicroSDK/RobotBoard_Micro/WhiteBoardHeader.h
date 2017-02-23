//
//  WhiteBoardHeader.h
//  WhiteBoardViewDemo
//
//  Created by JMS on 2016/11/19.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//


#ifdef DEBUG

#define RobotLog(fmt, ...) NSLog((@"RobotWB Log :   %s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);

#else

#define RobotLog(...)

#endif

#define ThemeColor [UIColor colorWithRed:54/255.0 green:189/255.0 blue:164/255.0 alpha:1] //图片操作边框颜色
//Scale状态
typedef enum {
    CENTER,
    FIT,
}ScaleType;
typedef enum{
    Gesture ,
    PenDraw
} DrawType;
