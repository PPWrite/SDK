//
//  ViewController.m
//  RobotDemo
//
//  Created by chong gao on 2016/6/28.
//  Copyright © 2016年 chong gao. All rights reserved.
//

#import "ViewController.h"
#import "RobotPenSDK.h"
#import "RobotPenService.h"
#import "RobotBoardSdk.h"
#import "MBProgressHUD.h"

#define RATIO (14335.0f/8191.0f)

@interface ViewController ()<ScanDeviceDelegate,ConnectStateDelegate,PointChangeDelegate,UITableViewDelegate,UITableViewDataSource,VideoTimeDelegate,UIImagePickerControllerDelegate,UINavigationControllerDelegate>
{
    BOOL isVer;
    BOOL isRecording;
    BOOL isPause;
    BOOL isConnected;
}
@property (weak, nonatomic) IBOutlet UIView *header;
@property (weak, nonatomic) IBOutlet UIButton *Ble;
- (IBAction)bleClick:(id)sender;
@property (weak, nonatomic) IBOutlet UIButton *mode;
- (IBAction)modeClick:(id)sender;
- (IBAction)cleanClick:(id)sender;
@property (weak, nonatomic) IBOutlet UIButton *rubber;
- (IBAction)rubberClick:(id)sender;
- (IBAction)addImageClick:(id)sender;
@property (weak, nonatomic) IBOutlet UIButton *play;
- (IBAction)playClick:(id)sender;
- (IBAction)turnClick:(id)sender;
- (IBAction)deleteClick:(id)sender;
- (IBAction)firstClick:(id)sender;
- (IBAction)upClick:(id)sender;
- (IBAction)downClick:(id)sender;
- (IBAction)lastClick:(id)sender;
- (IBAction)stopClick:(id)sender;
- (IBAction)deleteImgClick:(id)sender;
- (IBAction)pauseClick:(id)sender;
@property (weak, nonatomic) IBOutlet UIButton *pause;
@property (nonatomic, strong) RobotDrawView *drawView;
@property (nonatomic, strong) UITableView *tableView;
@property (nonatomic, strong) NSMutableArray *deviceArray;
@property (nonatomic, strong) DeviceObject *device;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor lightGrayColor];
    [self bulidDrawView];
    isVer = YES;
    [self.pause setTitle:@"暂停" forState:UIControlStateNormal];
    [self.pause setTitle:@"继续" forState:UIControlStateSelected];
    [self.Ble setTitle:@"搜索" forState:UIControlStateNormal];
    [self.Ble setTitle:@"断开" forState:UIControlStateSelected];
    UITableView *tableView = [[UITableView alloc] initWithFrame:self.drawView.frame];
    [self.view addSubview:tableView];
    self.tableView = tableView;
    tableView.dataSource = self;
    tableView.delegate = self;
    [tableView setHidden:YES];
    isConnected = NO;
    [RobotPenService sharePenService];
    [[RobotPenService sharePenService] setConnectStateDelegate:self];
    [[RobotPenService sharePenService] setPointChangeDelegate:self];
}
/**
 *
 *
 */
- (NSMutableArray *)deviceArray{
    if (!_deviceArray) {
        _deviceArray = [NSMutableArray array];
    }
    return _deviceArray;
}
/**
 * 创建画板
 */
- (void)bulidDrawView{
    RobotDrawView *view = [[RobotDrawView alloc] initWithFrame:CGRectMake(0, 0, 0, 0)];
    [self.view addSubview:view];
    self.drawView = view;
   
    NSInteger widthW = 250;
    NSInteger heightH = 250 * RATIO;
    if (widthW %2 != 0) {
        widthW += 1;
    }
    if (heightH %2 != 0) {
        heightH += 1;
    }
    self.drawView.frame = CGRectMake(0, CGRectGetMaxY(self.header.frame), widthW, heightH);
    
    self.drawView.lineWidth = 1.5;
    self.drawView.lineColor = [UIColor redColor];
    
    [self.drawView initializePage];
    [self.drawView turnToCurrentPage];
    self.drawView.timeDelegate = self;
    
    
}

