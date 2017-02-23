//
//  ViewController.m
//  RobotBoardSDKDemo
//
//  Created by JMS on 2017/2/21.
//  Copyright © 2017年 JMS. All rights reserved.
//

#import "ViewController.h"
#import "SearchDeviceViewController.h"
#import "WhiteBoardViewController.h"
#import "WhiteBoardMicroViewController.h"
#import "WhiteBoardLiveViewController.h"
@interface ViewController ()

@end

@implementation ViewController


-(IBAction)searchDevice:(id)sender
{
    SearchDeviceViewController *search = [[SearchDeviceViewController alloc] init];
    [self presentViewController:search animated:YES completion:nil];
}
-(IBAction)WhiteBoard:(id)sender
{
    WhiteBoardViewController *WB = [[WhiteBoardViewController alloc] init];
    [self presentViewController:WB animated:YES completion:nil];
}
-(IBAction)WhiteBoardMicro:(id)sender
{
    WhiteBoardMicroViewController *WBMicro = [[WhiteBoardMicroViewController alloc] init];
    [self presentViewController:WBMicro animated:YES completion:nil];
}
-(IBAction)WhiteBoardLive:(id)sender
{
    WhiteBoardLiveViewController *WBLive = [[WhiteBoardLiveViewController alloc] init];
    [self presentViewController:WBLive animated:YES completion:nil];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


@end
