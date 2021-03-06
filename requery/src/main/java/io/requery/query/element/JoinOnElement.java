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

package io.requery.query.element;

import io.requery.query.Condition;
import io.requery.query.JoinAndOr;
import io.requery.query.JoinOn;
import io.requery.util.Objects;

import java.util.LinkedHashSet;
import java.util.Set;

public class JoinOnElement<E> implements JoinOn<E> {

    private final QueryElement<E> query;
    private final String table;
    private final JoinType joinType;
    private final Set<JoinElement<E>> conditions;

    JoinOnElement(QueryElement<E> query, String table, JoinType joinType) {
        this.query = query;
        this.table = table;
        this.joinType = joinType;
        this.conditions = new LinkedHashSet<>();
    }

    public String tableName() {
        return table;
    }

    public JoinType joinType() {
        return joinType;
    }

    public Set<JoinElement<E>> conditions() {
        return conditions;
    }

    @Override
    public <V> JoinAndOr<E> on(Condition<V> condition) {
        JoinElement<E> w = new JoinElement<>(query, conditions, condition, null);
        conditions.add(w);
        return w;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof JoinOnElement) {
            JoinOnElement other = (JoinOnElement) obj;
            return Objects.equals(table, other.table) &&
                   Objects.equals(joinType, other.joinType) &&
                   Objects.equals(conditions, other.conditions);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, joinType, conditions);
    }
}