/**
 *  蓝牙点击
 */
- (IBAction)bleClick:(UIButton *)sender {
    
    if (isConnected == NO) {
        sender.selected = YES;
    [[RobotPenService sharePenService] scanDevice:self];
        [self.tableView setHidden:NO];
    } else{
        sender.selected = NO;
        [self.deviceArray removeAllObjects];
        [self.tableView reloadData];
        [[RobotPenService sharePenService] disconnectDevice];
    }
}
/**
 *  笔的坐标点击
 */
- (void)change:(PointObject *)point{
    CGPoint penPoint;
    
    if (point.isRoute == YES) {
        penPoint.x =[point getSceneX:self.drawView.frame.size.width];
        penPoint.y = [point getSceneY:self.drawView.frame.size.height];
    } else{
        penPoint.x = [point getSceneX:self.drawView.frame.size.width];
        penPoint.y = [point getSceneY:self.drawView.frame.size.height];
    }
    
    [self.drawView drawChirographyWithPoint:penPoint isRoute:point.isRoute Pressure:point.Pressure];
}
/**
 *  ScanDeviceDelegate 发现设备
 */
- (void)find:(DeviceObject *)deviceObject{
   
    [self.deviceArray addObject:deviceObject];
    [self.tableView reloadData];
}
/**
 *  连接状态改变
 */
- (void)stateChange:(ConnectState)state{
    switch (state) {
        case DISCONNECTED:
        {
            isConnected = NO;
            self.Ble.selected = NO;
            
        }
            break;
        case CONNECTED:
        {
            isConnected = YES;
            NSLog(@"--%d",isConnected);
            [MBProgressHUD hideHUDForView:self.view animated:YES];
            self.device = [[RobotPenService sharePenService] getCurrDevice];
            self.device.sceneType = A5;
            [[RobotPenService sharePenService] setConnectStateDelegate:self];
            [[RobotPenService sharePenService] setPointChangeDelegate:self];
            [[RobotPenService sharePenService] stopScanDevice];
            [self.tableView setHidden:YES];
        }
            break;
        default:
            break;
    }
}

#pragma mark tableView delegate implementation
- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
    NSInteger numberOfRowsInSection = 0;
    if (self.deviceArray && [self.deviceArray count] > 0) {
        numberOfRowsInSection = [self.deviceArray count];
    }
    return numberOfRowsInSection;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
    static NSString *CellIdentifier = @"Cell";
    
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:CellIdentifier];
        cell.accessoryType = UITableViewCellAccessoryDisclosureIndicator;
    }
    
    cell.textLabel.text = [[self.deviceArray objectAtIndex:indexPath.row] getName];
    
    [cell setAccessoryType: UITableViewCellAccessoryDisclosureIndicator];
    
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    DeviceObject *selectItem = [self.deviceArray objectAtIndex:[indexPath row]];
    [[RobotPenService sharePenService] connectDevice:selectItem delegate:self];
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.backgroundColor = [UIColor lightGrayColor];
    hud.mode = MBProgressHUDModeText;
    hud.labelText = @"connecting";
    
}
/**
 *  模式选择
 */
- (IBAction)modeClick:(UIButton *)sender {
    
    if (sender.isSelected == NO) {
        sender.selected = YES;
        self.drawView.backgroundIsCanMove = YES;
        [sender setBackgroundColor:[UIColor greenColor]];
    } else{
        sender.selected = NO;
        self.drawView.backgroundIsCanMove = NO;
        [sender setBackgroundColor:[UIColor clearColor]];
    }
}
/**
 *  清屏
 */
