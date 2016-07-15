//
//  drawView.h
//  RobotPen
//
//  Created by 高宠 on 16/3/18.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <UIKit/UIKit.h>


#import "pageModel.h"


@protocol VideoTimeDelegate <NSObject>

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


@property (nonatomic, strong) pageModel *page;
@property (nonatomic, strong) UILabel *pageLabel;

@property (nonatomic, assign) BOOL isRecording;
@property (nonatomic, assign) BOOL isPause;

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
 *  笔迹
 *
 *  @param penPoint <#penPoint description#>
 *  @param isRoute  <#isRoute description#>
 */

- (void)drawChirographyWithPoint:(CGPoint)penPoint isRoute:(BOOL)isRoute Pressure:(float)pressure;
/**
 *  <#Description#>
 *
 *  @param urls <#urls description#>
 */
- (void)setBackgroudImageWithUrl:(NSArray *)urls;

- (void)setBackgroudBoardImageWithPath:(NSString *)path;

- (void)IsVer:(BOOL)mark;

- (void)turnToCurrentPage;
- (void)deleteWithCurrentPagewith:(NSInteger)page;
- (void)deleteAll;
- (void)recodePage;
- (void)deleteImg;
- (void)deletepage;
- (void)turnToFirst;
- (void)turnToLast;
- (void)turnToPrevious;
- (void)turnToNext;
- (void)startRecord;
- (void)pauseRecord;
- (void)beginRecord;
- (void)stopRecord;
- (void)initializePage;
@end
