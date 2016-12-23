//
//  WhiteBoardHeader.h
//  WhiteBoardViewDemo
//
//  Created by chong gao on 2016/11/19.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//


#ifdef DEBUG

#define RobotLog(fmt, ...) NSLog((@"RobotWB Log :   %s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);

#else

#define RobotLog(...)

#endif

typedef enum{
    Gesture ,
    PenDraw
} DrawType;
//Scale状态
typedef enum {
    CENTER,
    FIT,
}ScaleType;

typedef enum{
    NOGes ,//笔模式，不能手写
    SnapSaveSucc,
    SnapSaveFaile,
    ReduceSuccessNote,
    ReduceSuccessImage,
    ReduceError,
    
    InsertError, //插入失败
    
    VideoSucc,
    VideoError
  
} WBState;
