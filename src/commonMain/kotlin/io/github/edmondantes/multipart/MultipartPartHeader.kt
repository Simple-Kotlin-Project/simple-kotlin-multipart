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

import io.github.edmondantes.multipart.builder.MultipartPartHeaderBuilder

/**
 * This class describe part's header of body of _multipart_ content type
 *
 * Example:
 * HeaderName: HeaderValue; Attr1Name = Attr1Value; Attr2Name = Attr2Value;
 */
public open class MultipartPartHeader(
    /**
     * Header's value
     */
    public open val value: String?,
    /**
     * Header's attribute
     *
     * Encoded in alphabetical order
     */
    public open val attributes: Map<String, String> = emptyMap(),
) {

    /**
     * Create a [MultipartPartHeaderBuilder] from current [MultipartPartHeader]
     */
    public open fun toBuilder(): MultipartPartHeaderBuilder = MultipartPartHeaderBuilder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartPartHeader) return false

        if (value != other.value) return false
        if (attributes != other.attributes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + attributes.hashCode()
        return result
    }

    public companion object {
        public val Empty: MultipartPartHeader = MultipartPartHeader(null)
    }
}
