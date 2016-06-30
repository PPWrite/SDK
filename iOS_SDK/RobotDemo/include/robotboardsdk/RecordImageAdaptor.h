//
//  RecordImageAdaptor.h
//  RecordDemo
//
//  Created by Xiaoz on 16/4/9.
//  Copyright © 2016年 Luistrue. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>

#ifdef __cplusplus
class RecordImageUtil;
#endif

//class RecordImageUtil;

@interface RecordImageAdaptor : NSObject{
@private
#ifdef __cplusplus
    RecordImageUtil *recordUtil;
#endif
    

}

- (void) setVideoRate:(int)rate;
- (float) getTimeDifference;
- (int) recordStart:(const char*)path videoW:(int)width videoH:(int)height;
- (void) recordEnd;
- (int) appendAudio:(uint8_t *)audioByte;
- (int) appendImage:(uint8_t *)imgByte;

@end

