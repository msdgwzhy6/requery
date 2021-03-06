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

package io.requery.sql;

import io.requery.proxy.Property;
import io.requery.query.Expression;
import io.requery.util.function.Predicate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

class EntityResultReader<E extends S, S> implements ResultReader<E>, Predicate<Property<E, ?>> {

    private final EntityReader<E, S> reader;
    private Collection selection;

    public EntityResultReader(EntityReader<E, S> reader) {
        this.reader = reader;
    }

    @Override
    public E read(ResultSet results, Set<? extends Expression<?>> selection) throws SQLException {
        if (this.selection == null) {
            this.selection = selection;
        }
        return reader.fromResult(null, results, this);
    }

    @Override
    public boolean test(Property<E, ?> value) {
        return selection.contains(value.attribute());
    }
}
