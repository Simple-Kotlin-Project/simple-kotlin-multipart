/*
 * Copyright (c) 2023. Ilia Loginov
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.edmondantes.multipart

import io.github.edmondantes.multipart.builder.MultipartBuilder

/**
 * This class describe body of _multipart_ content type
 */
public open class Multipart public constructor(
    /**
     * Body parts
     */
    public open val parts: List<MultipartPart> = emptyList(),
    /**
     * Preamble before body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public open val preamble: ByteArray? = null,
    /**
     * Epilogue after body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public open val epilogue: ByteArray? = null,
) {

    /**
     * Create a [MultipartBuilder] from current [Multipart]
     */
    public open fun toBuilder(): MultipartBuilder = MultipartBuilder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Multipart) return false

        if (parts != other.parts) return false
        if (preamble != null) {
            if (other.preamble == null) return false
            if (!preamble.contentEquals(other.preamble)) return false
        } else if (other.preamble != null) return false
        if (epilogue != null) {
            if (other.epilogue == null) return false
            if (!epilogue.contentEquals(other.epilogue)) return false
        } else if (other.epilogue != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = parts.hashCode()
        result = 31 * result + (preamble?.contentHashCode() ?: 0)
        result = 31 * result + (epilogue?.contentHashCode() ?: 0)
        return result
    }

    public companion object {
        public val Empty: Multipart = Multipart(emptyList(), null, null)
    }
}
