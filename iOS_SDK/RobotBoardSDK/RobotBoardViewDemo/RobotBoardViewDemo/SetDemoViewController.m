//
//  SetDemoViewController.m
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/14.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "SetDemoViewController.h"
#import "DeviceInfoTableViewCell.h"
#import "RobotWhiteBoardSdk.h"
@interface SetDemoViewController ()<UITableViewDelegate,UITableViewDataSource,RobotPenDelegate,DeviceInfoTableViewCellDelegate>
{
    BOOL isConnect;
    BOOL deleteConnect;//是否删除配对
    int allNoteNum;//所有未同步笔记
    int allhaveNoteNum;//剩余未同步笔记
}
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIView *naviView;
//@property (weak, nonatomic) IBOutlet UIButton *searchButton;
@property (nonatomic,strong) UIButton *searchButton;
@property(nonatomic,strong)PenDevice *device;
@property(nonatomic,strong)NSMutableArray *deviceArray;
@end

@implementation SetDemoViewController

-(NSMutableArray *)deviceArray{
    if (!_deviceArray) {
        _deviceArray = [NSMutableArray array];
    }
    return _deviceArray;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.tableView.delegate = self;
    self.tableView.dataSource = self;
    [self.naviView setFrame:CGRectMake(0, 0, self.view.frame.size.width, 44)];

    self.searchButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [self.searchButton setFrame:CGRectMake(33, self.view.frame.size.height-60, self.view.frame.size.width-66, 44)];
    [self.searchButton setBackgroundColor:[UIColor colorWithRed:120.0/255.0 green:89.0/255.0 blue:116.0/255.0 alpha:1.0]];
    [self.searchButton setTitle:@"查找设备" forState:UIControlStateNormal];
//    [self.searchButton setTitle:@"断开连接" forState:UIControlStateSelected];
    [self.searchButton addTarget:self action:@selector(searchButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    [self.view addSubview:self.searchButton];
    // Do any additional setup after loading the view from its nib.
}
-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    self.device = [[RobotPenManager sharePenManager]getConnectDevice];
    NSLog(@"%@",self.device.getName);
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}
- (IBAction)backButtonPressed:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}
-(void)searchButtonPressed:(UIButton *)sender{
    NSLog(@"%s",__func__);
    [self.deviceArray removeAllObjects];
    [[RobotPenManager sharePenManager] scanDevice:self];
        
    
}
-(void)getBufferDevice:(PenDevice *)device{
    
    [self.deviceArray addObject:device];
    NSLog(@"%lu",(unsigned long)[self.deviceArray count]);
    [self.tableView reloadData];
}
-(void)getDeviceState:(DeviceState)State{
    switch (State) {
        case DISCONNECTED:
            NSLog(@"disconnect");
            isConnect = NO;
            
            break;
        case CONNECTED:
            NSLog(@"CONNECTED");
            isConnect = YES;
            self.device = [[RobotPenManager sharePenManager] getConnectDevice];
            
            [[RobotPenManager sharePenManager] stopScanDevice];
            break;
        case CONNECTING:
            NSLog(@"connecting");
            break;
        default:
            
            break;
    }
    
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    if (self.device == nil ) {
        NSInteger rowNum = 0;
        if (self.deviceArray && [self.deviceArray count] > 0) {
            rowNum = [self.deviceArray count];
        }
        return rowNum;
    } else {
        
        if (allhaveNoteNum>0) {
            return 5;
        }
        return 4;
    }

}
-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPat{
    return 80;
}
-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *cellIdentifier = @"cellIdentifier";
    DeviceInfoTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellIdentifier];
    if (!cell) {
        cell = [[DeviceInfoTableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:cellIdentifier];
    }
    if (self.device == nil) {
        cell.deviceTitleLabel.text = [[self.deviceArray objectAtIndex:indexPath.row] getName];
        cell.deviceContentLabel.text = [[self.deviceArray objectAtIndex:indexPath.row] uuID];
        cell.deviceButton.hidden = YES;
    }else{
        if (indexPath.row == 0) {
            cell.deviceTitleLabel.text = [NSString stringWithFormat:@"%@",self.device.getName];
            cell.deviceContentLabel.text = @"断开连接后可连接其他设备";
            [cell.deviceButton setTitle:@"断开连接" forState:UIControlStateNormal];
            cell.deviceButton.hidden = NO;
        }
        cell.delegate = self;
        cell.deviceButton.tag = 100 + indexPath.row;
    }
    
        return cell;
}
-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    if (self.device == nil) {
        PenDevice *seleted = [self.deviceArray objectAtIndex:indexPath.row];
        [[RobotPenManager sharePenManager] connectDevice:seleted :self];
       
    }
     [self.tableView reloadData];

}
-(void)DeviceInfoButtonPressed:(UIButton *)sender{
    if (sender.tag == 100) {
        [[RobotPenManager sharePenManager]disconnectDevice];
        [self.deviceArray removeAllObjects];
        self.device = nil;
        [self.tableView reloadData];
    }
}

@end
