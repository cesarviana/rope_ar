#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_rope_ropelandia_study_StudyActivity_stringFromJNI(JNIEnv *env, jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}