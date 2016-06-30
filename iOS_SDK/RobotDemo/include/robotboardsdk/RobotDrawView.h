//
//  drawView.h
//  RobotPen
//
//  Created by 高宠 on 16/3/18.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "pageModel.h"

/**
 *  获取视频录制时间的协议
 */
@protocol VideoTimeDelegate <NSObject>
/**
 *  实时获取录制视频的协议方法
 *
 *  @param time 时间/s
 */
- (void)time:(NSInteger) time;

@end


@interface RobotDrawView : UIView

/**
 *  线宽
 */
@property (nonatomic, assign)CGFloat lineWidth;
/**
 *  线条颜色
 */
@property (nonatomic, strong)UIColor *lineColor;
/**
 *  画板背景图
 */
@property (nonatomic, strong)UIImage *backgroudBoardImage;
/**
 *  画板背景色
 */
@property (nonatomic, strong)UIColor *backgroundBoardColor;

/**
 *  是否使用橡皮擦
 */
@property (nonatomic, assign)BOOL eraserisEnabled;
/**
 *  背景图能否移动
 */
@property (nonatomic, assign) BOOL backgroundIsCanMove;
/**
 *  页码
 */
@property (nonatomic, strong) pageModel *page;
/**
 *  页码label
 */
@property (nonatomic, strong) UILabel *pageLabel;
/**
 *  是否在录制视频
 */
@property (nonatomic, assign) BOOL isRecording;
/**
 *  录制视频是否被暂停
 */
@property (nonatomic, assign) BOOL isPause;

/**
 *  获取录制时间的Delegate
 */
@property (nonatomic, weak) id<VideoTimeDelegate> timeDelegate;

/**
 *  清屏
 */
- (void)ClearBoard;
/**
 *  后撤一步
 */
- (void)retreatAStep;

/**
 *  miaohui笔迹
 */
- (void)drawChirographyWithPoint:(CGPoint)penPoint isRoute:(BOOL)isRoute;
/**
 *  根据urls数组设置图片
 *
 *  @param urls 图片地址
 */
- (void)setBackgroudImageWithUrl:(NSString *)path;
/**
 *  根据path设置图片
 *
 *  @param path 图片路径
 */
- (void)setBackgroudBoardImageWithPath:(NSString *)path;
/**
 *  是否竖屏
 *
 *  @param mark
 */
- (void)IsVer:(BOOL)mark;
/**
 *  准到当前页
 */
- (void)turnToCurrentPage;
/**
 *  删除页
 *
 *  @param 页码
 */
- (void)deleteWithCurrentPagewith:(NSInteger)page;
/**
 *  删除笔记
 */
- (void)deleteAll;
/**
 *  记录页码
 */
- (void)recodePage;
/**
 *  删除画布图片
 */
- (void)deleteImg;
/**
 *  删除当前页码
 */
- (void)deletepage;
/**
 *  跳转到第一页
 */
- (void)turnToFirst;
/**
 *  跳转到最后一页
 */
- (void)turnToLast;
/**
 *  上一页
 */
- (void)turnToPrevious;
/**
 *  下一页
 */
- (void)turnToNext;
/**
 *  开始录制
 */
- (void)startRecord;
/**
 *  暂停录制
 */
- (void)pauseRecord;
/**
 *  取消暂停
 */
- (void)beginRecord;
/**
 *  停止录制
 */
- (void)stopRecord;
/**
 *  初始化页码
 */
- (void)initializePage;

@end
