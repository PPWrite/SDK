//
//  RecordImageUtil.h
//  RecordDemo
//
//  Created by Xiaoz on 16/4/9.
//  Copyright © 2016年 Luistrue. All rights reserved.
//

#ifndef RecordImageUtil_h
#define RecordImageUtil_h

#ifdef __cplusplus

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/opt.h>
#include <libavutil/timestamp.h>
#include <libswscale/swscale.h>
#include <libswresample/swresample.h>
}

typedef struct OutputStream {
    AVStream *st;
    
    /* pts of the next frame that will be generated */
    int64_t next_pts;
    int samples_count;
    double pts_time;
    
    AVFrame *frame;
    
    struct SwsContext *sws_ctx;
    struct SwrContext *swr_ctx;
} OutputStream;


class RecordImageUtil {
public:
    RecordImageUtil();
    virtual ~RecordImageUtil();
    
    //设置rate
    void setVideoRate(int rate);
    
    //获取图像与声音的时差
    float getTimeDifference();
    
    //开始
    int start(const char *out_file, int width, int height);
    
    //结束
    void end();
    
    //追加音频
    int appendAudio(uint8_t *audioByte);
    
    //追加图片
    int appendImage(uint8_t *pBuffer);
    
private:
    int have_video,have_audio;
    //定义输入图像解码器
    AVCodecContext  *pICodecCtx;
    AVFormatContext *pOFormatCtx;
    AVOutputFormat  *pOutputFmt;
    OutputStream mOVideoStream = { 0 },	mOAudioStream = { 0 };
    //音、视频编码器
    AVCodec *pAudioCodec, *pVideoCodec;
    
    void release_resources();
};

#endif

#endif /* RecordImageUtil_h */
