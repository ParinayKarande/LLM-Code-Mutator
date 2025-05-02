package constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public final class Headers {

    public static final String APACHE_LICENCE ="""
                /*
                 * Licensed to the Apache Software Foundation (ASF) under one
                 * or more contributor license agreements.  See the NOTICE file
                 * distributed with this work for additional information
                 * regarding copyright ownership.  The ASF licenses this file
                 * to you under the Apache License, Version 2.0 (the
                 * "License"); you may not use this file except in compliance
                 * with the License.  You may obtain a copy of the License at
                 *
                 *   https://www.apache.org/licenses/LICENSE-2.0
                 *
                 * Unless required by applicable law or agreed to in writing,
                 * software distributed under the License is distributed on an
                 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
                 * KIND, either express or implied.  See the License for the
                 * specific language governing permissions and limitations
                 * under the License.
                 */
                """;

    public static final String APACHE_IO_LICENSE = """
                    /*
                    * Licensed to the Apache Software Foundation (ASF) under one or more
                    * contributor license agreements.  See the NOTICE file distributed with
                    * this work for additional information regarding copyright ownership.
                    * The ASF licenses this file to You under the Apache License, Version 2.0
                    * (the "License"); you may not use this file except in compliance with
                    * the License.  You may obtain a copy of the License at
                    *
                    *      http://www.apache.org/licenses/LICENSE-2.0
                    *
                    * Unless required by applicable law or agreed to in writing, software
                    * distributed under the License is distributed on an "AS IS" BASIS,
                    * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                    * See the License for the specific language governing permissions and
                    * limitations under the License.
                    */
                """;

    public static final String APACHE_COLLECTIONS_LICENSE = """
            /*
             * Licensed to the Apache Software Foundation (ASF) under one or more
             * contributor license agreements.  See the NOTICE file distributed with
             * this work for additional information regarding copyright ownership.
             * The ASF licenses this file to You under the Apache License, Version 2.0
             * (the "License"); you may not use this file except in compliance with
             * the License.  You may obtain a copy of the License at
             *
             *      http://www.apache.org/licenses/LICENSE-2.0
             *
             * Unless required by applicable law or agreed to in writing, software
             * distributed under the License is distributed on an "AS IS" BASIS,
             * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
             * See the License for the specific language governing permissions and
             * limitations under the License.
             */
            """;


    /**
     * Collects all the headers and returns in a list
     * @return list of all headers in the Headers class.
     */
    public static List<String> getAllHeaders() {
        List<String> headers = new ArrayList<>();
        for (Field field : Headers.class.getFields()) {
            if (field.getType() == String.class &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers())) {
                try {
                    headers.add((String) field.get(null));
                } catch (IllegalAccessException ignored) {}
            }
        }
        return headers;
    }


}
