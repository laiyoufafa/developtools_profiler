/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
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

package ohos.devtools.views.layout.chartview.utils;

import ohos.devtools.datasources.utils.profilerlog.ProfilerLogManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * check Digit Document
 *
 * @since 2021/11/15 15:32
 */
public class DigitCheckDocument extends PlainDocument {
    private static final long serialVersionUID = -7649548958014983841L;
    private static final Logger LOGGER = LogManager.getLogger(DigitCheckDocument.class);

    /**
     * limited Length
     */
    private final int limitedLength;

    /**
     * Digit Document
     *
     * @param limitedLength limitedLength
     */
    public DigitCheckDocument(int limitedLength) {
        this.limitedLength = limitedLength;
    }

    /**
     * insertString
     *
     * @param offset offset
     * @param insertString insertString
     * @param attributeSet attributeSet
     */
    @Override
    public void insertString(int offset, String insertString, AttributeSet attributeSet) {
        try {
            if (insertString == null) {
                return;
            }
            // Limit character length
            if ((this.getLength() + insertString.length()) <= limitedLength) {
                char[] charArray = insertString.toCharArray();
                int length = 0;
                for (int index = 0; index < charArray.length; index++) {
                    if (Character.isDigit(charArray[index])) {
                        charArray[length++] = charArray[index];
                    }
                }
                super.insertString(offset, new String(charArray, 0, length), attributeSet);
            }
        } catch (BadLocationException exception) {
            if (ProfilerLogManager.isErrorEnabled()) {
                LOGGER.error("limit character length error: " + insertString);
            }
        }
    }
}
