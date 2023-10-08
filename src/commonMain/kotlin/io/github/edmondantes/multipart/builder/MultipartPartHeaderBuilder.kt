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
package io.github.edmondantes.multipart.builder

import io.github.edmondantes.multipart.MultipartPartHeader

/**
 * This class helps to construct [MultipartPartHeader]
 */
public open class MultipartPartHeaderBuilder {

    /**
     * Header's value
     */
    public var value: String? = null

    /**
     * Header's attribute
     *
     * Encoded in alphabetical order
     */
    public val attributes: MutableMap<String, String> = mutableMapOf()

    public constructor()

    public constructor(value: String?, attributes: Map<String, String> = emptyMap()) {
        this.value = value
        this.attributes.putAll(attributes)
    }

    public constructor(from: MultipartPartHeaderBuilder) : this(from.value, from.attributes)

    public constructor(from: MultipartPartHeader) : this(from.value, from.attributes)

    /**
     * Set header's value
     */
    public open fun value(value: String): MultipartPartHeaderBuilder = apply {
        this.value = value
    }

    /**
     * Set header's attribute
     *
     * @param name attribute's name
     * @param value attribute's value
     */
    public open fun attribute(name: String, value: String): MultipartPartHeaderBuilder = apply {
        attributes[name] = value
    }

    /**
     * Construct [MultipartPartBuilder]
     */
    public fun build(): MultipartPartHeader =
        MultipartPartHeader(
            value,
            attributes,
        )
}

/**
 * Create [MultipartPartHeaderBuilder]
 */
public inline fun multipartPartHeaderBuilder(): MultipartPartHeaderBuilder = MultipartPartHeaderBuilder()

/**
 * Construct [MultipartPartHeader] with configuration from [block]
 *
 * @param block configuration for [MultipartPartHeader]
 */
public inline fun multipartPartHeader(block: MultipartPartHeaderBuilder.() -> Unit): MultipartPartHeader =
    multipartPartHeaderBuilder().apply(block).build()
