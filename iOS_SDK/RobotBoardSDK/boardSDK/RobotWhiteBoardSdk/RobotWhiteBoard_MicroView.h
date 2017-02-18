//
//  RobotWhiteBoard_MicroView.h
//  EverWrite
//
//  Created by chong gao on 2016/11/28.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import "RobotWhiteBoardView.h"

@interface RobotWhiteBoard_MicroView : RobotWhiteBoardView


- (BOOL)GetIsVoiceEnable;

- (void)StartVideoRecord;// 开始录制
- (void)EndVideoRecord; // 结束录制
- (void)PauseVideoRecord; // 暂停录制
- (void)ContinueVideoRecord;// 继续录制 

- (void)SendStateInfo:(NSDictionary *)StateInfo;



@end
