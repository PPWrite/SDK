//
//  PhotoImgView.h
//  WhiteBoardViewDemo
//
//  Created by JMS on 2016/11/19.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PhotoImage.h"

@interface PhotoImgView : UIImageView


@property (nonatomic, assign) int enbleEdit;
@property (nonatomic, strong)PhotoImage *photo;

- (void)setEditMode:(BOOL)enbleEdit;

@end
