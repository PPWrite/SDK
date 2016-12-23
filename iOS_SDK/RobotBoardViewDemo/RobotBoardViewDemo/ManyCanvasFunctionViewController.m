//
//  ManyCanvasFunctionViewController.m
//  RobotBoardViewDemo
//
//  Created by 高宠 on 2016/12/22.
//  Copyright © 2016年 robotPen. All rights reserved.
//

#import "ManyCanvasFunctionViewController.h"
#import "Video.h"

#define BG_NoteColor [UIColor colorWithRed:255/255.0 green:251/255.0 blue:207/255.0 alpha:1]
#define gScreenWidth          [[UIScreen mainScreen] bounds].size.width
#define gScreenHeight         [[UIScreen mainScreen] bounds].size.height
@interface ManyCanvasFunctionViewController ()<WhiteBoardViewDelegate,RobotPenDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate>
{
    BOOL isRecord;
    NSURL *_videoPathUrl;//视频地址url
    NSString *_videoTime;
}
@property (weak, nonatomic) IBOutlet UIButton *luzhi;
@property (weak, nonatomic) IBOutlet UIButton *zanting;
@property (weak, nonatomic) IBOutlet UILabel *luzhishijian;

@property (nonatomic, strong) RobotWhiteBoard_MicroView *whiteBoardView;
@property (nonatomic, copy) NSString *NoteTitle;
@property(nonatomic,assign) UIColor *penColor;//笔迹的颜色
@property(nonatomic,assign) CGFloat penWidth;//笔迹的宽度
@property (nonatomic , strong) NSMutableArray *videoPathArray;//视频地址数组

@end

@implementation ManyCanvasFunctionViewController

