//
//  RobotNote.h
//  PPWrite
//
//  Created by chong gao on 2017/1/4.
//  Copyright © 2017年 Robot. All rights reserved.
//
/**
*
*  ━━━━━━━━━━━━━━━━━━神兽出没━━━━━━━━━━━━━━━━━━
*
*          ┏━┓        ┏━┓
*         ┏┛━┻━━━━━━━━┛━┻━┓
*         ┃               ┃
*         ┃               ┃
*         ┃       ━       ┃
*         ┃   ┳┛     ┗┳   ┃
*         ┃               ┃
*         ┃       ┻       ┃
*         ┃               ┃
*         ┗━━━━┳━━━━━━━━━━┛
*              ┃　　　 ┃　　　　Code is far away from bug with the animal protecting
*              ┃　　　 ┃ + 　　　　神兽保佑,永无bug
*              ┃　　　 ┃
*              ┃　　　 ┃　　+
*              ┃　 　　┗━━━━━━┓ + +
*              ┃ 　　　　　　　  ┣┓
*              ┃ 　　　　　　　  ┏┛
*              ┗━┓ ┓━┳━┓━┓━┓━━┛ + + + +
*                ┃━┫━┫ ┃━┫━┫
*                ┗━┻━┛ ┗━┻━┛+ + + +
*
*  ━━━━━━━━━━━━━━━━━━感觉萌萌哒━━━━━━━━━━━━━━━━━━
*/

#import <Foundation/Foundation.h>

@interface RobotNote : NSObject

@property (nonatomic, assign) int NoteID;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) int DeviceType;
@property (nonatomic, copy) NSString *Title;
@property (nonatomic, assign) long UserID;
@property (nonatomic, assign) NSTimeInterval CreateTime;
@property (nonatomic, assign) NSTimeInterval UpdateTime;
@property (nonatomic, assign) int  IsHorizontal;


@end
