//
//  ViewController.m
//  RobotPenManagerDemo
//
//  Created by 高宠 on 2016/11/16.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "ViewController.h"
#import "RobotPenManager.h"
#import "PenDevice.h"
#import "PenPoint.h"

#define SCREEN_WIDTH self.view.bounds.size.width
#define SCREEN_HEIGHT self.view.bounds.size.height
@interface ViewController ()<UITableViewDelegate,UITableViewDataSource,RobotPenDelegate>
{
    UIButton *_blueToothButton;
    BOOL isConnect;
}
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UILabel *xValue;
@property (weak, nonatomic) IBOutlet UILabel *yValue;
@property (weak, nonatomic) IBOutlet UILabel *pressureLabel;
@property (weak, nonatomic) IBOutlet UILabel *deviceUUID;
@property (weak, nonatomic) IBOutlet UILabel *deviceName;
@property (weak, nonatomic) IBOutlet UILabel *electricityLabel;
@property (weak, nonatomic) IBOutlet UILabel *routeLabel;
@property(nonatomic,strong)PenDevice *device;
@property(nonatomic,strong)NSMutableArray *deviceArray;

@end

@implementation ViewController

-(NSMutableArray *)deviceArray{
    if (!_deviceArray) {
        _deviceArray = [NSMutableArray array];
    }
    return _deviceArray;
}
- (void)viewDidLoad {
    [super viewDidLoad];
    isConnect = NO;
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    
    _blueToothButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [_blueToothButton setFrame:CGRectMake(SCREEN_WIDTH/2-40, 35, 80, 40)];
    [_blueToothButton setBackgroundColor:[UIColor blueColor]];
    [_blueToothButton setTitle:@"查找设备" forState:UIControlStateNormal];
    [_blueToothButton setTitle:@"断开连接" forState:UIControlStateSelected];
    [_blueToothButton addTarget:self action:@selector(blueToothButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:_blueToothButton];
    

    
    [[RobotPenManager sharePenManager] setPenDelegate:self];
    
    // Do any additional setup after loading the view, typically from a nib.
}


-(void)blueToothButtonPressed:(UIButton *)sender{
    NSLog(@"%s",__func__);
    if (isConnect == NO) {
        sender.selected = YES;
//        [[RobotPenManager sharePenManager] scanDevice];
         [self.deviceArray removeAllObjects];
        [[RobotPenManager sharePenManager] scanDevice:self];
        
    } else{
        sender.selected = NO;
        [self.deviceArray removeAllObjects];
        [_tableView reloadData];
        [[RobotPenManager sharePenManager] disconnectDevice];
    }
}
/**
 获取点的信息
 */
-(void)getPointInfo:(PenPoint *)point{
    self.xValue.text = [NSString stringWithFormat:@"%hd",point.originalX];
    self.yValue.text = [NSString stringWithFormat:@"%hd",point.originalY];
    self.pressureLabel.text = [NSString stringWithFormat:@"%hd",point.pressure];
    self.electricityLabel.text = [NSString stringWithFormat:@"%ld",(long)point.batteryState];
    if (point.isTrail == YES) {
        self.routeLabel.text = [NSString stringWithFormat:@"%d",point.isTrail];
    }else{
        self.routeLabel.text = [NSString stringWithFormat:@"%d",point.isTrail];
    }


}

-(void)getBufferDevice:(PenDevice *)device{

    [self.deviceArray addObject:device];
    [self.tableView reloadData];
}
-(void)getDeviceState:(DeviceState)State{
    switch (State) {
        case DISCONNECTED:
            NSLog(@"disconnect");
            isConnect = NO;
            _blueToothButton.selected = NO;
            break;
        case CONNECTED:
            NSLog(@"CONNECTED");
            isConnect = YES;
            self.device = [[RobotPenManager sharePenManager] getConnectDevice];
            self.deviceName.text = [NSString stringWithFormat:@"%@",[self.device getName]];
            self.deviceUUID.text = [NSString stringWithFormat:@"%@",self.device.uuID];
            [[RobotPenManager sharePenManager] stopScanDevice];
            break;
        case CONNECTING:
            NSLog(@"connecting");
            break;
        default:
            
            break;
    }

}



- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    NSInteger rowNumber = 0;
    if (self.deviceArray && [self.deviceArray count] > 0) {
        rowNumber = [self.deviceArray count];
    }
    return rowNumber;
}
-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *cellIdentifier = @"cellIdentifier";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (!cell) {
        cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    cell.textLabel.text = [[self.deviceArray objectAtIndex:indexPath.row] getName];
    return cell;
}
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    NSLog(@"%s",__func__);
    PenDevice *selectItem = [self.deviceArray objectAtIndex:[indexPath row]];
    [[RobotPenManager sharePenManager] connectDevice:selectItem :self];
}


@end
