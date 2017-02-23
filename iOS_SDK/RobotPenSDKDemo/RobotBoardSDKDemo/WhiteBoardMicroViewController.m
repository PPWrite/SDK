//
//  WhiteBoardMicroViewController.m
//  RobotBoardSDKDemo
//
//  Created by JMS on 2017/2/22.
//  Copyright © 2017年 JMS. All rights reserved.
//

#import "WhiteBoardMicroViewController.h"
#import "RobotWhiteBoard_MicroView.h"
#import "Header.h"
#import "RobotSqlManager.h"
#import "Video.h"
static int interval_Board = 10;
@interface WhiteBoardMicroViewController ()<WhiteBoardViewDelegate,RobotPenDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate>
{
    BOOL isRecord;
    BOOL isHorizontal; //是否横屏
    DeviceType DeviceTypes;//设备类型
    NSURL *videoPathUrl;
    
}
@property (nonatomic, strong) RobotWhiteBoard_MicroView *WhiteBoardView;
@property (weak, nonatomic) IBOutlet UIButton *BackButton;
@property (weak, nonatomic) IBOutlet UIButton *RecordingButton;
@property (weak, nonatomic) IBOutlet UIButton *StopRecordingButton;
@property (weak, nonatomic) IBOutlet UILabel *TimeLabel;
@property (weak, nonatomic) IBOutlet UIView *WBBView;

@property (nonatomic, assign) UIColor *PenColor;
@property (nonatomic, assign) CGFloat PenWidth;
@property (nonatomic, copy) NSString *NoteKey;
@property (nonatomic, copy) NSString *NoteTitle;
@end

@implementation WhiteBoardMicroViewController

-(void)BackButtonPressed:(UIButton *)sender
{
    [self dismissViewControllerAnimated:YES completion:nil];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    
    
    isRecord= NO;
   
    [self.RecordingButton setTitle:@"录制" forState:UIControlStateNormal];
    [self.RecordingButton setTitle:@"停止" forState:UIControlStateSelected];
    [self.RecordingButton addTarget:self action:@selector(RecordingClicked:) forControlEvents:UIControlEventTouchUpInside];
    
    [self.StopRecordingButton setTitle:@"暂停" forState:UIControlStateNormal];
    [self.StopRecordingButton setTitle:@"继续" forState:UIControlStateSelected];
    [self.StopRecordingButton addTarget:self action:@selector(StopRecordingClicked:) forControlEvents:UIControlEventTouchUpInside];
    
    DeviceTypes = 1;
    [self BulidWhiteBoard];
    [_BackButton addTarget:self action:@selector(BackButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    // Do any additional setup after loading the view from its nib.
}

#pragma mark - 按钮点击事件

- (IBAction)VideoAddressClicked:(id)sender
{
    [RobotSqlManager GetVideoListWithPage:0 Success:^(id responseObject) {
        
        NSArray *videoPathArray = [NSArray arrayWithArray:[responseObject objectForKey:@"Data"]];
       
        if (videoPathArray.count >0) {
            Video *models = [videoPathArray objectAtIndex:0];
            videoPathUrl = [NSURL fileURLWithPath:[RobotSqlManager GetVideoPathWithNameKey:models.NameKey]];

        }
           } Failure:^(NSError *error) {
        NSLog(@"error %@",error);
    }];
    
    if (videoPathUrl) {
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"视频地址"
                                                                                 message:[NSString stringWithFormat:@"%@",videoPathUrl]
                                                                          preferredStyle:UIAlertControllerStyleAlert ];
        
        UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil];
        [alertController addAction:cancelAction];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self presentViewController:alertController animated:YES completion:nil];
        });

    }
   
}


//暂停继续
-(void)StopRecordingClicked:(UIButton *)sender{
    if (!isRecord) {
        return;
    }
    if (sender.isSelected) {
        sender.selected = NO;
        [self.WhiteBoardView ContinueVideoRecord];
    }else{
        sender.selected = YES;
        [self.WhiteBoardView PauseVideoRecord];
    }
}
//录制和停止
-(void)RecordingClicked:(UIButton *)sender{
    
    if (sender.isSelected) {
        sender.selected = NO;
        [self.WhiteBoardView EndVideoRecord];
        isRecord = NO;
        self.TimeLabel.text = @"录制完成";
    }else{
        sender.selected = YES;
        isRecord = YES;
        [self.WhiteBoardView StartVideoRecord];
    }
    
}



