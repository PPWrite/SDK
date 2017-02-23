//
//  Video.h
//  SqlManager
//
//  Created by JMS on 2016/12/1.
//  Copyright © 2016年 Robot.cn. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Video : NSObject

@property (nonatomic, copy)NSString *NameKey; //name
@property (nonatomic, copy)NSString *Alias; // 别名
@property (nonatomic, assign)long long Size;
@property (nonatomic, assign)long Length;
@property (nonatomic, assign)BOOL IsOnLine;
@property (nonatomic, copy)NSString *Md5Str;

@end
