//
//  NoteModel.h
//  FMDBTool
//
//  Created by 高宠 on 16/5/5.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NoteModel : NSObject


@property (nonatomic, assign) int NoteID;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) int NetID;
@property (nonatomic, assign) int DeviceType;
@property (nonatomic, copy) NSString *Title;
@property (nonatomic, assign) int UserID;
@property (nonatomic, strong) NSDate *CreateTime;
@property (nonatomic, strong) NSDate *UpdateTime;




@end
