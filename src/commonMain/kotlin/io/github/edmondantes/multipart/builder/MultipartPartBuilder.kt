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

import io.github.edmondantes.multipart.MultipartPart

/**
 * This class helps to construct [MultipartPart]
 */
public open class MultipartPartBuilder {

    /**
     * Part's body
     */
    public var body: ByteArray? = null

    /**
     * Part's headers
     *
     * Encoded in alphabetical order
     */
    public val headers: MutableMap<String, MultipartPartHeaderBuilder> = HashMap()

    public constructor()

    public constructor(body: ByteArray?, headers: Map<String, MultipartPartHeaderBuilder> = emptyMap()) {
        this.body = body
        headers.forEach { (key, value) ->
            this.headers[key] = MultipartPartHeaderBuilder(value)
        }
    }

    public constructor(from: MultipartPartBuilder) : this(from.body, from.headers)

    public constructor(from: MultipartPart) : this(
        from.body,
        from.headers.mapValues { (_, value) -> MultipartPartHeaderBuilder(value) },
    )

    /**
     * Set part's body
     */
    public open fun body(bytes: ByteArray?): MultipartPartBuilder = apply {
        this.body = bytes
    }

    /**
     * Set part's header
     *
     * @param name header's name
     * @param block configuration for header
     */
    public open fun header(name: String, block: MultipartPartHeaderBuilder.() -> Unit): MultipartPartBuilder = apply {
        headers.getOrPut(name) { MultipartPartHeaderBuilder() }.apply(block)
    }

    /**
     * Construct [MultipartPart]
     */
    public open fun build(): MultipartPart =
        MultipartPart(
            requireNotNull(body) { "'body' can not be null" },
            headers.mapValues { it.value.build() },
        )
}

/**
 * Create [MultipartPartBuilder]
 */
public inline fun multipartPartBuilder(): MultipartPartBuilder = MultipartPartBuilder()

/**
 * Construct [MultipartPart] with configuration from [block]
 *
 * @param block configuration for [MultipartPart]
 */
public inline fun multipartPart(block: MultipartPartBuilder.() -> Unit): MultipartPart =
    multipartPartBuilder().apply(block).build()
