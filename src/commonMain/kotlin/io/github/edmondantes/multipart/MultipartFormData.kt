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

import io.github.edmondantes.multipart.builder.MultipartFormDataBuilder
import io.github.edmondantes.multipart.builder.multipartFormData

/**
 * This class describe body of _multipart/form-data_ content type
 */
public open class MultipartFormData(
    /**
     * Body parts which have names
     */
    public open val namedParts: Map<String, List<MultipartPart>>,
    /**
     * Body parts which doesn't have names
     */
    notNamedElements: List<MultipartPart> = emptyList(),
    /**
     * Preamble before body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    preamble: ByteArray? = null,
    /**
     * Epilogue after body parts.
     *
     * _This will be ignored most of multipart parsers_
     */
    epilogue: ByteArray? = null,
) : Multipart(notNamedElements, preamble, epilogue) {

    override val parts: List<MultipartPart>
        get() = super.parts + flatNamedParts(namedParts)

    /**
     * Create a [MultipartFormDataBuilder] from current [MultipartFormData]
     */
    public override fun toBuilder(): MultipartFormDataBuilder = MultipartFormDataBuilder(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MultipartFormData) return false
        if (!super.equals(other)) return false

        if (namedParts != other.namedParts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + namedParts.hashCode()
        return result
    }

    public companion object {
        public val Empty: MultipartFormData = MultipartFormData(emptyMap())

        public fun constructFromMultipart(multipart: Multipart): MultipartFormData {
            if (multipart is MultipartFormData) {
                return multipart
            }

            return multipartFormData {
                preamble = multipart.preamble
                epilogue = multipart.epilogue

                multipart.parts.forEach { part ->
                    val name = part.headers[MultipartFormDataBuilder.CONTENT_DISPOSITION_HEADER]?.attributes
                        ?.get(MultipartFormDataBuilder.NAME_ATTRIBUTE)
                        ?.trim('"')
                    if (name != null) {
                        add(name, part)
                    } else {
                        add(part)
                    }
                }
            }
        }

        private fun flatNamedParts(namedParts: Map<String, List<MultipartPart>>): List<MultipartPart> =
            namedParts.flatMap { (name, list) ->
                list.map { part ->
                    part.toBuilder().header(MultipartFormDataBuilder.CONTENT_DISPOSITION_HEADER) {
                        value = "form-data"
                        attribute(MultipartFormDataBuilder.NAME_ATTRIBUTE, "\"$name\"")
                    }.build()
                }
            }
    }
}
