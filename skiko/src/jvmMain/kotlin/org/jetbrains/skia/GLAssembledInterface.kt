package org.jetbrains.skia

import org.jetbrains.skia.impl.Library.Companion.staticLoad
import org.jetbrains.skia.impl.NativePointer
import org.jetbrains.skia.impl.RefCnt
import org.jetbrains.skia.impl.Stats
import org.jetbrains.skiko.RenderException

class GLAssembledInterface internal constructor(ptr: NativePointer) : RefCnt(ptr) {
    companion object {
        /**
         * Creates an OpenGL interface object.
         *
         * There must be a current OpenGL context set (i.e., by calling `eglMakeCurrent` before this), otherwise
         * this function will fail.
         * For more information refer to skia `GrGLMakeAssembledInterface` function.
         *
         * For example, this `GetGLFuncPtrByName` function could be passed as a [fPtr]:
         *  ```
         *  typedef void(*GLFuncPtr)();
         *  GLFuncPtr GetGLFuncPtrByName(void* ctx, const char* name);
         *  ```
         *
         * @param ctxPtr  native pointer to the custom context, that [fPtr] will be called with.
         * @param fPtr    native pointer to the function that takes [ctxPtr] and the OpenGL function name,
         *                and returns a function pointer of that OpenGL function (see skia `GrGLGetProc`).
         */
        fun createFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): GLAssembledInterface {
            if (fPtr == NullPointer) throw RenderException("Function pointer must not be null")
            Stats.onNativeCall()
            val ptr = _nCreateFromNativePointers(ctxPtr, fPtr)
            if (ptr == NullPointer) throw RenderException("Can't assemble OpenGL interface")
            return GLAssembledInterface(ptr)
        }

        /**
         * Creates an OpenGL interface object from a loader with the single-argument shape
         * used by `eglGetProcAddress` and `glXGetProcAddress`:
         *  ```
         *  typedef void(*GLFuncPtr)();
         *  GLFuncPtr eglGetProcAddress(const char* name);
         *  ```
         * That signature is not ABI-compatible with skia's `GrGLGetProc`, which takes a
         * leading context argument, so it cannot be handed to [createFromNativePointers]
         * directly. Use this when the OpenGL implementation is supplied by the host rather
         * than by the system driver (a GL-over-GLES translation layer, ANGLE, an Android
         * launcher's bundled loader), so skia resolves against that loader instead of
         * whatever it linked against at build time.
         *
         * There must be a current OpenGL context set, otherwise this function will fail.
         *
         * @param fPtr  native pointer to the `GLFuncPtr(const char*)` loader.
         */
        fun createFromProcAddress(fPtr: NativePointer): GLAssembledInterface {
            if (fPtr == NullPointer) throw RenderException("Function pointer must not be null")
            Stats.onNativeCall()
            val ptr = _nCreateFromProcAddress(fPtr)
            if (ptr == NullPointer) throw RenderException("Can't assemble OpenGL interface")
            return GLAssembledInterface(ptr)
        }

        init {
            staticLoad()
        }
    }
}

private external fun _nCreateFromNativePointers(ctxPtr: NativePointer, fPtr: NativePointer): NativePointer

private external fun _nCreateFromProcAddress(fPtr: NativePointer): NativePointer
