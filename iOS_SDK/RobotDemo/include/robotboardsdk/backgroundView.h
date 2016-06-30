//
//  backgroundView.h
//  RobotPen
//
//  Created by 高宠 on 16/3/18.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <UIKit/UIKit.h>

/**
 *  背景移动协议
 */
@protocol imageMovingDelegate <NSObject>
@optional
- (void)imageMoveWithPan:(UIPanGestureRecognizer *)rec;
- (void)imageMoveWithPinch:(UIPinchGestureRecognizer *)rec;
@end

@interface backgroundView : UIView <imageMovingDelegate>

@property (nonatomic, strong) NSString *imageName;
@property (nonatomic, strong) UIImageView *backgroungImageView;
- (void)saveImage:(UIImage *)image;
- (void)saveImages:(NSArray *)urls;
- (void)saveDeleteImage;
//- (void)saveImageToNote;
- (void)reductionImage:(NSDictionary *)dict;
@end
