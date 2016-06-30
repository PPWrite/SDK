//
//  NoteModel.h
//  FMDBTool
//
//  Created by 高宠 on 16/5/5.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface NoteModel : NSObject

@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, assign) NSInteger NetID;
@property (nonatomic, assign) NSInteger IsPublic;
@property (nonatomic, assign) NSInteger Width;
@property (nonatomic, assign) NSInteger Height;
@property (nonatomic, assign) NSString *Title;
@property (nonatomic, assign) NSInteger UserID;
@property (nonatomic, strong) NSDate *CreateTime;
@property (nonatomic, strong) NSDate *UpdateTime;


@end
