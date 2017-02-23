//
//  VideoImageModel.h
//  PPWrite
//
//  Created by chong gao on 2016/12/6.
//  Copyright © 2016年 JMS. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface VideoImageModel : UIView
@property (nonatomic, assign)unsigned char *imageData;
@property (nonatomic, assign)uint8_t **YuvData;
@end
