/*
 * This file is part of the OWL API.
 *
 * The contents of this file are subject to the LGPL License, Version 3.0.
 *
 * Copyright (C) 2011, The University of Manchester
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 * Alternatively, the contents of this file may be used under the terms of the Apache License, Version 2.0
 * in which case, the provisions of the Apache License Version 2.0 are applicable instead of those above.
 *
 * Copyright 2011, The University of Manchester
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package org.semanticweb.owlapi.io;

/**
 * Author: Matthew Horridge<br>
 * The University of Manchester<br>
 * Bio-Health Informatics Group<br>
 * Date: 22/09/2011
 * <br>
 * This class contains various methods for checking QNames, NCNames etc.
 * The implementation is based on the <a href="http://www.w3.org/TR/xml-names/">W3C namespaces in XML specification</a>.
 * @since 3.3.0
 */
@SuppressWarnings("javadoc")
public class XMLUtils {

    public static final String LT = "&lt;";

    public static final String GT = "&gt;";

    public static final String QUOT = "&quot;";

    public static final String AMP = "&amp;";

    public static final String APOS = "&apos;";

    // For some point in the future
    public static final String OWL_PROCESSING_INSTRUCTION_NAME = "owl";


    /**
     * Determines if a character is an XML name start character.
     * @param codePoint The code point of the character to be tested.  For UTF-16 characters the code point corresponds
     * to the value of the char that represents the character.
     * @return <code>true</code> if <code>codePoint</code> is an XML name start character, otherwise <code>false</code>
     */
    public static boolean isXMLNameStartCharacter(int codePoint) {
        return codePoint == ':'
                || codePoint >= 'A' && codePoint <= 'Z'
                || codePoint == '_'
                || codePoint >= 'a' && codePoint <= 'z'
                || codePoint >= 0xC0 && codePoint <= 0xD6
                || codePoint >= 0xD8 && codePoint <= 0xF6
                || codePoint >= 0xF8 && codePoint <= 0x2FF
                || codePoint >= 0x370 && codePoint <= 0x37D
                || codePoint >= 0x37F && codePoint <= 0x1FFF
                || codePoint >= 0x200C && codePoint <= 0x200D
                || codePoint >= 0x2070 && codePoint <= 0x218F
                || codePoint >= 0x2C00 && codePoint <= 0x2FEF
                || codePoint >= 0x3001 && codePoint <= 0xD7FF
                || codePoint >= 0xF900 && codePoint <= 0xFDCF
                || codePoint >= 0xFDF0 && codePoint <= 0xFFFD
                || codePoint >= 0x10000 && codePoint <= 0xEFFFF;

    }

    /**
     * Determines if a character is an XML name character.
     * @param codePoint The code point of the character to be tested.  For UTF-8 and UTF-16 characters the code point
     * corresponds to the value of the char that represents the character.
     * @return <code>true</code> if <code>codePoint</code> is an XML name start character, otherwise <code>false</code>
     */
    public static boolean isXMLNameChar(int codePoint) {
        return isXMLNameStartCharacter(codePoint)
                || codePoint == '-'
                || codePoint == '.'
                || codePoint >= '0' && codePoint <= '9'
                || codePoint == 0xB7
                || codePoint >= 0x0300 && codePoint <= 0x036F
                || codePoint >= 0x203F && codePoint <= 0x2040;
    }


    /**
     * Deterimines if a character is an NCName (Non-Colonised Name) start character.
     * @param codePoint The code point of the character to be tested.  For UTF-8 and UTF-16 characters the code point
     * corresponds to the value of the char that represents the character.
     * @return <code>true</code> if <code>codePoint</code> is a NCName start character, otherwise <code>false</code>.
     */
    public static boolean isNCNameStartChar(int codePoint) {
        return codePoint != ':' && isXMLNameStartCharacter(codePoint);
    }

    /**
     * Deterimines if a character is an NCName (Non-Colonised Name) character.
     * @param codePoint The code point of the character to be tested.  For UTF-8 and UTF-16 characters the code point
     * corresponds to the value of the char that represents the character.
     * @return <code>true</code> if <code>codePoint</code> is a NCName character, otherwise <code>false</code>.
     */
    public static boolean isNCNameChar(int codePoint) {
        return codePoint != ':' && isXMLNameChar(codePoint);
    }

