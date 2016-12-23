//
//  SqlManager.h
//  SqlManager
//
//  Created by chong gao on 2016/12/1.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//
//笔迹消息类型
typedef enum {
    /**轨迹数据**/
    TRACK_DATA,
    /**图像数据**/
    IMG_DATA,
    /**翻页**/
    PAGE_TURN,
    /**背景**/
    BACKGROUND_IMG,
    /**清屏**/
    CLS = 10,
    /**清除图片**/
    ALL_IMG_DEL,
    /**清除图形**/
    SHAPE_DEL,
    /**清除笔迹**/
    TRACK_DEL,
    /**删除图片**/
    IMG_DEL = 21,
}TrailType;

#import <Foundation/Foundation.h>

@class Video;
@class Note;
@class Trails;

#ifdef DEBUG

#define RobotLog(fmt, ...) NSLog((@"RobotSql Log :   %s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);

#else

#define RobotLog(...)

#endif

@interface SqlManager : NSObject

//检查数据库
+ (void)checkSqlManager;
//保存截图信息
+ (void)SaveImageKeySuccess:(void (^)(id responseObject))Success
                        Failure:(void (^)(NSError *error))Failure;
//获取截图
+ (void)GetImagesArrayWithPage:(int)Page Success:(void (^)(id responseObject))Success
                           Failure:(void (^)(NSError *error))Failure;
//获取截图图片路径
+ (NSString *)GetImagePathWithKey:(NSString *)Key;

//删除图片
+ (void)DeleteImageWithKeyArray:(NSArray *)KeyArray Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;


//创建Video

+ (void)BulidVideoWithNameKey:(NSString *)NameKey Success:(void (^)(id responseObject))Success
                      Failure:(void (^)(NSError *error))Failure;
//完善Video信息

+ (void)PerfectVideoInfosWithVideo:(Video *)Video Success:(void (^)(id responseObject))Success
                           Failure:(void (^)(NSError *error))Failure;
//改名

+ (void)ChangeVideoWithNameKey:(NSString *)NameKey Alias:(NSString *)Alias Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//设置是否发布
+ (void)SetOnLineWithNameKey:(NSString *)NameKey IsOnLine:(int)IsOnLine Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//保存Md5信息
+ (void)SaveMd5StrWithNameKey:(NSString *)NameKey Md5Str:(NSString *)Md5Str Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//根据Md5获取视频信息
+ (void)GetNoteInfoWithMd5Str:(NSString *)Md5Str Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//根据Md5获取视频路径
+ (void)GetVideoPathWithMd5Str:(NSString *)Md5Str  Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//获取视频信息 - NameKey
+ (void)GetVideoInfoWithNameKey:(NSString *)NameKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

// 获取视频列表 page >= 0
+ (void)GetVideoListWithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//删除视频

+ (void)DeleteVideoWithNameKey:(NSString *)NameKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//获取视频路径
+ (NSString *)GetVideoPathWithNameKey:(NSString *)NameKey;


//创建一本笔记
+ (void)BulidNote:(Note *)NoteObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//更新笔记的保存时间
+ (void)UpdateNoteWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//获取笔记信息
+ (void)GetAllNoteListWithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//保存轨迹数据
+ (void)SaveTrails:(Trails *)TrailsObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//获取笔记页数
+ (void)GetPageInfosWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//删除当前页
+ (void)DeleteCurrentPage:(int)Page NoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//修改临时笔记的设备类型
+ (void)ChangeTempNoteDeviceType:(int)DeviceType;
+ (void)ChangeTempNoteIsHorizontal:(BOOL)mark;
//保存临时笔记
+ (void)SaveTempNote:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//笔记修改名字
+ (void)ChangeNameWithNoteKey:(NSString *)NoteKey TagertTitle:(NSString *)TagertTitle Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//删除笔记
+ (void)DeleteNoteWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//还原笔记数据
+ (void)GetNoteTrailsWithNoteKey:(NSString *)NoteKey Page:(int)Page SuccessTrails:(void (^)(id responseObject))SuccessTrails SuccessImages:(void (^)(id responseObject))SuccessImages Failure:(void (^)(NSError *error))Failure;


//获取所有轨迹
+ (void)GetAllTrailsWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//获取某一页的所有轨迹
+ (void)GetAllTrailsWithNoteKey:(NSString *)NoteKey WithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//新建下一页
+ (void)NextNewPage:(int)OldPage WithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//向上合并
+ (void)MergeTrailsWith:(int)Page WithEndTime:(long long)endTime WithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//向下分离
+ (void)SeparateTrailsWith:(int)Page WithEndTime:(long long)endTime WithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;




+ (NSTimeInterval)GetTimeInterval;

@end