- (IBAction)cleanClick:(id)sender {
    [self.drawView ClearBoard];
    self.drawView.lineColor = [UIColor redColor];
    self.drawView.lineWidth = 1.5;
}
/**
 *  橡皮
 */
- (IBAction)rubberClick:(UIButton *)sender {
    if (sender.isSelected == NO) {
        sender.selected = YES;
        self.drawView.eraserisEnabled = YES;
        [sender setBackgroundColor:[UIColor greenColor]];
    } else{
        sender.selected = NO;
        self.drawView.eraserisEnabled = NO;
        self.drawView.lineWidth = 1.5;
        self.drawView.lineColor = [UIColor redColor];
        [sender setBackgroundColor:[UIColor clearColor]];
    }
}
/**
 *  添加图片
 */
- (IBAction)addImageClick:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"提示" message:@"选择插入图片方式" preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *action1 = [UIAlertAction actionWithTitle:@"拍照" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        UIImagePickerControllerSourceType sourceType = UIImagePickerControllerSourceTypeCamera;
        if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]) {
            UIImagePickerController *picker = [[UIImagePickerController alloc] init];
            picker.delegate =self;
            picker.allowsEditing = NO;
            picker.sourceType = sourceType;
            
            [self presentViewController:picker animated:YES completion:^{
            }];
        }
    }];
    UIAlertAction *action2 = [UIAlertAction actionWithTitle:@"相册" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        picker.delegate = self;
        picker.allowsEditing = NO;
        [self presentViewController:picker animated:YES completion:^{
        }];
    }];
    UIAlertAction *cancle = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        
    }];
    [alert addAction:action1];
    [alert addAction:action2];
    [alert addAction:cancle];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self presentViewController:alert animated:NO completion:^{
            
        }];
    });
}

#pragma mark --AddImageDelegate
- (void)imagePickerController:(UIImagePickerController *)picker didFinishPickingMediaWithInfo:(NSDictionary<NSString *,id> *)info{
    NSString *type = [info objectForKey:UIImagePickerControllerMediaType];
    
    //当选择的类型是图片
    if ([type isEqualToString:@"public.image"])
    {
        UIImage* image = [info objectForKey:@"UIImagePickerControllerOriginalImage"];
        
        NSString *path = [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) objectAtIndex:0] stringByAppendingPathComponent:@"image1"];
        [UIImagePNGRepresentation([self fixOrientation:image]) writeToFile:path atomically:YES];
        image = nil;
        
        [picker dismissViewControllerAnimated:YES completion:^{
            
            [self.drawView setBackgroudBoardImageWithPath:path];
            
        }];
    }
}
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker{
    dispatch_async(dispatch_get_main_queue(), ^{
        [picker dismissViewControllerAnimated:YES completion:nil];
    });
    
}

/**
 *  开始录制
 */
- (IBAction)playClick:(id)sender {
    [self.drawView startRecord];
    isRecording = YES;
    isPause = NO;
}
/**
 *  横竖屏切换
 */
- (IBAction)turnClick:(id)sender {
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"警告" message:@"切换横竖屏会重置笔记" preferredStyle:UIAlertControllerStyleAlert];
    
    UIAlertAction *cancelAction = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDestructive handler:^(UIAlertAction * _Nonnull action) {
        
        [self.drawView deleteAll];
        
        if (isVer == NO) {
            isVer = YES;
            [self.drawView IsVer:YES];
        } else{
            isVer = NO;
            [self.drawView IsVer:NO];
        }
        
    }];
    [alert addAction:cancelAction];
    [alert addAction:okAction];
    [self presentViewController:alert animated:NO completion:^{
    }];
}
/**
 *  删除当前页
 */
- (IBAction)deleteClick:(id)sender {
    [self.drawView deletepage];
}
/**
 *  第一页
 */
- (IBAction)firstClick:(id)sender {
    [self.drawView turnToFirst];
}
/**
 *  上一页
 */