- (IBAction)FrontPageClicked:(id)sender {
    [self.WhiteBoardView frontPage];
}

- (IBAction)NextPageClicked:(id)sender {
    [self.WhiteBoardView nextPage];
}
- (IBAction)DeletePageClicked:(id)sender {
    [self.WhiteBoardView delCurrPage];
}

//笔的颜色
- (IBAction)PenColorClicked:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"线的颜色" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *blueAction = [UIAlertAction actionWithTitle:@"绿色" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.PenColor = [UIColor greenColor];
    }];
    UIAlertAction *purpleAction = [UIAlertAction actionWithTitle:@"黑色" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.PenColor = [UIColor blackColor];
    }] ;
    UIAlertAction *cancleAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    [alert addAction:blueAction];
    [alert addAction:purpleAction];
    [alert addAction:cancleAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alert animated:YES completion:nil];
    });
    
}
//笔的粗细
- (IBAction)PenWidthClicked:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"笔的粗细" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *twoAction = [UIAlertAction actionWithTitle:@"2" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.PenWidth = 2.0f;
    }];
    UIAlertAction *fourAction = [UIAlertAction actionWithTitle:@"4" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.PenWidth = 4.0f;
    }] ;
    UIAlertAction *sixAction = [UIAlertAction actionWithTitle:@"6" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.PenWidth = 6.0f;
    }] ;
    UIAlertAction *cancleAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    [alert addAction:twoAction];
    [alert addAction:fourAction];
    [alert addAction:sixAction];
    [alert addAction:cancleAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alert animated:YES completion:nil];
    });
}
//插图
- (IBAction)InsertImageClicked:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"选择插图的方式" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *albumAction = [UIAlertAction actionWithTitle:@"相册" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.delegate = self;
        picker.allowsEditing = NO;
        
        picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        [self presentViewController:picker animated:YES completion:nil];
    }];
    UIAlertAction *photoAction = [UIAlertAction actionWithTitle:@"拍照" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.delegate = self;
        picker.allowsEditing = NO;
        
        picker.sourceType = UIImagePickerControllerSourceTypeCamera;
        [self presentViewController:picker animated:YES completion:nil];
        
    }] ;
    UIAlertAction *cancleAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    [alert addAction:albumAction];
    [alert addAction:photoAction];
    [alert addAction:cancleAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alert animated:YES completion:nil];
    });
    
}
//图片旋转
- (IBAction)ChangeImageClicked:(id)sender {
    if (![self.WhiteBoardView GetIsPhoto]) {
        return;
    }
    [self.WhiteBoardView currPhotoRotate90];
}
//清笔迹
- (IBAction)CleanTrailClicked:(id)sender {
    [self.WhiteBoardView cleanTrail];
}
//清图片
- (IBAction)CleanPhotoClicked:(id)sender {
    [self.WhiteBoardView cleanPhoto];
}
//清画布
- (IBAction)CleanScreenClicked:(id)sender {
    [self.WhiteBoardView cleanScreen];
}

//截图
- (IBAction)ScreenshotsClicked:(id)sender {
    [self.WhiteBoardView saveSnapshot];
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"图片地址"
                                                                             message:[NSString stringWithFormat:@"已保存到相册"]
                                                                      preferredStyle:UIAlertControllerStyleAlert ];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:nil];
    [alertController addAction:cancelAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alertController animated:YES completion:nil];
    });
    
}

#pragma mark ImagePickerDelegate
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    __weak __block id weakPicker = picker;
    [picker dismissViewControllerAnimated:YES completion:^{
        [weakPicker removeFromParentViewController];
    }];
}
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info{
    
    if ([[info objectForKey:UIImagePickerControllerMediaType] isEqualToString:@"public.image"]) {
        UIImage* image = [info objectForKey:@"UIImagePickerControllerOriginalImage"];
        NSString *path = [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingPathComponent:@"image1"];
        [UIImagePNGRepresentation(image) writeToFile:path atomically:YES];
        UIGraphicsEndImageContext();
        [self.WhiteBoardView insterPhotoWithPath:path];
        __weak __block id weakPicker = picker;
        [picker dismissViewControllerAnimated:YES completion:^{
            [weakPicker removeFromParentViewController];
            weakPicker = nil;
        }];
        image = nil;
    }
}

