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

import io.github.edmondantes.multipart.builder.MultipartPartBuilder

/**
 * This class describe part of body of _multipart_ content type
 */
public open class MultipartPart(
    /**
     * Part's body
     */
    public open val body: ByteArray,
    /**
     * Part's headers
     *
     * Encoded in alphabetical order
     */
    public open val headers: Map<String, MultipartPartHeader> = emptyMap(),
) {

    /**
     * Create a [MultipartPartBuilder] from current [MultipartPart]
     */
    public open fun toBuilder(): MultipartPartBuilder = MultipartPartBuilder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartPart) return false

        if (!body.contentEquals(other.body)) return false
        if (headers != other.headers) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body.contentHashCode()
        result = 31 * result + headers.hashCode()
        return result
    }

    public companion object {
        public val Empty: MultipartPart = MultipartPart(byteArrayOf())
    }
}