-(NSMutableArray *)videoPathArray{
    if (!_videoPathArray) {
        _videoPathArray = [[NSMutableArray alloc]init];
    }
    return _videoPathArray;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    isRecord= NO;
    [self.zanting setTitle:@"暂停" forState:UIControlStateNormal];
    [self.zanting setTitle:@"继续" forState:UIControlStateSelected];
    [self.zanting addTarget:self action:@selector(zantingButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    [self.luzhi setTitle:@"录制" forState:UIControlStateNormal];
    [self.luzhi setTitle:@"停止" forState:UIControlStateSelected];
    [self.luzhi addTarget:self action:@selector(luzhiButtonPressed:) forControlEvents:UIControlEventTouchUpInside];
    [self setContentView];
    // Do any additional setup after loading the view from its nib.
}
-(void)setContentView{
    RobotWhiteBoard_MicroView *wbView = [[RobotWhiteBoard_MicroView alloc] init];
    wbView.whiteBoardDelegate = self;
    self.whiteBoardView = wbView;
    
    [self.view addSubview:self.whiteBoardView];
    self.penColor = [UIColor redColor];
    self.penWidth = 4.0f;
    [self.whiteBoardView setBgColor:BG_NoteColor];
    
}
-(void)setWB{
    [self.whiteBoardView setDeviceType:self.DeviceType];
    //    [self.whiteBoardView setIsHorizontal:isHorizontal];
    self.whiteBoardView.userInteractionEnabled = YES;
    self.NoteTitle = @"tmp";//笔记的名字
    [self.whiteBoardView setDrawAreaFrame:CGRectMake(10 , 54 , gScreenWidth - 85, gScreenHeight - 64)];
    [self.whiteBoardView RefreshAll];
}
-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [SqlManager checkSqlManager];
    [[RobotPenManager sharePenManager] setPenDelegate:self];
    PenDevice *device = [[RobotPenManager sharePenManager] getConnectDevice];
    self.DeviceType = device.deviceType;
    
    [self setWB];
}
#pragma mark WhiteBoardViewDelegate
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
-(void)getPointInfo:(PenPoint *)point{
    [self.whiteBoardView drawLine:point];
}
- (IBAction)back:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}
#pragma mark 画布上的按钮方法
//笔的颜色
- (IBAction)penColorClicked:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"线的颜色" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *blueAction = [UIAlertAction actionWithTitle:@"蓝色" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.penColor = [UIColor blueColor];
    }];
    UIAlertAction *purpleAction = [UIAlertAction actionWithTitle:@"紫色" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.penColor = [UIColor purpleColor];
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
- (IBAction)penWidthClicked:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"笔的粗细" message:nil preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction *twoAction = [UIAlertAction actionWithTitle:@"2" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.penWidth = 2.0f;
    }];
    UIAlertAction *fourAction = [UIAlertAction actionWithTitle:@"4" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.penWidth = 4.0f;
    }] ;
    UIAlertAction *sixAction = [UIAlertAction actionWithTitle:@"6" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        self.penWidth = 6.0f;
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
- (IBAction)chatu:(id)sender {
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
//清笔迹
- (IBAction)qingbiji:(id)sender {
    [self.whiteBoardView cleanTrail];
}
//清图片
- (IBAction)qingtupian:(id)sender {
    [self.whiteBoardView cleanPhoto];
}
//清画布
- (IBAction)qinghuabu:(id)sender {
    [self.whiteBoardView cleanScreen];
}
//图片旋转
- (IBAction)xuanzhuan:(id)sender {
    if (![self.whiteBoardView GetIsPhoto]) {
        return;
    }
    [self.whiteBoardView currPhotoRotate90];
}
//截图
- (IBAction)jietu:(id)sender {
    UIImage *image = [self.whiteBoardView saveSnapshot];
    NSLog(@"截图 %@",image);
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"图片地址"
                                                                             message:[NSString stringWithFormat:@"%@",image]
                                                                      preferredStyle:UIAlertControllerStyleAlert ];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil];
    [alertController addAction:cancelAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alertController animated:YES completion:nil];
    });

}
//暂停继续
-(void)zantingButtonPressed:(UIButton *)sender{
    if (!isRecord) {
        return;
    }
    if (sender.isSelected) {
        sender.selected = NO;
        [self.whiteBoardView ContinueVideoRecord];
    }else{
        sender.selected = YES;
        [self.whiteBoardView PauseVideoRecord];
    }
}
//录制和停止
-(void)luzhiButtonPressed:(UIButton *)sender{
    
    if (sender.isSelected) {
        sender.selected = NO;
        self.zanting.selected = NO;
        [self.whiteBoardView EndVideoRecord];
        isRecord = NO;
        NSString *str = @"录制完成";
        self.luzhishijian.text = [NSString stringWithFormat:@"%@%@",_videoTime,str];
    }else{
        sender.selected = YES;
        isRecord = YES;
        [self.whiteBoardView StartVideoRecord];
    }
    
}
- (IBAction)shipindizhi:(id)sender {
    [SqlManager GetVideoListWithPage:0 Success:^(id responseObject) {
        
        
        if (self.videoPathArray.count>0) {
            [self.videoPathArray removeAllObjects];
        }
        [self.videoPathArray addObjectsFromArray: [responseObject objectForKey:@"Data"]];
        NSLog(@" shipin %@",self.videoPathArray);
        Video *model = [self.videoPathArray objectAtIndex:0];
        _videoPathUrl = [NSURL fileURLWithPath:[SqlManager GetVideoPathWithNameKey:model.NameKey]];
    } Failure:^(NSError *error) {
        NSLog(@"error %@",error);
    }];
    
    
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"视频地址"
                                                                             message:[NSString stringWithFormat:@"%@",_videoPathUrl]
                                                                      preferredStyle:UIAlertControllerStyleAlert ];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil];
    [alertController addAction:cancelAction];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alertController animated:YES completion:nil];
    });
    
}
- (IBAction)shangyiye:(id)sender {
    [self.whiteBoardView frontPage];
}

- (IBAction)xiayiye:(id)sender {
    [self.whiteBoardView nextPage];
}
- (IBAction)shanchuye:(id)sender {
    [self.whiteBoardView delCurrPage];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
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
        [self.whiteBoardView insterPhotoWithPath:path];
        __weak __block id weakPicker = picker;
        [picker dismissViewControllerAnimated:YES completion:^{
            [weakPicker removeFromParentViewController];
            weakPicker = nil;
        }];
        image = nil;
    }
}


@end
