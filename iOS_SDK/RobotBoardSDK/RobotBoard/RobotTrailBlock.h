//
//  RobotTrailBlock.h
//  PPWrite
//
//  Created by JMS on 2017/1/4.
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

@interface RobotTrailBlock : NSObject

@property (nonatomic, assign) int BlockID;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, copy) NSString *BlockKey;
@property (nonatomic, copy) NSString *NextBlockKey;
@property (nonatomic, assign)NSTimeInterval CreateTime;
@property (nonatomic, assign)NSTimeInterval UpdateTime;

@end
