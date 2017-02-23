//
//  AppDelegate.h
//  RobotBoardSDKDemo
//
//  Created by JMS on 2017/2/21.
//  Copyright © 2017年 JMS. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (readonly, strong) NSPersistentContainer *persistentContainer;

- (void)saveContext;


@end

