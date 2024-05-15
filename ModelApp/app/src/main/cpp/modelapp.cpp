#include <jni.h>
#include <string>

// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("modelapp");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("modelapp")
//      }
//    }

#include <jni.h>
#include <cstdint>
#include <algorithm>
#include <jni.h>
#include <android/bitmap.h>
#include <cstring>

extern "C" JNIEXPORT jstring

JNICALL
Java_com_example_modelapp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {

    std::string hello = "Code from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_modelapp_MainActivityKt_stringFromJNI(JNIEnv *env, jclass clazz) {
    std::string hello = "Executing from C++ Native !!";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_modelapp_MainActivityKt_Button1stringFromJNI(JNIEnv *env, jclass clazz) {
    std::string hello = "Select Image";
    return env->NewStringUTF(hello.c_str());
}




extern "C" JNIEXPORT void JNICALL
Java_com_example_modelapp_MainActivityKt_processImage(JNIEnv *env, jclass clazz, jobject first1, jobject second2) {
    void *oP;
    void *sP;
    AndroidBitmapInfo Info1;
    AndroidBitmapInfo Info2;
    AndroidBitmap_getInfo(env, first1, &Info1);
    AndroidBitmap_getInfo(env, second2, &Info2);
    AndroidBitmap_lockPixels(env, first1, &oP);
    AndroidBitmap_lockPixels(env, second2, &sP);

    float scaleX_fact = (float) Info1.width / Info2.width;
    float scaleY_fact = (float) Info1.height / Info2.height;
    int initial_x = 0;
    int initial_y = 0;

    for (int y = 0; y < Info2.height; ++y) {
        for (int x = 0; x < Info2.width; ++x) {
            int srcX = static_cast<int>(x * scaleX_fact);
            uint32_t *PD1 = (uint32_t *) ((char *) sP + y * Info2.stride + x * 4);
            int srcY = static_cast<int>(y * scaleY_fact);
            uint32_t *PS1 = (uint32_t *) ((char *) oP + srcY * Info1.stride + srcX * 4);
            *PD1 = *PS1;
        }
    }

    AndroidBitmap_unlockPixels(env, first1);
    AndroidBitmap_unlockPixels(env, second2);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_modelapp_MainActivityKt_Button2stringFromJNI(JNIEnv *env, jclass clazz) {
    std::string hello = "Predict & Get Results";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT jstring JNICALL
Java_com_example_modelapp_MainActivityKt_findbest(JNIEnv *env, jclass clazz, jfloatArray arr) {

    jsize len = env->GetArrayLength(arr);

    jfloat *arrElem = env->GetFloatArrayElements(arr, nullptr);


    jint maxIndex = std::distance(arrElem, std::max_element(arrElem, arrElem + len));


    env->ReleaseFloatArrayElements(arr, arrElem, JNI_ABORT);

    return env->NewStringUTF(std::to_string(maxIndex).c_str());
}

