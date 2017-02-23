//
//  WhiteBoardLiveViewController.m
//  RobotBoardSDKDemo
//
//  Created by JMS on 2017/2/22.
//  Copyright © 2017年 JMS. All rights reserved.
//

#import "WhiteBoardLiveViewController.h"

@interface WhiteBoardLiveViewController ()
@property (weak, nonatomic) IBOutlet UIButton *BackButton;
@end

@implementation WhiteBoardLiveViewController



-(void)BackButtonPressed:(UIButton *)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    [_BackButton addTarget:self action:@selector(BackButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    // Do any additional setup after loading the view from its nib.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
