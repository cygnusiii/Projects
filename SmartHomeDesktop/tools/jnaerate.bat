rd /s /q ..\src\vpx
rd /s /q ..\src\opus

java -jar jnaerator.jar -mode Directory -synchronized -runtime BridJ -I..\include -o ..\src -library vpx ..\include\vpx\vpx_image.h ..\include\vpx\vpx_codec.h ..\include\vpx\vpx_encoder.h ..\include\vpx\vpx_decoder.h ..\include\vpx\vp8.h ..\include\vpx\vp8cx.h ..\include\vpx\vp8dx.h

java -jar jnaerator.jar -mode Directory -synchronized -runtime BridJ -I..\include -o ..\src -library opus ..\include\opus\opus.h ..\include\opus\opus_custom.h ..\include\opus\opus_defines.h ..\include\opus\opus_multistream.h ..\include\opus\opus_types.h