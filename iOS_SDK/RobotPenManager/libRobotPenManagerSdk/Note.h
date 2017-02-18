//
//  Note.h
//  SqlManager
//
//  Created by chong gao on 2016/12/1.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Note : NSObject

@property (nonatomic, assign) int NoteID;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) long NetID;
@property (nonatomic, assign) int DeviceType;
@property (nonatomic, copy) NSString *Title;
@property (nonatomic, assign) long UserID;
@property (nonatomic, assign) long CreateTime;
@property (nonatomic, assign) long UpdateTime;
@property (nonatomic, assign) int  isHorizontal;

@end
