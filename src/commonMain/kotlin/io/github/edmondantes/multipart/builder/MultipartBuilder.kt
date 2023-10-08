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

import io.github.edmondantes.multipart.Multipart
import io.github.edmondantes.multipart.MultipartPart

/**
 * This class helps to construct [Multipart]
 */
public open class MultipartBuilder {

    protected val builtParts: MutableList<MultipartPart> = mutableListOf()
    protected val parts: MutableList<MultipartPartBuilder> = mutableListOf()

    /**
     * Preamble before body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public var preamble: ByteArray? = null

    /**
     * Epilogue after body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public var epilogue: ByteArray? = null

    public constructor()

    public constructor(
        builtParts: List<MultipartPart>,
        parts: List<MultipartPartBuilder>,
        preamble: ByteArray? = null,
        epilogue: ByteArray? = null,
    ) {
        this.builtParts.addAll(builtParts)
        this.parts.addAll(parts.map { MultipartPartBuilder(it) })
        this.preamble = preamble
        this.epilogue = epilogue
    }

    public constructor(
        parts: List<MultipartPartBuilder>,
        preamble: ByteArray? = null,
        epilogue: ByteArray? = null,
    ) : this(builtParts = emptyList(), parts = parts, preamble = preamble, epilogue = epilogue)

    public constructor(from: MultipartBuilder) : this(
        parts = from.parts,
        preamble = from.preamble,
        epilogue = from.epilogue,
    )

    public constructor(from: Multipart) : this(
        from.parts.map { MultipartPartBuilder(it) },
        from.preamble,
        from.epilogue,
    )

    /**
     * Set preamble before body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public open fun preamble(bytes: ByteArray): MultipartBuilder = apply {
        preamble = bytes
    }

    /**
     * Set epilogue after body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    public open fun epilogue(bytes: ByteArray): MultipartBuilder = apply {
        epilogue = bytes
    }

    /**
     * Add a part to builder
     */
    public open fun add(part: MultipartPart): MultipartBuilder = apply {
        builtParts.add(part)
    }

    /**
     * Add a part to builder
     */
    public open fun add(part: MultipartPartBuilder): MultipartBuilder = apply {
        parts.add(part)
    }

    /**
     * Construct [Multipart]
     */
    public open fun build(): Multipart = Multipart(
        builtParts + parts.map { it.build() },
        preamble,
        epilogue,
    )
}

/**
 * Create [MultipartBuilder]
 */
public inline fun multipartBuilder(): MultipartBuilder = MultipartBuilder()

/**
 * Construct [Multipart] with configuration from [block]
 *
 * @param block configuration for [Multipart]
 */
public inline fun multipart(block: MultipartBuilder.() -> Unit): Multipart =
    multipartBuilder().apply(block).build()
