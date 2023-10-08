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
package io.github.edmondantes.multipart.impl

import io.github.edmondantes.multipart.Multipart
import io.github.edmondantes.multipart.MultipartFormData
import io.github.edmondantes.multipart.MultipartPart
import io.github.edmondantes.multipart.MultipartPartHeader
import io.github.edmondantes.multipart.builder.MultipartFormDataBuilder.Companion.CONTENT_DISPOSITION_HEADER
import io.github.edmondantes.multipart.builder.MultipartPartBuilder
import io.github.edmondantes.multipart.builder.MultipartPartHeaderBuilder
import io.github.edmondantes.multipart.builder.multipart
import io.github.edmondantes.multipart.builder.multipartFormData
import io.github.edmondantes.multipart.builder.multipartPart
import kotlin.math.absoluteValue
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultMultipartEncoderDecoderTest {
    private val encoder = DefaultMultipartEncoderDecoder(DefaultMultipartEncoderDecoderConfiguration)

    @Test
    fun tooShortBoundary() {
        assertFailsWith(IllegalArgumentException::class, "Boundary length should be from 1 to 70") {
            encoder.decode("", ONE_SIMPLE_PART)
        }
    }

    @Test
    fun tooLongBoundary() {
        assertFailsWith(IllegalArgumentException::class, "Boundary length should be from 1 to 70") {
            encoder.decode(boundary + "a", ONE_SIMPLE_PART)
        }
    }

    @Test
    fun boundaryWithNewLine() {
        assertFailsWith(IllegalArgumentException::class, "Boundary should not contains new line character") {
            encoder.decode(DefaultMultipartEncoderDecoderConfiguration.newLine, ONE_SIMPLE_PART)
        }
    }

    @Test
    fun boundaryWithLastWhitespace() {
        assertFailsWith(IllegalArgumentException::class, "Boundary should not ending with white space.") {
            encoder.decode(" ", ONE_SIMPLE_PART)
        }
    }

    @Test
    fun boundaryWithLastTab() {
        assertFailsWith(IllegalArgumentException::class, "Boundary should not ending with white space.") {
            encoder.decode("\t", ONE_SIMPLE_PART)
        }
    }

    @Test
    fun tooMuchTrailers() {
        assertFailsWith(IllegalArgumentException::class, "Multipart must contains 1 trailer") {
            encoder.decode(
                "1", """--1
                |
                |element
                |--1--
                |
                |--1--
                |
            """.trimMargin().encodeToByteArray()
            )
        }
    }

    @Test
    fun encodeEmpty() {
        assertTrue { encoder.encode(boundary, multipart { }).isEmpty() }
    }

    @Test
    fun decodeEmpty() {
        val multipart = encoder.decode(boundary, byteArrayOf())

        assertNull(multipart.epilogue)
        assertNull(multipart.preamble)
        assertEquals(0, multipart.parts.size)
    }

    @Test
    fun encodeOneSimplePart() {
        val actual = encoder.encode(
            boundary,
            multipart {
                add(MultipartPart(ONE_SIMPLE_PART_FIRST_BYTES))
            },
        )

        assertContentEquals(ONE_SIMPLE_PART, actual)
    }

    @Test
    fun decodeOneSimplePart() {
        val multipart = encoder.decode(boundary, ONE_SIMPLE_PART)

        assertNull(multipart.epilogue)
        assertNull(multipart.preamble)
        assertEquals(1, multipart.parts.size)
        multipart.parts[0].apply {
            assertEquals(0, headers.size)
            assertContentEquals(ONE_SIMPLE_PART_FIRST_BYTES, body)
        }
    }

    @Test
    fun encodeOneSimplePartWithPreamble() {
        val actual = encoder.encode(
            boundary,
            Multipart(
                listOf(MultipartPart(ONE_SIMPLE_PART_FIRST_BYTES)),
                ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE_BYTES,
            ),
        )

        assertContentEquals(ONE_SIMPLE_PART_WITH_PREAMBLE, actual)
    }

    @Test
    fun decodeOneSimplePartWithPreamble() {
        val multipart = encoder.decode(boundary, ONE_SIMPLE_PART_WITH_PREAMBLE)

        assertNull(multipart.epilogue)
        assertContentEquals(ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE_BYTES, multipart.preamble)
        assertEquals(1, multipart.parts.size)
        multipart.parts[0].apply {
            assertEquals(0, headers.size)
            assertContentEquals(ONE_SIMPLE_PART_FIRST_BYTES, body)
        }
    }

    @Test
    fun encodeOneSimplePartWithEpilogue() {
        val actual = encoder.encode(
            boundary,
            Multipart(
                listOf(MultipartPart(ONE_SIMPLE_PART_FIRST_BYTES)),
                null,
                ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE_BYTES,
            ),
        )

        assertContentEquals(ONE_SIMPLE_PART_WITH_EPILOGUE, actual)
    }

    @Test
    fun decodeOneSimplePartWithEpilogue() {
        val multipart = encoder.decode(boundary, ONE_SIMPLE_PART_WITH_EPILOGUE)

        assertContentEquals(ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE_BYTES, multipart.epilogue)
        assertNull(multipart.preamble)
        assertEquals(1, multipart.parts.size)
        multipart.parts[0].apply {
            assertEquals(0, headers.size)
            assertContentEquals(ONE_SIMPLE_PART_FIRST_BYTES, body)
        }
    }

    @Test
    fun encodeOneComplexPart() {
        val actual = encoder.encode(
            boundary,
            multipart {
                add(
                    MultipartPart(
                        ONE_COMPLEX_PART_FIRST_BYTES,
                        mapOf(
                            ONE_COMPLEX_PART_FIRST_HEADERS_FIRST to MultipartPartHeader(
                                ONE_COMPLEX_PART_FIRST_HEADERS_FIRST_VALUE,
                            ),
                            ONE_COMPLEX_PART_FIRST_HEADERS_SECOND to MultipartPartHeader(
                                ONE_COMPLEX_PART_FIRST_HEADERS_SECOND_VALUE,
                            ),
                        ),
                    ),
                )
            },
        )

        assertContentEquals(ONE_COMPLEX_PART, actual)
    }

    @Test
    fun decodeOneComplexPart() {
        val multipart = encoder.decode(boundary, ONE_COMPLEX_PART)

        assertNull(multipart.epilogue)
        assertNull(multipart.preamble)
        assertEquals(1, multipart.parts.size)
        multipart.parts[0].apply {
            assertEquals(2, headers.size)
            assertEquals(
                ONE_COMPLEX_PART_FIRST_HEADERS_FIRST_VALUE,
                headers[ONE_COMPLEX_PART_FIRST_HEADERS_FIRST]?.value,
            )
            assertEquals(
                ONE_COMPLEX_PART_FIRST_HEADERS_SECOND_VALUE,
                headers[ONE_COMPLEX_PART_FIRST_HEADERS_SECOND]?.value,
            )
            assertContentEquals(ONE_COMPLEX_PART_FIRST_BYTES, body)
        }
    }

    @Test
    fun encodeFullMultipart() {
        val forEncode = multipart {
            preamble = SOME_PARTS_PREAMBLE_BYTES
            epilogue = SOME_PARTS_EPILOGUE_BYTES

            add(MultipartPartBuilder(SOME_PARTS_PARTS_BYTES[0]))
            add(
                MultipartPartBuilder(
                    SOME_PARTS_PARTS_BYTES[1],
                    mapOf(
                        SOME_PARTS_SECOND_HEADER_FIRST to MultipartPartHeaderBuilder(
                            SOME_PARTS_SECOND_HEADER_FIRST_VALUE,
                        ),
                        SOME_PARTS_SECOND_HEADER_SECOND to MultipartPartHeaderBuilder(
                            SOME_PARTS_SECOND_HEADER_SECOND_VALUE,
                        ),
                    ),
                ),
            )
            add(
                MultipartPartBuilder(
                    SOME_PARTS_PARTS_BYTES[2],
                ),
            )
            add(
                MultipartPartBuilder(
                    SOME_PARTS_PARTS_BYTES[3],
                    mapOf(
                        SOME_PARTS_FOURTH_HEADER_FIRST to MultipartPartHeaderBuilder(
                            SOME_PARTS_FOURTH_HEADER_FIRST_VALUE,
                        ),
                    ),
                ),
            )
        }

        val actual = encoder.encode(boundary, forEncode)
        assertContentEquals(SOME_PARTS, actual)
    }

    @Test
    fun decodeFullMultipart() {
        val multipart = encoder.decode(boundary, SOME_PARTS)

        assertContentEquals(SOME_PARTS_PREAMBLE_BYTES, multipart.preamble)
        assertContentEquals(SOME_PARTS_EPILOGUE_BYTES, multipart.epilogue)
        assertEquals(4, multipart.parts.size)

        assertContentEquals(SOME_PARTS_PARTS_BYTES[0], multipart.parts[0].body)
        assertContentEquals(SOME_PARTS_PARTS_BYTES[1], multipart.parts[1].body)
        assertContentEquals(SOME_PARTS_PARTS_BYTES[2], multipart.parts[2].body)
        assertContentEquals(SOME_PARTS_PARTS_BYTES[3], multipart.parts[3].body)

        multipart.parts[1].apply {
            assertEquals(2, headers.size)
            assertEquals(SOME_PARTS_SECOND_HEADER_FIRST_VALUE, headers[SOME_PARTS_SECOND_HEADER_FIRST]?.value)
            assertEquals(SOME_PARTS_SECOND_HEADER_SECOND_VALUE, headers[SOME_PARTS_SECOND_HEADER_SECOND]?.value)
        }

        multipart.parts[3].apply {
            assertEquals(1, headers.size)
            assertEquals(SOME_PARTS_FOURTH_HEADER_FIRST_VALUE, headers[SOME_PARTS_FOURTH_HEADER_FIRST]?.value)
        }
    }

    @Test
    fun encodeMultipartFormData() {
        val forEncode = multipartFormData {
            add(
                "Field1",
                multipartPart {
                    body = MULTIPART_FORM_DATA_PARTS_BYTES["Field1"]
                },
            )
            add(
                "id",
                multipartPart {
                    body = MULTIPART_FORM_DATA_PARTS_BYTES["id"]
                },
            )
        }

        val actual = encoder.encode(boundary, forEncode)
        assertContentEquals(MULTIPART_FORM_DATA, actual)
    }

    @Test
    fun decodeMultipartFormData() {
        val multipart = encoder.decode(boundary, MULTIPART_FORM_DATA)
        val formData = MultipartFormData.constructFromMultipart(multipart)

        assertNull(formData.preamble)
        assertNull(formData.epilogue)
        assertEquals(2, formData.namedParts.size)

        val part0 = formData.namedParts["Field1"]
        assertNotNull(part0)
        assertEquals(1, part0.size)
        assertContentEquals(MULTIPART_FORM_DATA_PARTS_BYTES["Field1"], part0[0].body)

        val part1 = formData.namedParts["id"]
        assertNotNull(part1)
        assertEquals(1, part1.size)

        assertContentEquals(MULTIPART_FORM_DATA_PARTS_BYTES["id"], part1[0].body)
    }

    private companion object {
        val boundary =
            generateSequence { Random.nextInt().absoluteValue }.take(70)
                .map { ((it % ('z'.code - 'a'.code + 1)) + 'a'.code).toChar() }
                .joinToString(separator = "")

        const val ONE_SIMPLE_PART_FIRST = "Test"
        val ONE_SIMPLE_PART_FIRST_BYTES = ONE_SIMPLE_PART_FIRST.encodeToByteArray()
        val ONE_SIMPLE_PART = """--$boundary
            |
            |$ONE_SIMPLE_PART_FIRST
            |--$boundary--
            |
        """.trimMargin().encodeToByteArray()

        const val ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE = "preamble"
        val ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE_BYTES = ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE.encodeToByteArray()
        val ONE_SIMPLE_PART_WITH_PREAMBLE = """$ONE_SIMPLE_PART_WITH_PREAMBLE_PREAMBLE
            |--$boundary
            |
            |$ONE_SIMPLE_PART_FIRST
            |--$boundary--
            |
        """.trimMargin().encodeToByteArray()

        const val ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE = "epilogue"
        val ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE_BYTES = ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE.encodeToByteArray()
        val ONE_SIMPLE_PART_WITH_EPILOGUE = """--$boundary
            |
            |$ONE_SIMPLE_PART_FIRST
            |--$boundary--
            |$ONE_SIMPLE_PART_WITH_EPILOGUE_EPILOGUE
        """.trimMargin().encodeToByteArray()

        const val ONE_COMPLEX_PART_FIRST = "Test"
        val ONE_COMPLEX_PART_FIRST_BYTES = ONE_COMPLEX_PART_FIRST.encodeToByteArray()
        const val ONE_COMPLEX_PART_FIRST_HEADERS_FIRST = "Content-Type"
        const val ONE_COMPLEX_PART_FIRST_HEADERS_FIRST_VALUE = "plain/text"
        const val ONE_COMPLEX_PART_FIRST_HEADERS_SECOND = "FileName"
        const val ONE_COMPLEX_PART_FIRST_HEADERS_SECOND_VALUE = "Test.txt"
        val ONE_COMPLEX_PART = """--$boundary
            |$ONE_COMPLEX_PART_FIRST_HEADERS_FIRST: $ONE_COMPLEX_PART_FIRST_HEADERS_FIRST_VALUE
            |$ONE_COMPLEX_PART_FIRST_HEADERS_SECOND: $ONE_COMPLEX_PART_FIRST_HEADERS_SECOND_VALUE
            |
            |$ONE_COMPLEX_PART_FIRST
            |--$boundary--
            |
        """.trimMargin().encodeToByteArray()

        const val SOME_PARTS_PREAMBLE = "Some Parts Preamble"
        val SOME_PARTS_PREAMBLE_BYTES = SOME_PARTS_PREAMBLE.encodeToByteArray()
        const val SOME_PARTS_EPILOGUE = "Some Parts Epilogue"
        val SOME_PARTS_EPILOGUE_BYTES = SOME_PARTS_EPILOGUE.encodeToByteArray()
        val SOME_PARTS_PARTS = listOf("Element0", "Element1", "123565", "{\"requestId\": 134}\n")
        val SOME_PARTS_PARTS_BYTES = SOME_PARTS_PARTS.map { it.encodeToByteArray() }
        const val SOME_PARTS_SECOND_HEADER_FIRST = "Content-Type"
        const val SOME_PARTS_SECOND_HEADER_FIRST_VALUE = "plain/text"
        const val SOME_PARTS_SECOND_HEADER_SECOND = "FileName"
        const val SOME_PARTS_SECOND_HEADER_SECOND_VALUE = "FileName"
        const val SOME_PARTS_FOURTH_HEADER_FIRST = "Content-Type"
        const val SOME_PARTS_FOURTH_HEADER_FIRST_VALUE = "application/json"

        val SOME_PARTS = """$SOME_PARTS_PREAMBLE
            |--$boundary
            |
            |${SOME_PARTS_PARTS[0]}
            |--$boundary
            |$SOME_PARTS_SECOND_HEADER_FIRST: $SOME_PARTS_SECOND_HEADER_FIRST_VALUE
            |$SOME_PARTS_SECOND_HEADER_SECOND: $SOME_PARTS_SECOND_HEADER_SECOND_VALUE
            |
            |${SOME_PARTS_PARTS[1]}
            |--$boundary
            |
            |${SOME_PARTS_PARTS[2]}
            |--$boundary
            |$SOME_PARTS_FOURTH_HEADER_FIRST: $SOME_PARTS_FOURTH_HEADER_FIRST_VALUE
            |
            |${SOME_PARTS_PARTS[3].replace("\n", "\n|")}
            |--$boundary--
            |$SOME_PARTS_EPILOGUE
        """.trimMargin().encodeToByteArray()

        val MULTIPART_FORM_DATA_PARTS = mapOf("Field1" to "Element1", "id" to "123567")
        val MULTIPART_FORM_DATA_PARTS_BYTES = MULTIPART_FORM_DATA_PARTS.mapValues { it.value.encodeToByteArray() }

        val MULTIPART_FORM_DATA = """--$boundary
            |$CONTENT_DISPOSITION_HEADER: form-data; name="Field1"
            |
            |${MULTIPART_FORM_DATA_PARTS["Field1"]}
            |--$boundary
            |$CONTENT_DISPOSITION_HEADER: form-data; name="id"
            |
            |${MULTIPART_FORM_DATA_PARTS["id"]}
            |--$boundary--
            |
        """.trimMargin().encodeToByteArray()
    }
}
