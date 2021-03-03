#include <jni.h>
#include <string>
#include "topcodes/topcode.h"
#include <android/log.h>

#define LOGV(TAG, FORMAT, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, FORMAT, __VA_ARGS__)

using namespace TopCodes;

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_topcodes_TopCodesScanner_searchTopCodesNative(JNIEnv *env, jobject thiz, jint image_width,
                                                   jint image_height, jintArray image_data) {
    auto *image = new Image();
    image->height = image_height;
    image->width = image_width;

    auto image_data_size = env->GetArrayLength(image_data);

    LOGV("Android", "Size of java imageData: %d", image_data_size);

    LOGV("Android", "Height: %d", image->height);
    LOGV("Android", "Width: %d", image->width);

    jint image_data_buf[image_data_size];
    LOGV("Android", "image_data_buf initialized", "");
    env->GetIntArrayRegion(image_data, 0, image_data_size, image_data_buf);
    LOGV("Android", "GetIntArrayRegion called", "");

    image->ucdata = (unsigned int *) image_data_buf;
    LOGV("Android", "image->ucdata initialized", "");

/*
    auto clazz = env->FindClass("topcodes/TopCode");
    auto object = env->AllocObject(clazz);
    return env->NewObjectArray(0, clazz, object);

    std::vector<Code*> topcode_codes = std::vector<Code *>();

    LOGV("Android", "First array element ucdata %d", image->ucdata[0]);
*/

    Scanner scanner;
    std::vector<Code *> topcode_codes = scanner.scan(image, nullptr);

    auto clazz = env->FindClass("topcodes/TopCode");

    auto fieldCenterX = env->GetFieldID(clazz, "centerX", "F");
    auto fieldCenterY = env->GetFieldID(clazz, "centerY", "F");
    auto fieldAngleRadians = env->GetFieldID(clazz, "angleInRadians", "F");
    auto fieldUnit = env->GetFieldID(clazz, "unit", "F");
    auto fieldCode = env->GetFieldID(clazz, "code", "I");

    auto num_topcodes = topcode_codes.size();

    LOGV("Android", "num_topcodes %d", num_topcodes);

    auto object = env->AllocObject(clazz);
    auto array = env->NewObjectArray(num_topcodes, clazz, object);

    for (auto i = 0; i < num_topcodes; i++) {
        auto topcode = topcode_codes[i];

        object = env->AllocObject(clazz);
        env->SetFloatField(object, fieldCenterX, topcode->x);
        env->SetFloatField(object, fieldCenterY, topcode->y);
        env->SetFloatField(object, fieldAngleRadians, topcode->orientation);
        env->SetFloatField(object, fieldUnit, topcode->unit);
        env->SetIntField(object, fieldCode, topcode->code);
        env->SetObjectArrayElement(array, i, object);
    }

    scanner.disposeCodes(topcode_codes);
    delete image;

    return array;
}