#pragma mark   白板状态监听
- (void)GetEndPercent:(int)percent{
//    NSLog(@"=======%d",percent);
}
- (void)GetVideoTime:(float)Seconds{
//    NSLog(@"_______%f",Seconds);
    self.TimeLabel.text = [NSString stringWithFormat:@"%@",[self getMMSSFromSS:[NSString stringWithFormat:@"%f",Seconds]]];

}
-(NSString *)getMMSSFromSS:(NSString *)totalTime{
    
    NSInteger seconds = [totalTime integerValue];
    
    NSString *str_hour = [NSString stringWithFormat:@"%02ld",seconds/3600];
    NSString *str_minute = [NSString stringWithFormat:@"%02ld",(seconds%3600)/60];
    NSString *str_second = [NSString stringWithFormat:@"%02ld",seconds%60];
    NSString *format_time = [NSString stringWithFormat:@"%@:%@:%@",str_hour,str_minute,str_second];
    
    return format_time;
    
}
//白板状态回调
- (void)GetWhiteBoardState:(NSDictionary *)StateInfo{
    
    if (StateInfo == nil) {
        return;
    }

    
    
}
//录课相关回调
- (void)GetVideoRecordState:(NSDictionary *)StateInfo{
    
    NSLog(@"录课相关回调%@",StateInfo);
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

#pragma mark 白板相关
/**
 白板创建
 */
- (void)BulidWhiteBoard{
    RobotWhiteBoard_MicroView *wbView = [[RobotWhiteBoard_MicroView alloc] init];
    wbView.whiteBoardDelegate = self;
    self.WhiteBoardView = wbView;
    [self.WBBView addSubview:_WhiteBoardView];
    [self.WhiteBoardView setBgColor:BG_NoteColor];
    
}
- (void)setWB{
    
    
    [self.WhiteBoardView setDeviceType: DeviceTypes];
    [self.WhiteBoardView setIsHorizontal:isHorizontal];
    [self.WhiteBoardView setDrawAreaFrame:CGRectMake(interval_Board , interval_Board , self.WBBView.frame.size.width - 2 * interval_Board,self.WBBView.frame.size.height - 2 * interval_Board)];
    [self.WhiteBoardView RefreshAll];
    
}

- (void)viewWillAppear:(BOOL)animated{
    //笔服务
    [[RobotPenManager sharePenManager] setPenDelegate:self];
    PenDevice *device = [[RobotPenManager sharePenManager] getConnectDevice];
    DeviceTypes = device.deviceType;
    //数据库
    [RobotSqlManager checkRobotSqlManager];
    if (![RobotSqlManager checkNoteWithNoteKey:@"WBMicro"]) {
        [self BuildTempNote];
    }
    
    //白板
    _NoteKey = @"WBMicro";
    _NoteTitle = @"录制白板";
    _PenColor = [UIColor redColor];
    _PenWidth = 1;
    [self.WhiteBoardView SetDrawType:0];
    [self setWB];
    
    
}
-(void)BuildTempNote
{
    [RobotSqlManager checkRobotSqlManager];
    RobotNote *notemodel =  [[RobotNote alloc]init];
    notemodel.NoteKey = @"WBMicro";
    notemodel.Title = @"录制白板";
    
    notemodel.DeviceType = DeviceTypes;
    notemodel.UserID = 0;
    notemodel.IsHorizontal = 0;
    
    [RobotSqlManager BulidNote:notemodel Success:^(id responseObject) {
        
    } Failure:^(NSError *error) {
    }];
    
    
}


-(void)getPointInfo:(PenPoint *)point{
    //    NSLog(@"%hd %hd",point.originalX,point.originalY);
    [self.WhiteBoardView drawLine:point];
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