    /**
     * Determines if a character sequence is an NCName (Non-Colonised Name).  An NCName is a string which starts with an
     * NCName start character and is followed by zero or more NCName characters.
     * @param s The character sequence to be tested.
     * @return <code>true</code> if <code>s</code> is an NCName, otherwise <code>false</code>.
     */
    public static  boolean isNCName(CharSequence s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        int firstCodePoint = Character.codePointAt(s, 0);
        if(!isNCNameStartChar(firstCodePoint)) {
            return false;
        }
        for(int i = Character.charCount(firstCodePoint); i < s.length(); ) {
            int codePoint = Character.codePointAt(s, i);
            if(!isNCNameChar(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }

    /**
     * Determines if a character sequence is a QName.  A QName is either an NCName (LocalName), or an NCName followed by a colon
     * followed by another NCName (where the first NCName is referred to as the 'Prefix Name' and the second NCName is referred to
     * as the 'Local Name' - i.e. PrefixName:LocalName).
     * @param s The character sequence to be tested.
     * @return <code>true</code> if <code>s</code> is a QName, otherwise <code>false</code>.
     */
    public static boolean isQName(CharSequence s) {
        if (isNullOrEmpty(s)) {
            return false;
        }
        boolean foundColon = false;
        boolean inNCName = false;
        for(int i = 0; i < s.length(); ) {
            int codePoint = Character.codePointAt(s, i);
            if(codePoint == ':') {
                if(foundColon) {
                    return false;
                }
                foundColon = true;
                if(!inNCName) {
                    return false;
                }
                inNCName = false;
            }
            else {
                if(!inNCName) {
                    if(!isXMLNameStartCharacter(codePoint)) {
                        return false;
                    }
                    inNCName = true;
                }
                else {
                    if(!isXMLNameChar(codePoint)) {
                        return false;
                    }
                }
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }

    /**
     * Determines if a character sequence has a suffix that is an NCName.
     * @param s The character sequence.
     * @return <code>true</code> if the character sequence <code>s</code> has a suffix that is an NCName.
     */
    public static boolean hasNCNameSuffix(CharSequence s) {
        return getNCNameSuffixIndex(s) != -1;
    }


    /**
     * Gets the index of the longest NCName that is the suffix of a character sequence.
     * @param s The character sequence.
     * @return The index of the longest suffix of the specified character sequence <code>s</code> that is an NCName, or
     * -1 if the character sequence <code>s</code> does not have a suffix that is an NCName.
     */
    public static int getNCNameSuffixIndex(CharSequence s) {
        int index = -1;
        for(int i = s.length() - 1; i > -1; i--) {
            if (!Character.isLowSurrogate(s.charAt(i))) {
                int codePoint = Character.codePointAt(s, i);
                if(isNCNameStartChar(codePoint)) {
                    index = i;
                }
                if(!isNCNameChar(codePoint)) {
                    break;
                }
            }
        }
        return index;
    }


    /**
     * Get the longest NCName that is a suffix of a character sequence.
     * @param s The character sequence.
     * @return The String which is the longest suffix of the character sequence <code>s</code> that is an NCName, or
     * <code>null</code> if the character sequence <code>s</code> does not have a suffix that is an NCName.
     */
    public static String getNCNameSuffix(CharSequence s) {
        int localPartStartIndex = getNCNameSuffixIndex(s);
        if(localPartStartIndex != -1) {
            return s.toString().substring(localPartStartIndex);
        }
        else {
            return null;
        }
    }

    /**utility to get the part of a charsequence that is not the NCName fragment
     * @param s the charsequence to split
     * @return the prefix split at the last non-ncname character, or the whole input if no ncname is found*/
    public static String getNCNamePrefix(CharSequence s) {
        int localPartStartIndex = getNCNameSuffixIndex(s);
        if(localPartStartIndex != -1) {
            return s.toString().substring(0, localPartStartIndex);
        }
        else {
            return s.toString();
        }
    }

    /**
     * Escapes a character sequence so that it is valid XML.
     * @param s The character sequence.
     * @return The escaped version of the character sequence.
     */
    public static String escapeXML(CharSequence s) {
        // double quote -- quot
        // ampersand    -- amp
        // less than    -- lt
        // greater than -- gt
        // apostrophe   -- apos
        StringBuilder sb = new StringBuilder(s.length() * 2);
        for (int i = 0; i < s.length(); ) {
            int codePoint = Character.codePointAt(s, i);
            if (codePoint == '<') {
                sb.append(LT);
            }
            else if (codePoint == '>') {
                sb.append(GT);
            }
            else if (codePoint == '\"') {
                sb.append(QUOT);
            }
            else if (codePoint == '&') {
                sb.append(AMP);
            }
            else if (codePoint == '\'') {
                sb.append(APOS);
            }
            else {
                sb.appendCodePoint(codePoint);
            }
            i += Character.charCount(codePoint);
        }
        return sb.toString();
    }




    /**
     * Determines if a character sequence is <code>null</code> or empty.
     * @param s The character sequence.
     * @return <code>true</code> if the character sequence is <code>null</code>, <code>true</code> if the character
     * sequence is empty, otherwise <code>false</code>.
     */
    private static boolean isNullOrEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }
}
