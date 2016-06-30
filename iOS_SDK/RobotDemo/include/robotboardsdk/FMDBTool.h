//
//  FMDBTool.h
//  FMDBTool
//
//  Created by 高宠 on 16/5/5.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FMDB.h"
@class NoteModel;
@class TralisModel;
@interface FMDBTool : NSObject
+ (void)openFMDB;
+ (BOOL)insertNoteModel:(NoteModel *)model;
+ (void)insertTrailModel:(TralisModel *)model;
+ (void)insertTmpTrailModel:(TralisModel *)model;
+ (NSDictionary *)queryDataTmp:(NSInteger)page;
+ (void)DeleteDataTmp:(NSInteger)page;
+ (void)cleanDataTemp:(NSInteger)page;
+ (void)DeleteDataTmpAll;
@end
