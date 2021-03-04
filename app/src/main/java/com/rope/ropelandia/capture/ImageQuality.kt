package com.rope.ropelandia.capture

enum class ImageQuality {
    HIGH {
        override fun floatValue() = 1.0f
    },
    MEDIUM {
        override fun floatValue() = .5f
    },
    LOW {
        override fun floatValue() = .3f
    };

    abstract fun floatValue(): Float
}