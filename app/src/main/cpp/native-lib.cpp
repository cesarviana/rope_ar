#include <jni.h>
#include <string>
#include "topcodes/topcode.h"

using namespace TopCodes;

extern "C"
JNIEXPORT jobjectArray JNICALL
Java_topcodes_TopCodesScanner_searchTopCodesNative(JNIEnv *env, __unused jobject _, jint image_width,
                                                   jint image_height, jintArray image_data) {
    auto *image = new Image();
    image->height = image_height;
    image->width = image_width;

    auto image_data_size = env->GetArrayLength(image_data);

    std::vector<jint> image_data_buf(image_data_size);
    env->GetIntArrayRegion(image_data, 0, image_data_size, image_data_buf.data());

    image->ucdata = (unsigned int *) image_data_buf.data();

/*
    auto clas = env->FindClass("topcodes/TopCode");
    auto object = env->AllocObject(clas);
    return env->NewObjectArray(0, clas, object);
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

    auto object = env->AllocObject(clazz);
    auto java_topcodes = env->NewObjectArray(num_topcodes, clazz, object);

    for (auto i = 0; i < num_topcodes; i++) {
        auto topcode = topcode_codes[i];

        object = env->AllocObject(clazz);
        env->SetFloatField(object, fieldCenterX, topcode->x);
        env->SetFloatField(object, fieldCenterY, topcode->y);
        env->SetFloatField(object, fieldAngleRadians, topcode->orientation);
        env->SetFloatField(object, fieldUnit, topcode->unit);
        env->SetIntField(object, fieldCode, topcode->code);
        env->SetObjectArrayElement(java_topcodes, i, object);
    }

    scanner.disposeCodes(topcode_codes);
    delete image;

    return java_topcodes;

}