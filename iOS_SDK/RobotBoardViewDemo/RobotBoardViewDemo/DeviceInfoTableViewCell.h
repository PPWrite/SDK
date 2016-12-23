//
//  DeviceInfoTableViewCell.h
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/14.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol DeviceInfoTableViewCellDelegate <NSObject>

-(void)DeviceInfoButtonPressed:(UIButton *)sender;

@end

@interface DeviceInfoTableViewCell : UITableViewCell
@property(nonatomic,assign) id <DeviceInfoTableViewCellDelegate>delegate;
@property(nonatomic,strong) UILabel *deviceTitleLabel;
@property(nonatomic,strong) UILabel *deviceContentLabel;
@property(nonatomic ,strong) UIButton *deviceButton;

@end
