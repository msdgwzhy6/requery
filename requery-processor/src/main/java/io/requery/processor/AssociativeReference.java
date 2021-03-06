/*
 * Copyright 2016 requery.io
 *
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

package io.requery.processor;

import io.requery.ReferentialAction;

import javax.lang.model.element.TypeElement;
import java.util.Objects;

/**
 * Defines a reference to another type element from one entity into another.
 *
 * @author Nikhil Purushe
 */
class AssociativeReference {

    private final String name;
    private final ReferentialAction action;
    private final TypeElement referenceType;

    AssociativeReference(String name, ReferentialAction action, TypeElement referenceType) {
        this.name = name;
        this.action = action;
        this.referenceType = referenceType;
    }

    String name() {
        return name;
    }

    ReferentialAction referentialAction() {
        return action;
    }

    TypeElement referencedType() {
        return referenceType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AssociativeReference) {
            AssociativeReference other = (AssociativeReference) obj;
            return Objects.equals(name, other.name) &&
                Objects.equals(action, other.action) &&
                Objects.equals(referenceType, other.referenceType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, action, referenceType);
    }
}
