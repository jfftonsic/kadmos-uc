package com.example.db.relational;

import org.hibernate.dialect.PostgreSQL94Dialect;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

import java.sql.Types;

public class CustomPostgreSqlDialect extends PostgreSQL94Dialect {

    public static final int POSTGRESQL_JSON = 1111;

    @Override
    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor)
    {
        return switch (sqlTypeDescriptor.getSqlType()) {
            case Types.CLOB, Types.BLOB, POSTGRESQL_JSON ->//1111 should be json of pgsql
                    VarcharTypeDescriptor.INSTANCE;
            default -> super.remapSqlTypeDescriptor(sqlTypeDescriptor);
        };
    }
    public CustomPostgreSqlDialect() {
        super();
        registerHibernateType(POSTGRESQL_JSON, "string");
    }
}
