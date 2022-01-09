package org.nh.pharmacy.config;

import io.github.jhipster.domain.util.FixedPostgreSQL95Dialect;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

import java.sql.Types;

public class AthmaPostgreSQL95Dialect extends FixedPostgreSQL95Dialect {

    public AthmaPostgreSQL95Dialect() {
    }

    public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
        return (SqlTypeDescriptor) (sqlTypeDescriptor.getSqlType() == Types.CLOB ? LongVarcharTypeDescriptor.INSTANCE : super.remapSqlTypeDescriptor(sqlTypeDescriptor));
    }

}
