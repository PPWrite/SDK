//
//  Trails.h
//  SqlManager
//
//  Created by chong gao on 2016/12/1.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Trails : NSObject

@property (nonatomic, assign) int TrailID;//索引id
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) long UserID;
@property (nonatomic, assign) long long StartTime;
@property (nonatomic, assign) long long EndTime;
@property (nonatomic, strong) NSArray *Data;
@property (nonatomic, assign) long Color;
@property (nonatomic, assign) int Type;
@property (nonatomic, assign) int Page;
@property (nonatomic, copy) NSString *Ext;//注解

@end
