//
//  RobotTrails.h
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

@interface RobotTrails : NSObject

@property (nonatomic, assign)int TrailID;
@property (nonatomic, copy)NSString *EXT;//图片模型key 和 翻页|
@property (nonatomic, copy)NSString *Block;
@property (nonatomic, assign)long Color;
@property (nonatomic, assign)long UserID;
@property (nonatomic, assign)long ST;
@property (nonatomic, assign)long ET;
@property (nonatomic, strong)NSArray *Data;
@property (nonatomic, assign) int Type;


- (NSData*)getJSONOptions:(NSJSONWritingOptions)options error:(NSError**)error;

@end
