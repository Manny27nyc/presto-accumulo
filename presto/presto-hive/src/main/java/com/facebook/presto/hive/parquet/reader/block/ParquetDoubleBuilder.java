/*
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
package com.facebook.presto.hive.parquet.reader.block;

import com.facebook.presto.hive.parquet.reader.ParquetLevelReader;
import com.facebook.presto.spi.block.BlockBuilderStatus;
import parquet.column.ColumnDescriptor;
import parquet.column.values.ValuesReader;

import static com.facebook.presto.spi.type.DoubleType.DOUBLE;

public class ParquetDoubleBuilder
        extends ParquetBlockBuilder
{
    public ParquetDoubleBuilder(int size, ColumnDescriptor descriptor)
    {
        super(descriptor, DOUBLE.createBlockBuilder(new BlockBuilderStatus(), size));
    }

    @Override
    public void readValues(ValuesReader valuesReader, int valueNumber, ParquetLevelReader definitionReader)
    {
        for (int i = 0; i < valueNumber; i++) {
            if (definitionReader.readLevel() == descriptor.getMaxDefinitionLevel()) {
                DOUBLE.writeDouble(blockBuilder, valuesReader.readDouble());
            }
            else {
                blockBuilder.appendNull();
            }
        }
    }
}
