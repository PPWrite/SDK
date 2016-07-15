//
//  DrawingBoardView.h
//  RobotPen
//
//  Created by 高宠 on 16/3/16.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "backgroundView.h"



@interface DrawingBoardView : UIView



/**
 *  线宽
 */
@property (nonatomic ,assign)CGFloat lineWidth;

/**
 *  线条颜色
 */
@property (nonatomic, strong)UIColor *lineColor;


/**
 *  笔记数组
 */
@property (nonatomic ,strong) NSMutableArray *pathArray;
/**
 *  笔迹
 *  @param penPoint <#penPoint description#>
 *  @param isRoute  <#isRoute description#>
 */

@property (nonatomic, strong) UIImageView *pen;

@property (nonatomic, assign) BOOL isVer;

/**
 *  背景图能否移动
 */
@property (nonatomic, assign) BOOL backgroundIsCanMove;

@property (nonatomic, weak) id<imageMovingDelegate> backgroundImageMoveDelegate;

- (void)drawChirographyWithPoint:(CGPoint)penPoint isRoute:(BOOL)isRoute Pressure:(float)pressure;

- (void)reductionNote:(NSArray *)array;

- (void)clearBoard;


@end
