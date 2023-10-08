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
import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.MultipartPart

/**
 * This class helps to construct [MultipartFormData]
 */
public open class MultipartFormDataBuilder : MultipartBuilder {
    protected val builtNamedParts: MutableMap<String, MutableList<MultipartPart>> = mutableMapOf()
    protected val namedParts: MutableMap<String, MutableList<MultipartPartBuilder>> = mutableMapOf()

    public constructor() : super()

    public constructor(
        builtNamedParts: Map<String, List<MultipartPart>>,
        namedParts: Map<String, List<MultipartPartBuilder>>,
        builtParts: List<MultipartPart> = emptyList(),
        parts: List<MultipartPartBuilder> = emptyList(),
        preamble: ByteArray? = null,
        epilogue: ByteArray? = null,
    ) : super(builtParts, parts, preamble, epilogue) {
        builtNamedParts.forEach { (key, value) ->
            this.builtNamedParts.getOrPut(key) { mutableListOf() }.addAll(value)
        }

        namedParts.forEach { (key, value) ->
            this.namedParts.getOrPut(key) { mutableListOf() }.addAll(value)
        }
    }

    public constructor(from: MultipartFormDataBuilder) : this(
        from.builtNamedParts,
        from.namedParts,
        from.builtParts,
        from.parts,
        from.preamble,
        from.epilogue,
    )

    public constructor(from: MultipartFormData) : this(
        from.namedParts,
        emptyMap(),
        from.parts,
        emptyList(),
        from.preamble,
        from.epilogue,
    )

    public constructor(from: MultipartBuilder) : super(from)

    public constructor(from: Multipart) : super(from)

    /**
     * Set preamble before body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    override fun preamble(bytes: ByteArray): MultipartFormDataBuilder = apply {
        super.preamble(bytes)
    }

    /**
     * Set epilogue after body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    override fun epilogue(bytes: ByteArray): MultipartFormDataBuilder = apply {
        super.epilogue(bytes)
    }

    /**
     * Add unnamed part to builder
     */
    override fun add(part: MultipartPart): MultipartFormDataBuilder = apply {
        super.add(part)
    }

    /**
     * Add unnamed part to builder
     */
    override fun add(part: MultipartPartBuilder): MultipartFormDataBuilder = apply {
        super.add(part)
    }

    /**
     * Add a named part to builder
     */
    public open fun add(name: String, part: MultipartPart): MultipartFormDataBuilder = apply {
        builtNamedParts.getOrPut(name) { mutableListOf() }.add(part)
    }

    /**
     * Add a named part to builder
     */
    public open fun add(name: String, partBuilder: MultipartPartBuilder): MultipartFormDataBuilder = apply {
        namedParts.getOrPut(name) { mutableListOf() }.add(partBuilder)
    }

    /**
     * Edit named part
     *
     * @param name part's name
     * @param block function for change part
     */
    public open fun edit(name: String, block: (MultipartPartBuilder) -> Unit): MultipartFormDataBuilder = apply {
        val parts = namedParts.getOrPut(name) { mutableListOf() }

        builtNamedParts.remove(name)?.forEach { element ->
            parts.add(element.toBuilder())
        }

        parts.forEach(block)
    }

    /**
     * Construct [MultipartFormData]
     */
    public override fun build(): MultipartFormData {
        namedParts.forEach { (name, list) ->
            list.forEach { part ->
                part.header(CONTENT_DISPOSITION_HEADER) {
                    value = "form-data"
                    attribute(NAME_ATTRIBUTE, "\"$name\"")
                }
            }
        }

        return MultipartFormData(
            builtNamedParts + namedParts.mapValues { it.value.map { it.build() } },
            builtParts + parts.map { it.build() },
            preamble,
            epilogue,
        )
    }

    public companion object {
        public const val CONTENT_DISPOSITION_HEADER: String = "Content-Disposition"
        public const val NAME_ATTRIBUTE: String = "name"
    }
}

/**
 * Create [MultipartFormDataBuilder]
 */
public inline fun multipartFormDataBuilder(): MultipartFormDataBuilder = MultipartFormDataBuilder()

/**
 * Construct [MultipartFormData] with configuration from [block]
 *
 * @param block configuration for [MultipartFormData]
 */
public inline fun multipartFormData(block: MultipartFormDataBuilder.() -> Unit): MultipartFormData =
    multipartFormDataBuilder().apply(block).build()
