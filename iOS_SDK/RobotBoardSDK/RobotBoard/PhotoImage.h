//
//  PhotoImage.h
//  PPWrite
//
//  Created by JMS on 2016/12/2.
//  Copyright © 2016年 JMS. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface PhotoImage : NSObject

@property (nonatomic, copy)NSString *Ext;
@property (nonatomic, copy)NSString * blockKey;
@property (nonatomic, strong) UIImage *Image;

+ (PhotoImage *)ImageWithData:(NSData *)data;
+ (PhotoImage *)ImageWithContentsOfFile:(NSString *)path;


@end
