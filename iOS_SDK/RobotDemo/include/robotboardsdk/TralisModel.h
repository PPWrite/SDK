//
//  TralisModel.h
//  FMDBTool
//
//  Created by 高宠 on 16/5/5.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TralisModel : NSObject
@property (nonatomic, assign) NSInteger TrailID;
@property (nonatomic, assign) NSInteger NoteID;
@property (nonatomic, assign) NSInteger UserID;
@property (nonatomic, strong) NSDate *StartTime;
@property (nonatomic, strong) NSDate *EndTime;
@property (nonatomic, strong) NSArray *DataArray;
@property (nonatomic, assign) NSInteger Color;
@property (nonatomic, assign) NSInteger Type;
@property (nonatomic, assign) NSInteger Page;
@end
