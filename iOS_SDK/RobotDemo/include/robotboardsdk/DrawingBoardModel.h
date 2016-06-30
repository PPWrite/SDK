//
//  DrawingBoardModel.h
//  RobotPen
//
//  Created by 高宠 on 16/3/16.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface DrawingBoardModel : NSObject

/**
 *  初始化方法
 *
 */
+ (id)viewModelWithColor:(UIColor *)color Path:(UIBezierPath *)path Width:(CGFloat)width;
/**
 *  颜色
 */
@property (strong, nonatomic) UIColor *color;
/**
 *  轨迹
 */
@property (strong, nonatomic) UIBezierPath *path;
/**
 *  线宽
 */
@property (assign, nonatomic) CGFloat width;

@end
