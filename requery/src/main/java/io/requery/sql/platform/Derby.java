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

package io.requery.sql.platform;

import io.requery.sql.BaseType;
import io.requery.sql.BasicTypes;
import io.requery.sql.Keyword;
import io.requery.sql.Mapping;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;


/**
 * platform configuration for Apache Derby.
 */
public class Derby extends Generic {

    private static class CharBitData extends BaseType<byte[]> {

        CharBitData(int jdbcType) {
            super(byte[].class, jdbcType);
        }

        @Override
        public boolean hasLength() {
            return true;
        }

        @Override
        public Integer defaultLength() {
            return 32;
        }

        @Override
        public Object identifier() {
            switch (sqlType()) {
                case Types.BINARY:
                    return "char";
                case Types.VARBINARY:
                    return Keyword.VARCHAR;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public String identifierSuffix() {
            return "for bit data";
        }

        @Override
        public byte[] read(ResultSet results, int column) throws SQLException {
            byte[] value = results.getBytes(column);
            return results.wasNull() ? null : value;
        }
    }

    @Override
    public void addMappings(Mapping mapping) {
        super.addMappings(mapping);
        mapping.replaceType(BasicTypes.VARBINARY, new CharBitData(Types.VARBINARY));
        mapping.replaceType(BasicTypes.BINARY, new CharBitData(Types.BINARY));
        mapping.replaceType(BasicTypes.NVARCHAR, BasicTypes.VARCHAR);
    }

    @Override
    public boolean supportsIfExists() {
        return false;
    }

    @Override
    public boolean supportsGeneratedColumnsInPrepareStatement() {
        return false;
    }
}
