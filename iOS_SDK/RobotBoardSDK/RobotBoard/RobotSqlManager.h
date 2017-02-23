//
//  RobotSqlManager.h
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

//#ifdef DEBUG
//
//#define RobotLog(fmt, ...) NSLog((@"Robot Log :   %s [Line %d] " fmt), __PRETTY_FUNCTION__, __LINE__, ##__VA_ARGS__);
//
//#else
//
//#define RobotLog(...)
//
//#endif


#import <Foundation/Foundation.h>

@class Video;
@class RobotNote;
@class RobotTrails;
@class RobotTrailBlock;


@interface RobotSqlManager : NSObject


//检查数据库
+ (void)checkRobotSqlManager;
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

// 获取视频总个数
+ (long)GetVideoTotalNum;

//删除视频

+ (void)DeleteVideoWithNameKey:(NSString *)NameKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//根据页码删除所有视频

+ (void)DeleteAllVideoWithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//获取视频路径
+ (NSString *)GetVideoPathWithNameKey:(NSString *)NameKey;

//创建一本笔记
+ (void)BulidNote:(RobotNote *)NoteObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//创建一本直播笔记
+ (void)BulidLiveNote:(RobotNote *)NoteObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//保存一本笔记
+ (void)SaveNote:(RobotNote *)NoteObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//更新笔记的UpdateTime
+ (void)UpdateNoteWithNoteKey:(NSString *)NoteKey BlockKey:(NSString *)BlockKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//获取笔记列表
+ (void)GetAllNoteListWithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//获取笔记Info
+ (void)GetPageInfosWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//获取目标页的blockKey
//自己主动翻页 （没有则新建）
+ (NSString *)getNextWithNoteKey:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;
+ (NSString *)getFrontWithNoteKey:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;
//插页
+ (NSString *)GetNewNextWithNoteKey:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;

//查找是否存在
+ (NSString *)GetFront:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;
+ (NSString *)getNext:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;

//修改指向
+ (void)SetNextBlockKey:(NSString *)nextBlockKey BlockKey:(NSString *)blockKey :(NSString *)Notekey;
+ (void)SetBlockKey:(NSString *)blockKey NextBlockKey:(NSString *)nextBlockKey :(NSString *)Notekey;
+ (BOOL)IsExist:(NSString *)NoteKey BlockKey:(NSString *)ActivityBlockKey;

//检查笔记是否存在
+ (BOOL)checkNoteWithNoteKey:(NSString *)NoteKey;
//获取笔记blocks
+ (void)GetAllBlockWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//创建块
+ (void)BulidBlockWithBlockKey:(NSString *)blockKey NextBlockKey:(NSString *)NextBlockKey NoteKey:(NSString *)NoteKey;

//保存轨迹数据
+ (void)SaveTrails:(RobotTrails *)TrailsObj Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//还原笔记数据
+ (void)GetNoteTrailsWithBlockKey:(NSString *)ActivityBlockKey SuccessTrails:(void (^)(id responseObject))SuccessTrails SuccessImages:(void (^)(id responseObject))SuccessImages Failure:(void (^)(NSError *error))Failure;

//删除图片
+ (void)DelNoteImgsWithBlockKey:(NSString *)ActivityBlockKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//删除笔记
+ (void)DelNoteTrailsWithBlockKey:(NSString *)ActivityBlockKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;


//清屏
+ (void)DelNoteWithBlockKey:(NSString *)ActivityBlockKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;


//删除当前页
+ (void)DeleteBlockKey:(NSString *)ActivityBlockKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//修改临时笔记的设备类型
+ (void)ChangeTempNoteDeviceType:(int)DeviceType;
+ (void)ChangeTempNoteIsHorizontal:(BOOL)mark;

//保存临时笔记
+ (void)SaveTempNote:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//笔记修改名字
+ (void)ChangeNameWithNoteKey:(NSString *)NoteKey TagertTitle:(NSString *)TagertTitle Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;

//删除笔记
+ (void)DeleteNoteWithNoteKey:(NSString *)NoteKey Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;
//根据页码删除所有笔记

+ (void)DeleteNoteWithPage:(int)Page Success:(void (^)(id responseObject))Success Failure:(void (^)(NSError *error))Failure;



+ (NSTimeInterval)GetTimeInterval;
+ (NSString *)GetNewKey;




@end
