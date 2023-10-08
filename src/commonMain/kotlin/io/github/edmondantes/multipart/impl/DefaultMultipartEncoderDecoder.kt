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
import io.github.edmondantes.multipart.MultipartDecoder
import io.github.edmondantes.multipart.MultipartEncoder
import io.github.edmondantes.multipart.builder.MultipartPartBuilder
import io.github.edmondantes.multipart.builder.MultipartPartHeaderBuilder

/**
 * Default implementation of [MultipartEncoder] and [MultipartDecoder]
 */
public class DefaultMultipartEncoderDecoder(
    private val config: DefaultMultipartEncoderDecoderConfiguration,
) : MultipartEncoder, MultipartDecoder {

    private val stringEncoder: (String) -> ByteArray = config.stringEncoder
    private val stringDecoder: (ByteArray) -> String = config.stringDecoder

    private val twoDashesBytes = stringEncoder("--")
    private val newLineBytes = stringEncoder(config.newLine)
    private val headerKeyValueBounderBytes = stringEncoder(HEADER_KEY_VALUE_BOUNDER)

    override fun encode(boundary: String, multipart: Multipart): ByteArray {
        if (multipart.parts.isEmpty()) {
            return byteArrayOf()
        }

        val boundaryBytes = boundaryToBytes(boundary)

        var size =
            multipart.preamble.let { it?.size?.plus(newLineBytes.size) ?: 0 } +
                multipart.epilogue.let { it?.size ?: 0 } +
                trailerSize(boundaryBytes)

        val parts = multipart.parts.map {
            size += boundarySize(boundaryBytes)
            size += newLineBytes.size + it.body.size + newLineBytes.size

            it.headers
                .map { (key, value) ->
                    val valueBuilder = StringBuilder()
                    value.value?.also(valueBuilder::append)
                    value.attributes
                        .map { it }
                        .sortedBy { it.key }
                        .forEach { (key, value) ->
                            valueBuilder.append("; ")
                            valueBuilder.append(key)
                            valueBuilder.append('=')
                            valueBuilder.append(value)
                        }

                    key to valueBuilder.toString()
                }
                .sortedBy { it.first }
                .map { (key, value) ->
                    (stringEncoder(key) to stringEncoder(value)).also { (keyBytes, valueBytes) ->
                        size += keyBytes.size + headerKeyValueBounderBytes.size + valueBytes.size + newLineBytes.size
                    }
                } to it.body
        }

        val result = ByteArray(size)
        var index = 0

        multipart.preamble?.also {
            index = result.fill(it, index)
            index = result.fill(newLineBytes, index)
        }

        parts.forEach {
            index = result.fill(twoDashesBytes, index)
            index = result.fill(boundaryBytes, index)
            index = result.fill(newLineBytes, index)

            it.first.forEach { (key, value) ->
                index = result.fill(key, index)
                index = result.fill(headerKeyValueBounderBytes, index)
                index = result.fill(value, index)
                index = result.fill(newLineBytes, index)
            }

            index = result.fill(newLineBytes, index)

            index = result.fill(it.second, index)
            index = result.fill(newLineBytes, index)
        }

        index = result.fill(twoDashesBytes, index)
        index = result.fill(boundaryBytes, index)
        index = result.fill(twoDashesBytes, index)
        index = result.fill(newLineBytes, index)

        multipart.epilogue?.also {
            result.fill(it, index)
        }

        return result
    }

    private fun boundarySize(boundary: ByteArray): Int =
        twoDashesBytes.size + boundary.size + newLineBytes.size

    private fun trailerSize(boundary: ByteArray): Int =
        twoDashesBytes.size + boundary.size + twoDashesBytes.size + newLineBytes.size

    override fun decode(boundary: String, array: ByteArray): Multipart {
        if (array.isEmpty()) {
            return Multipart.Empty
        }

        val boundaryBytes = boundaryToBytes(boundary)
        val multipartData = array.split(0, array.lastIndex, twoDashesBytes, boundaryBytes, twoDashesBytes, newLineBytes)
        if (config.shouldCheckTrailerCounts) {
            require(multipartData.size <= 2) { "Multipart must contains 1 trailer" }
        }

        if (multipartData.size < 2) {
            return Multipart(emptyList(), null, null)
        }

        val partIndex = array.split(
            multipartData[0].first,
            multipartData[0].last,
            twoDashesBytes,
            boundaryBytes,
        )

        val preambleRange = if (partIndex.isNotEmpty() && partIndex[0].size() > 0) {
            if (array.match(partIndex[0].last - newLineBytes.size + 1, newLineBytes)) {
                partIndex[0].first..(partIndex[0].last - newLineBytes.size)
            } else {
                partIndex[0]
            }
        } else {
            null
        }

        val epilogueRange = if (multipartData[1].size() > 0) multipartData[1] else null

        val parts = partIndex.subList(1, partIndex.size).map {
            val builder = MultipartPartBuilder()
            val index = array.search(it.first, it.last, newLineBytes, newLineBytes)

            array.split(it.first + newLineBytes.size, index - 1, newLineBytes)
                .map { headersIndices -> array.copyOfRange(headersIndices) }
                .map { bytes -> stringDecoder(bytes) }
                .forEach { header ->
                    val delimiterIndex = header.indexOf(':')

                    val headerValueParts = header.substring(delimiterIndex + 1).split(';')
                    if (headerValueParts.isNotEmpty()) {
                        val headerName = header.substring(0, delimiterIndex).trim()
                        builder.headers[headerName] = MultipartPartHeaderBuilder().apply {
                            this.value = headerValueParts[0].trim()
                            for (i in 1..<headerValueParts.size) {
                                val attributeDelimiter = headerValueParts[i].indexOf('=')
                                if (attributeDelimiter > -1) {
                                    attributes[headerValueParts[i].substring(0, attributeDelimiter).trim()] =
                                        headerValueParts[i].substring(attributeDelimiter + 1).trim()
                                } else {
                                    attributes[headerValueParts[i].trim()] = ""
                                }
                            }
                        }
                    }
                }

            builder.body = array.copyOfRange(index + newLineBytes.size * 2, it.last - newLineBytes.size + 1)
            builder.build()
        }

        return Multipart(
            parts,
            preambleRange?.let { array.copyOfRange(it) },
            epilogueRange?.let { array.copyOfRange(it) },
        )
    }

    private fun ByteArray.match(start: Int, vararg sources: ByteArray): Boolean {
        var i = start

        for (source in sources) {
            if (i + source.size > size) {
                return false
            }

            var j = 0
            while (i < size && j < source.size) {
                if (this[i++] != source[j++]) {
                    return false
                }
            }
        }

        return true
    }

    private fun ByteArray.search(first: Int = 0, last: Int = lastIndex, vararg sources: ByteArray): Int {
        var i = first

        while (i <= last) {
            if (match(i, *sources)) {
                return i
            }
            i++
        }

        return -1
    }

    private fun ByteArray.split(first: Int = 0, last: Int = lastIndex, vararg sources: ByteArray): List<IntRange> {
        val result = mutableListOf<IntRange>()
        val length = sources.sumOf { it.size }
        var start = first
        var i = first

        while (i <= last) {
            if (match(i, *sources)) {
                result.add(start..<i)
                start = i + length
                i = start
            } else {
                i++
            }
        }

        if (start - 1 <= last) {
            result.add(start..last)
        }

        return result
    }

    @Suppress("DEPRECATION")
    private fun ByteArray.copyOfRange(range: IntRange): ByteArray = copyOfRange(range.first, range.endExclusive)

    private fun ByteArray.fill(source: ByteArray, index: Int): Int {
        source.copyInto(this, index)
        return index + source.size
    }

    private fun IntRange.size(): Int = last - start + 1

    private fun boundaryToBytes(boundary: String): ByteArray {
        if (config.shouldCheckBoundary) {
            require(boundary.length in 1..70) { "Boundary length should be from 1 to 70" }
            require(!boundary.contains(config.newLine)) { "Boundary should not contains new line character" }
            require(!boundary.last().isWhitespace()) { "Boundary should not ending with white space." }
        }

        return stringEncoder(boundary)
    }

    private companion object {
        const val HEADER_KEY_VALUE_BOUNDER = ": "
    }
}
