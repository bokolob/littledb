package storage.implementations.tables;

/*
    Table:
            Name
            Schema:
                    Column:
                            Name
                            Type
                    ValueSerializer
                    ValueDeserializer

    //NOW get table key f1,f2,f3 | get table key
    //NOW set table key f1="", f2="", ...
    //NOW create table colName=type,colName=type ...
    //FUTURE select from tableName f1, f2, fN where ...
 */

import java.io.IOException;
import java.util.List;

import storage.implementations.tables.data.TableInfo;
import storage.implementations.tables.data.TableSchema;

public interface TablesListService  {
    List<TableInfo> listTables();
    void createTable(String name, TableSchema tableSchema) throws IOException;
    //void dropTable(String name);
}
