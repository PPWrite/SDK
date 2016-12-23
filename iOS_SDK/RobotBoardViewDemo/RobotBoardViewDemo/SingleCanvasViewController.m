//
//  SingleCanvasViewController.m
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/14.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "SingleCanvasViewController.h"

#define BG_NoteColor [UIColor colorWithRed:255/255.0 green:251/255.0 blue:207/255.0 alpha:1]
#define gScreenWidth          [[UIScreen mainScreen] bounds].size.width
#define gScreenHeight         [[UIScreen mainScreen] bounds].size.height
@interface SingleCanvasViewController ()<WhiteBoardViewDelegate,RobotPenDelegate>
{
    BOOL isHorizontal; //是否横屏
}
@property (nonatomic, strong) RobotWhiteBoard_MicroView *whiteBoardView;
@property (nonatomic, copy) NSString *NoteTitle;
@property(nonatomic,assign) UIColor *penColor;//笔迹的颜色
@property(nonatomic,assign) CGFloat penWidth;//笔迹的宽度
@end


static int interval_Board = 10;

@implementation SingleCanvasViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setContentView];
    // Do any additional setup after loading the view from its nib.
}
-(void)setContentView{
    RobotWhiteBoard_MicroView *wbView = [[RobotWhiteBoard_MicroView alloc] init];
    wbView.whiteBoardDelegate = self;
    self.whiteBoardView = wbView;
    
    [self.view addSubview:self.whiteBoardView];
    self.penColor = [UIColor redColor];
    self.penWidth = 5.0f;
    [self.whiteBoardView setBgColor:BG_NoteColor];
    UIButton *clearButton = [UIButton buttonWithType:UIButtonTypeCustom];
    [clearButton setTitle:@"清除所有" forState:UIControlStateNormal];
    [clearButton setBackgroundColor:[UIColor redColor]];
    [clearButton setFrame:CGRectMake(0, 0, 80, 40)];
    [clearButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearButton addTarget:self action:@selector(clearAll) forControlEvents:UIControlEventTouchUpInside];
    [self.whiteBoardView addSubview:clearButton];
    
}
-(void)setWB{
    [self.whiteBoardView setDeviceType:self.DeviceType];
    [self.whiteBoardView setIsHorizontal:isHorizontal];
    self.whiteBoardView.userInteractionEnabled = YES;
    self.NoteTitle = @"tmp";//笔记的名字
    [self.whiteBoardView setDrawAreaFrame:CGRectMake(10 , 54 , gScreenWidth - 2 * 10, gScreenHeight - 64)];
//    if (isHorizontal) {
//        
//        float interval = 0.0f;
//        if (gScreenWidth > h7Plus) {
//            interval = interval_Board;
//        } else {
//            interval = interval_Board * gScreenWidth / h7Plus;
//        }
//        
//        [self.whiteBoardView setDrawAreaFrame:CGRectMake(interval , interval + 20, gScreenWidth - 2 * interval , gScreenHeight - 2 * interval - 20)];
//        
//    } else {
//        float interval = 0.0f;
//        if (gScreenWidth > w7Plus) {
//            interval = interval_Board;
//        } else {
//            interval = interval_Board * gScreenWidth / w7Plus;
//        }
//        [self.whiteBoardView setDrawAreaFrame:CGRectMake(interval , interval , gScreenWidth - 2 * interval, gScreenHeight - 2 * interval)];
//    }
    
    [self.whiteBoardView RefreshAll];
    
}


-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
     [[RobotPenManager sharePenManager] setPenDelegate:self];
    PenDevice *device = [[RobotPenManager sharePenManager] getConnectDevice];
    self.DeviceType = device.deviceType;

    [self setWB];
}

- (IBAction)back:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}
//笔的颜色
-(UIColor *)getPenColor{
    return _penColor;
}
//笔的宽度
-(CGFloat)getPenWeight{
    return _penWidth/[UIScreen mainScreen].scale;
}
-(long)getCurrUserId{
    return 0;
}
- (int)getIsRubber{
    return 0;
}
-(NSString *)getNoteTitle{
    return _NoteTitle;
}
-(NSString *)getNoteKey{
    return @"TempNote";
}

//清除所有
-(void)clearAll{
    [self.whiteBoardView cleanScreen];
}

-(void)getPointInfo:(PenPoint *)point{
    NSLog(@"%hd %hd",point.originalX,point.originalY);
    [self.whiteBoardView drawLine:point];
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
