//
//  RobotPenManager.h
//  RobotPenManager
//
//  Created by chong gao on 2016/11/15.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PenHeader.h"
#import "RobotNote.h"
#import "RobotTrailBlock.h"
#import "RobotTrails.h"
#import <CoreBluetooth/CoreBluetooth.h>
#import "PenDevice.h"
#import "PenPoint.h"


@protocol RobotPenDelegate <NSObject>

@optional

/**
 获取设备状态
 
 @param State <#State description#>
 */
- (void)getDeviceState:(DeviceState)State;

/**
 获取点数据
 
 @param point <#point description#>
 */
- (void)getPointInfo:(PenPoint *)point;

/**
 获取设备信息
 
 @param infos <#infos description#>
 */
- (void)getDeviceVersion:(NSDictionary *)infos;


/**
 自动连接的中的设备
 
 @param device <#device description#>
 */
- (void)AutoConnectingDevice:(PenDevice *)device;


/**
 OTA升级state
 
 @param state <#state description#>
 */
- (void)OTAUpdateState:(OTAState)state;

- (void)OTAUpdateProgress:(float)progess;


/**
 SYNCNoteState
 
 @param state <#state description#>
 */
- (void)SyncState:(SYNCState)state;


/**
 获取笔记数据
 */
- (void)getSyncData:(RobotTrails *)trails;
- (void)getSyncNote:(RobotNote *)note;


/**
 获取笔记数量
 @param num <#num description#>
 */
- (void)getStorageNum:(int)num;



/**
 发现设备

 @param device <#device description#>
 */
- (void)getBufferDevice:(PenDevice *)device;



//设备点击事件
- (void)getDeviceEvent:(DeviceEventType)Type;



@end



@interface RobotPenManager : NSObject

/**
 单例初始化
 */
+ (RobotPenManager *)sharePenManager;



////获取当前链接的设备
- (PenDevice *)getConnectDevice;
//
//连接设备
- (void)connectDevice:(PenDevice *)penDevice :(id<RobotPenDelegate>)delegate;
- (void)connectDevice:(PenDevice *)penDevice ;
/**
 扫描设备
 @param delegate <#delegate description#>
 */
- (void)scanDevice:(id<RobotPenDelegate>)delegate;
- (void)scanDevice;

  
//断开设备
- (void)disconnectDevice;



//获取版本号
- (NSDictionary *)getDeviceVersion;


/**
 获取设备类型

 @return <#return value description#>
 */
- (DeviceType)getConnectDeviceType;


/**
 设置回调代练

 @param delegate <#delegate description#>
 */
- (void)setPenDelegate:(id<RobotPenDelegate>)delegate;




/**
 停止扫描
 **/
-(void)stopScanDevice;


//检查设备是否连接
- (void)checkDeviceConnect;

/**
 取消配对
 */
- (void)deleteConnect;



/**
 开始OTA升级
 
 @param delegate <#delegate description#>
 */
-(void)startOTA:(id<RobotPenDelegate>)delegate;

- (void)startOTA;



/**
 同步笔记 开始
 
 @param delegate <#delegate description#>
 */
- (void)startSyncNote:(id<RobotPenDelegate>)delegate;

- (void)startSyncNote;

/**
 同步笔记 停止
 */
-(void)stopSyncNote;


//发送页码信息
- (void)SendPage:(int)Current :(int)Totla;



@end