- (IBAction)upClick:(id)sender {
    [self.drawView turnToPrevious];
}
/**
 *  下一页
 */

- (IBAction)downClick:(id)sender {
    [self.drawView turnToNext];
}
/**
 *  最后一页
 */
- (IBAction)lastClick:(id)sender {
    [self.drawView turnToLast];
}
/**
 *  停止
 */
- (IBAction)stopClick:(id)sender {
    if (isRecording == YES) {
        [self.drawView stopRecord];
        isRecording = NO;
    }
    MBProgressHUD *hud = [MBProgressHUD showHUDAddedTo:self.view animated:YES];
    hud.dimBackground = NO;
    hud.backgroundColor = [UIColor clearColor];
    hud.mode = MBProgressHUDModeText;
    hud.color = [UIColor lightGrayColor];
    hud.labelText = @"正在处理中" ;
    [hud hide:YES afterDelay:0.5];
    
    
}
/**
 *  删除图片
 */
- (IBAction)deleteImgClick:(id)sender {
    [self.drawView deleteImg];
}
/**
 *  暂停/继续
 */
- (IBAction)pauseClick:(UIButton *)sender {
    if (isRecording == NO) {
        return;
    }
    if (sender.isSelected == NO) {
        sender.selected = YES;
        [self.drawView pauseRecord];

    } else{
        sender.selected = NO;
        [self.drawView beginRecord];
    }
}


/**
 *  录制时间协议方法
 */
- (void)time:(NSInteger)time{
    NSLog(@"time:-----%ld",(long)time);
}


- (UIImage *)fixOrientation:(UIImage *)aImage {
    
    // No-op if the orientation is already correct
    if (aImage.imageOrientation == UIImageOrientationUp)
        return aImage;
    
    // We need to calculate the proper transformation to make the image upright.
    // We do it in 2 steps: Rotate if Left/Right/Down, and then flip if Mirrored.
    CGAffineTransform transform = CGAffineTransformIdentity;
    
    switch (aImage.imageOrientation) {
        case UIImageOrientationDown:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, aImage.size.height);
            transform = CGAffineTransformRotate(transform, M_PI);
            break;
            
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformRotate(transform, M_PI_2);
            break;
            
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, 0, aImage.size.height);
            transform = CGAffineTransformRotate(transform, -M_PI_2);
            break;
        default:
            break;
    }
    
    switch (aImage.imageOrientation) {
        case UIImageOrientationUpMirrored:
        case UIImageOrientationDownMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.width, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
            
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRightMirrored:
            transform = CGAffineTransformTranslate(transform, aImage.size.height, 0);
            transform = CGAffineTransformScale(transform, -1, 1);
            break;
        default:
            break;
    }
    
    // Now we draw the underlying CGImage into a new context, applying the transform
    // calculated above.
    CGContextRef ctx = CGBitmapContextCreate(NULL, aImage.size.width, aImage.size.height,
                                             CGImageGetBitsPerComponent(aImage.CGImage), 0,
                                             CGImageGetColorSpace(aImage.CGImage),
                                             CGImageGetBitmapInfo(aImage.CGImage));
    CGContextConcatCTM(ctx, transform);
    switch (aImage.imageOrientation) {
        case UIImageOrientationLeft:
        case UIImageOrientationLeftMirrored:
        case UIImageOrientationRight:
        case UIImageOrientationRightMirrored:
            // Grr...
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.height,aImage.size.width), aImage.CGImage);
            break;
            
        default:
            CGContextDrawImage(ctx, CGRectMake(0,0,aImage.size.width,aImage.size.height), aImage.CGImage);
            break;
    }
    
    // And now we just create a new UIImage from the drawing context
    CGImageRef cgimg = CGBitmapContextCreateImage(ctx);
    UIImage *img = [UIImage imageWithCGImage:cgimg];
    CGContextRelease(ctx);
    CGImageRelease(cgimg);
    
    return img;
}

@end
