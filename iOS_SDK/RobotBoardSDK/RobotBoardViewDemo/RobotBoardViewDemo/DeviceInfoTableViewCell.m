//
//  DeviceInfoTableViewCell.m
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/14.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "DeviceInfoTableViewCell.h"


#define NameColor [UIColor colorWithRed:113.0/255.0f green:113.0/255.0f blue:113.0/255.0f alpha:1.00]//浅灰色的颜色
#define GrayLineColor [UIColor colorWithRed:240.0/255.0 green:240.0/255.0 blue:240.0/255.0 alpha:1.00]

#define TitleNormalColor [UIColor colorWithRed:33/255.0 green:33/255.0 blue:33/255.0 alpha:1]
@implementation DeviceInfoTableViewCell

-(id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        [self setContentView];
    }
    return self;
}

-(void)setContentView{
    self.deviceTitleLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 0, self.frame.size.width, self.frame.size.height)];
    self.deviceTitleLabel.textColor = TitleNormalColor;
    [self.deviceTitleLabel setFont:[UIFont systemFontOfSize:16]];
    
    [self addSubview:self.deviceTitleLabel];
    
    self.deviceContentLabel = [[UILabel alloc] initWithFrame:CGRectMake(self.deviceTitleLabel.frame.origin.x, self.deviceTitleLabel.frame.size.height,  self.deviceTitleLabel.frame.size.width,self.deviceTitleLabel.frame.size.height) ];
    self.deviceContentLabel.textColor = NameColor;
    [ self.deviceContentLabel setFont:[UIFont systemFontOfSize:12]];
    [self addSubview: self.deviceContentLabel];
    
    
    
    self.deviceButton = [UIButton buttonWithType:UIButtonTypeSystem];
    self.deviceButton.frame = CGRectMake(self.frame.size.width - 100,80/2-30/2, 80, 30);
    [self.deviceButton setBackgroundColor:[UIColor whiteColor]];
    [self.deviceButton setTitleColor:[UIColor redColor] forState:UIControlStateNormal];
    [self.deviceButton.titleLabel setFont:[UIFont systemFontOfSize:13]];
    self.deviceButton.layer.cornerRadius = 5;
    self.deviceButton.layer.borderWidth = 1;
    self.deviceButton.hidden  = YES;
    self.deviceButton.layer.borderColor = [GrayLineColor CGColor];
    [self.deviceButton addTarget:self action:@selector(deviceButtonPressed) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:self.deviceButton];
}

-(void)deviceButtonPressed{

    if (self.delegate && [self.delegate respondsToSelector:@selector(DeviceInfoButtonPressed:)]) {
        [self.delegate DeviceInfoButtonPressed:_deviceButton];
    }

}

@end
