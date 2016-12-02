//
//  TralisModel.h
//  FMDBTool
//
//  Created by 高宠 on 16/5/5.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>





@interface TralisModel : NSObject
@property (nonatomic, assign) int TrailID;//索引id
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) long UserID;
@property (nonatomic, strong) NSDate *StartTime;
@property (nonatomic, strong) NSDate *EndTime;
@property (nonatomic, strong) NSArray *Data;
@property (nonatomic, assign) int Color;
@property (nonatomic, assign) int Type;
@property (nonatomic, assign) int Page;
@property (nonatomic, copy) NSString *Ext;//注解













@end
