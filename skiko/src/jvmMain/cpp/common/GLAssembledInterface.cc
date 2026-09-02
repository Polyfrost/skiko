#include <jni.h>

#include "ganesh/gl/GrGLAssembleInterface.h"
#include "ganesh/gl/GrGLInterface.h"

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_GLAssembledInterfaceKt__1nCreateFromNativePointers
  (JNIEnv* env, jclass jclass, jlong ctxPtr, jlong fPtr) {
    void* ctx = reinterpret_cast<void*>(static_cast<uintptr_t>(ctxPtr));
    GrGLGetProc f = reinterpret_cast<GrGLGetProc>(static_cast<uintptr_t>(fPtr));
    sk_sp<const GrGLInterface> interface = GrGLMakeAssembledInterface(ctx, f);
    return reinterpret_cast<jlong>(interface.release());
}

// Adapter for loaders exposing the one-argument `eglGetProcAddress`/`glXGetProcAddress`
// shape instead of skia's two-argument GrGLGetProc. The loader itself is smuggled through
// the `ctx` slot, so no per-call state is needed.
typedef void (*SkikoGLFuncPtr)();
typedef SkikoGLFuncPtr (*SkikoGetProcAddress)(const char*);

static GrGLFuncPtr skiko_proc_address_adapter(void* ctx, const char name[]) {
    SkikoGetProcAddress get = reinterpret_cast<SkikoGetProcAddress>(ctx);
    return reinterpret_cast<GrGLFuncPtr>(get(name));
}

extern "C" JNIEXPORT jlong JNICALL Java_org_jetbrains_skia_GLAssembledInterfaceKt__1nCreateFromProcAddress
  (JNIEnv* env, jclass jclass, jlong fPtr) {
    void* ctx = reinterpret_cast<void*>(static_cast<uintptr_t>(fPtr));
    sk_sp<const GrGLInterface> interface = GrGLMakeAssembledInterface(ctx, skiko_proc_address_adapter);
    return reinterpret_cast<jlong>(interface.release());
}
