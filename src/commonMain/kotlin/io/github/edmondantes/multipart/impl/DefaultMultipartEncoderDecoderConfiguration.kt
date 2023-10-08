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

public open class DefaultMultipartEncoderDecoderConfiguration(
    /**
     * Should [DefaultMultipartEncoderDecoder] check boundary is valid
     *
     * Boundary requirements:
     * 1. Length from 1 to 70
     * 2. It should not have new line character(s)
     * 3. It should not end with whitespace
     */
    public val shouldCheckBoundary: Boolean,
    /**
     * Should [DefaultMultipartEncoderDecoder] check trailer counts on decoding
     *
     * If true, [DefaultMultipartEncoderDecoder] will ignore if trailers more than 1
     */
    public val shouldCheckTrailerCounts: Boolean,
    /**
     * Function that transform [String] to [ByteArray]
     */
    public val stringEncoder: (String) -> ByteArray,
    /**
     * Function that transform [ByteArray] to [String]
     */
    public val stringDecoder: (ByteArray) -> String,
    /**
     * New line characters (default: \n)
     */
    public val newLine: String = "\n",
) {
    /**
     * Default configuration
     */
    public companion object Default :
        DefaultMultipartEncoderDecoderConfiguration(
            true,
            true,
            String::encodeToByteArray,
            ByteArray::decodeToString,
            "\n",
        )
}
