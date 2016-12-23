//
//  ViewController.m
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/14.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "ViewController.h"
#import "SingleCanvasViewController.h"
#import "SetDemoViewController.h"
#import "ManyCanvasViewController.h"
#import "ManyCanvasFunctionViewController.h"
#import "RobotWhiteBoardSdk.h"


@interface ViewController ()

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
}
- (IBAction)searchDevice:(id)sender {
    SetDemoViewController *vc = [[SetDemoViewController alloc]init];
    [self presentViewController:vc animated:YES completion:nil];
    
}
- (IBAction)singleButtonPressed:(id)sender {
    SingleCanvasViewController *vc = [[SingleCanvasViewController alloc]init];
    [self presentViewController:vc animated:YES completion:nil];
}

- (IBAction)manyButtonPressed:(id)sender {
    ManyCanvasViewController *vc = [[ManyCanvasViewController alloc]init];
    [self presentViewController:vc animated:YES completion:nil];
}
- (IBAction)function:(id)sender {
     ManyCanvasFunctionViewController *vc = [[ManyCanvasFunctionViewController alloc]init];
    [self presentViewController:vc animated:YES completion:nil];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


@end
