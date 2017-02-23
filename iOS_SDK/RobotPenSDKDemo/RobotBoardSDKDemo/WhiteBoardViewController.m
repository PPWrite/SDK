//
//  WhiteBoardViewController.m
//  RobotBoardSDKDemo
//
//  Created by JMS on 2017/2/21.
//  Copyright © 2017年 JMS. All rights reserved.
//

#import "WhiteBoardViewController.h"
#import "RobotWhiteBoardView.h"
#import "Header.h"
#import "RobotSqlManager.h"
static int interval_Board = 10;
@interface WhiteBoardViewController ()<WhiteBoardViewDelegate,RobotPenDelegate>
{
    BOOL isHorizontal; //是否横屏
    DeviceType DeviceTypes;//设备类型
    
}
@property (nonatomic, weak) IBOutlet UIButton *BackButton;
@property (nonatomic, weak) IBOutlet UIButton *CleanButton;
@property (nonatomic, weak) IBOutlet UIView *WBBView;

@property (nonatomic, strong) RobotWhiteBoardView *WhiteBoardView;



@property (nonatomic, assign) UIColor *PenColor;
@property (nonatomic, assign) CGFloat PenWidth;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, copy) NSString *NoteTitle;
@end

@implementation WhiteBoardViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    DeviceTypes = 1;
    [self BulidWhiteBoard];
    
    [_BackButton addTarget:self action:@selector(BackButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    [_CleanButton addTarget:self action:@selector(CleanButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    // Do any additional setup after loading the view from its nib.
}
-(void)BackButtonPressed:(UIButton *)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}
-(void)CleanButtonPressed:(UIButton *)sender
{
    [self.WhiteBoardView cleanScreen];

}



#pragma mark Notification画板
//获取当前用户ID
- (long)getCurrUserId{
    return 0;
}
//大于0表示是橡皮擦
- (int)getIsRubber{
    return 0;
}


//大于0表示是有压感
- (int)getIsPressure
{
    return 0;
}

//获取当前笔记本Key
- (NSString *)getNoteKey{
    return _NoteKey;
}
//获取笔颜色
- (UIColor *)getPenColor{
    return _PenColor;
}

//获取笔的粗细
- (CGFloat)getPenWeight{

    return _PenWidth/[UIScreen mainScreen].scale;
}

- (NSString *)getNoteTitle
{
    return _NoteTitle;
}


-(void)getPointInfo:(PenPoint *)point{
    //    NSLog(@"%hd %hd",point.originalX,point.originalY);
    [self.WhiteBoardView drawLine:point];
}
#pragma mark 白板相关
/**
 白板创建
 */
- (void)BulidWhiteBoard{
    RobotWhiteBoardView *wbView = [[RobotWhiteBoardView alloc] init];
    wbView.whiteBoardDelegate = self;
    self.WhiteBoardView = wbView;
    [self.WBBView addSubview:_WhiteBoardView];
    [self.WhiteBoardView setBgColor:BG_NoteColor];
    
}
- (void)setWB{
    
    [self.WhiteBoardView setDeviceType: DeviceTypes];
    [self.WhiteBoardView setIsHorizontal:isHorizontal];

    [self.WhiteBoardView setDrawAreaFrame:CGRectMake(interval_Board , interval_Board , ScreenWidth - 2 * interval_Board,ScreenHeight - 2 * interval_Board - 64)];

    [self.WhiteBoardView RefreshAll];
    
}

- (void)viewWillAppear:(BOOL)animated{
    //笔服务
    [[RobotPenManager sharePenManager] setPenDelegate:self];
    PenDevice *device = [[RobotPenManager sharePenManager] getConnectDevice];
    DeviceTypes = device.deviceType;
    //数据库
    [RobotSqlManager checkRobotSqlManager];
    if (![RobotSqlManager checkNoteWithNoteKey:@"WB"]) {
        [self BuildTempNote];
    }
    
    //白板信息
     _NoteKey = @"WB";
    _NoteTitle = @"白板";
    _PenColor = [UIColor redColor];
    _PenWidth = 1;
    [self.WhiteBoardView SetDrawType:0];

    [self setWB];


}



-(void)BuildTempNote
{
    [RobotSqlManager checkRobotSqlManager];
    RobotNote *notemodel =  [[RobotNote alloc]init];
    notemodel.NoteKey = @"WB";
    notemodel.Title = @"白板";
    
    notemodel.DeviceType = DeviceTypes;
    notemodel.UserID = 0;
    notemodel.IsHorizontal = 0;
    
    [RobotSqlManager BulidNote:notemodel Success:^(id responseObject) {

    } Failure:^(NSError *error) {
    }];
    
    
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
