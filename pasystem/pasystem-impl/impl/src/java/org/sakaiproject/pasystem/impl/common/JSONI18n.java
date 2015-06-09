/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl.common;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sakaiproject.pasystem.api.I18n;
import org.sakaiproject.pasystem.api.I18nException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class JSONI18n implements I18n {

    private Map<String, String> translations;

    public JSONI18n(ClassLoader loader, String resourceBase, Locale locale) {
        String language = "default";

        if (locale != null) {
            language = locale.getLanguage();
        }

        InputStream stream = loader.getResourceAsStream(resourceBase + "/" + language + ".json");

        if (stream == null) {
            stream = loader.getResourceAsStream(resourceBase + "/default.json");
        }

        if (stream == null) {
            throw new I18nException("Missing default I18n file: " + resourceBase + "/default.json");
        }

        try {
            JSONParser parser = new JSONParser();
            translations = new ConcurrentHashMap((JSONObject) parser.parse(new InputStreamReader(stream)));
        } catch (IOException | ParseException e) {
            throw new I18nException("Failure when reading I18n stream", e);
        }
    }

    public String t(String key) {
        String result = translations.get(key);

        if (result == null) {
            throw new I18nException("Missing translation for key: " + key);
        }

        return result;
    }

}
