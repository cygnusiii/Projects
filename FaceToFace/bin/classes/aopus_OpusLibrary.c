#include "aopus_OpusLibrary.h"

#include <stdlib.h>
#include <string.h>
#include <android/log.h>

#include "../include/opus/opus.h"

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  JNIEnv* env;
  if ((* vm)->GetEnv(vm, (void**)(&env), JNI_VERSION_1_6) != JNI_OK) {
    return JNI_ERR;
  }
  return JNI_VERSION_1_6;
}

typedef struct
{
    OpusEncoder   *enc;
    int            frameSizePerChannel;
    int            maxBufferSize;
    unsigned char *buffer;
} Encoder;

JNIEXPORT jlong JNICALL Java_aopus_OpusLibrary_encoderCreate
  (JNIEnv *env, jobject obj, jint clockRate, jint channels, jint packetTime)
{
    Encoder *encoder = malloc(sizeof* encoder);
    
    encoder->frameSizePerChannel = (clockRate * packetTime) / 1000;
    encoder->maxBufferSize = 4000;
    encoder->buffer = malloc(encoder->maxBufferSize);

    // initialize encoder
    int error;
    encoder->enc = opus_encoder_create(clockRate, channels, OPUS_APPLICATION_VOIP, &error);
    if (error != OPUS_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, "fm.libopus", "Could not initialize encoder. %d", error);
        return 0;
    }
    return encoder;
}

JNIEXPORT void JNICALL Java_aopus_OpusLibrary_encoderDestroy
  (JNIEnv *env, jobject obj, jlong state)
{
    Encoder *encoder = (Encoder *)state;
    
    if (encoder->enc)
    {
        opus_encoder_destroy(encoder->enc);
        encoder->enc = NULL;
    }

    if (encoder->buffer)
    {
        free(encoder->buffer);
        encoder->buffer = NULL;
    }
    
    free(encoder);
}

JNIEXPORT jbyteArray JNICALL Java_aopus_OpusLibrary_encoderEncode
  (JNIEnv *env, jobject obj, jlong state, jbyteArray data, jint index, jint length)
{
    Encoder *encoder = (Encoder *)state;
    
    // copy managed to unmanaged
    unsigned char *dataBytes = malloc(length);
    (* env)->GetByteArrayRegion(env, data, index, length, dataBytes);

    int encodedLength = opus_encode(encoder->enc, (opus_int16 *)dataBytes, encoder->frameSizePerChannel, encoder->buffer, encoder->maxBufferSize);
    free(dataBytes);

    if (encodedLength > 0)
    {
        // copy unmanaged to managed
        jbyteArray encodedFrame = (* env)->NewByteArray(env, encodedLength);
        (* env)->SetByteArrayRegion(env, encodedFrame, 0, encodedLength, encoder->buffer);
        return encodedFrame;
    }
    
    return NULL;
}

typedef struct
{
    OpusDecoder   *dec;
    int            frameSizePerChannel;
    int            maxBufferSize;
    unsigned char *buffer;
    int            channels;
    int            previousPacketInvalid;
} Decoder;

JNIEXPORT jlong JNICALL Java_aopus_OpusLibrary_decoderCreate
  (JNIEnv *env, jobject obj, jint clockRate, jint channels, jint packetTime)
{
    Decoder *decoder = malloc(sizeof* decoder);
    
    decoder->frameSizePerChannel = (clockRate * packetTime) / 1000;
    decoder->maxBufferSize = clockRate * channels * packetTime * 2 / 1000; // 2 bytes per sample, 1000 ms per second
    decoder->buffer = malloc(decoder->maxBufferSize);

    decoder->channels = channels;
    decoder->previousPacketInvalid = 0;

    // initialize decoder
    int error;
    decoder->dec = opus_decoder_create(clockRate, channels, &error);
    if (error != OPUS_OK)
    {
        __android_log_print(ANDROID_LOG_ERROR, "fm.libopus", "Could not initialize decoder. %d", error);
        return 0;
    }
    return decoder;
}

JNIEXPORT void JNICALL Java_aopus_OpusLibrary_decoderDestroy
  (JNIEnv *env, jobject obj, jlong state)
{
    Decoder *decoder = (Decoder *)state;
    
    if (decoder->dec)
    {
        opus_decoder_destroy(decoder->dec);
        decoder->dec = NULL;
    }

    if (decoder->buffer)
    {
        free(decoder->buffer);
        decoder->buffer = NULL;
    }
    
    free(decoder);
}

JNIEXPORT jbyteArray JNICALL Java_aopus_OpusLibrary_decoderDecode
  (JNIEnv *env, jobject obj, jlong state, jbyteArray encodedData)
{
    Decoder *decoder = (Decoder *)state;
    
    // copy managed to unmanaged
    int encodedLength = (* env)->GetArrayLength(env, encodedData);
    unsigned char *encodedDataBytes = malloc(encodedLength);
    (* env)->GetByteArrayRegion(env, encodedData, 0, encodedLength, encodedDataBytes);

    int numSamplesDecoded;
    int bandwidth = opus_packet_get_bandwidth(encodedDataBytes);
    if (bandwidth == OPUS_INVALID_PACKET)
    {
        numSamplesDecoded = opus_decode(decoder->dec, 0, 0, (opus_int16 *)decoder->buffer, decoder->frameSizePerChannel, 0);
        decoder->previousPacketInvalid = 1;
    }
    else
    {
        numSamplesDecoded = opus_decode(decoder->dec, encodedDataBytes, encodedLength, (opus_int16 *)decoder->buffer, decoder->frameSizePerChannel, decoder->previousPacketInvalid);
        decoder->previousPacketInvalid = 0;
    }
    free(encodedDataBytes);

    if (numSamplesDecoded > 0)
    {
        // copy unmanaged to managed
        int frameLength = numSamplesDecoded * decoder->channels * 2;
        jbyteArray frame = (* env)->NewByteArray(env, frameLength); // 2 bytes per sample
        (* env)->SetByteArrayRegion(env, frame, 0, frameLength, decoder->buffer);
        return frame;
    }

    return NULL;
}